package com.tianji.learning.utils;

import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
@Component
@Slf4j
@RequiredArgsConstructor
public class LearningRecordDelayTaskHandler {
    private final StringRedisTemplate redisTemplate;
    private final DelayQueue<DelayTask<RecordTaskData>> delayQueue=new DelayQueue<>();
    private static final String RECORD_KEY_TEMPLATE = "learning:record:{}";
    private final LearningRecordMapper recordMapper;
    private final ILearningLessonService lessonService;
    private static volatile boolean begin=true;
    /**
     * 写数据到redis并添加延迟任务
     * @param record
     */
    public void addLearningRecordDelayTask(LearningRecord  record){
        // 1.添加数据到Redis缓存
        writeRecordCache(record);
        // 2.提交延迟任务到延迟队列 DelayQueue
        delayQueue.add(new DelayTask<>(new RecordTaskData(record), Duration.ofSeconds(20)));
    }
    /**
     * 读取缓存中的学习记录
     * @param lessonId：课程id
     * @param sectionId：小节id
     * @return
     */
    public LearningRecord readRecordCache(Long lessonId, Long sectionId) {
        try {
            //1.读取redis
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
            Object cacheData = redisTemplate.opsForHash().get(key, sectionId.toString());
            if (cacheData == null){
                return null;
            }
            //2.数据转换
            return JsonUtils.toBean(cacheData.toString(), LearningRecord.class);
        } catch (Exception e) {
            log.error("读取学习记录缓存失败", e);
            return null;
        }

    }

    /**
     * 删除学习记录的缓存信息
     * @param lessonId
     * @param sectionId
     */
    public void removeRecordCache(Long lessonId, Long sectionId) {
        log.debug("删除学习记录的缓存信息");
        try {
            //1.删除Redis
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
            redisTemplate.opsForHash().delete(key, sectionId.toString());
        }catch (Exception e){
            log.error("删除学习记录的缓存信息失败", e);
        }
    }
    @PostConstruct
    public void init(){
        CompletableFuture.runAsync(this::handleDelayTask);
    }
    @PreDestroy
    public void destory(){
        begin=false;
        log.info("正在销毁延迟任务处理线程");
    }
    /*
    处理延迟任务
     */
    public void handleDelayTask() {
        log.info("开始处理延迟任务");
        while (true){
            try {
                //1.获取到期的延迟任务
                DelayTask<RecordTaskData> task = delayQueue.take();
                RecordTaskData data = task.getData();
                //2.查询redis缓存
                LearningRecord record = readRecordCache(data.getLessonId(), data.getSectionId());
                if (record == null){
                    continue;
                }
                //3.比较数据，moment值
                if (!Objects.equals(record.getMoment(), data.getMoment())){
                    //不一致，说明用户还在观看，放弃旧数据
                    continue;
                }
                //4.一致，需要持久化数据
                //4.1 更新学习记录的moment
                record.setFinished(null);
                recordMapper.updateById(record);
                //4.2 更新课表最近学习信息
                LearningLesson lesson=new LearningLesson();
                lesson.setId(data.getLessonId());
                lesson.setLatestSectionId(data.getSectionId());
                lesson.setLatestLearnTime(LocalDateTime.now());
                lessonService.updateById(lesson);
            } catch (Exception e) {
                log.error("延迟任务处理异常", e);
            }
        }
    }

    public void writeRecordCache(LearningRecord record) {
        log.debug("更新学习记录的缓存信息");
        try {
            //1.数据转换
            String jsonStr = JsonUtils.toJsonStr(new RecordCacheData(record));
            //2.写入Redis
            String key = StringUtils.format(RECORD_KEY_TEMPLATE, record.getLessonId());
            redisTemplate.opsForHash().put(key, record.getSectionId().toString(), jsonStr);
            //3.添加缓存过期时间
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }catch (Exception e){
            log.error("更新学习记录的缓存信息失败", e);
        }
    }
    @Data
    @NoArgsConstructor
    private static class RecordCacheData{
        private Long id;

        public RecordCacheData(LearningRecord record) {
            this.id = record.getId();
            this.moment =record.getMoment();
            this.finished = record.getFinished();
        }

        private Integer moment;
        private Boolean finished;
    }
    @Data
    @NoArgsConstructor
    private static class RecordTaskData{
        public RecordTaskData(LearningRecord  record) {
            this.lessonId = record.getLessonId();
            this.sectionId = record.getSectionId();
            this.moment = record.getMoment();
        }

        private Long lessonId;
        private Long sectionId;
        private Integer moment;
    }
}
