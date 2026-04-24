package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.SectionType;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.utils.LearningRecordDelayTaskHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 学习记录表 服务实现类
 * </p>
 *
 * @author hh
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {
    private final ILearningLessonService lessonService;
    private final CourseClient courseClient;
    private final LearningRecordDelayTaskHandler taskHandler;

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        //1.获取当前用户
        Long userId = UserContext.getUser();
        //2.查询课表
        LearningLesson lesson = lessonService.getOneByUserAndCourse(userId, courseId);
        if (lesson == null) {
            return null;
        }
        //3.查询学习记录
        List<LearningRecord> records = lambdaQuery()
                .eq(LearningRecord::getLessonId, lesson.getId())
                .list();
        //4.组装数据并返回
        LearningLessonDTO dto = new LearningLessonDTO();
        dto.setId(lesson.getId());
        dto.setLatestSectionId(lesson.getLatestSectionId());
        List<LearningRecordDTO> learningRecordDTOS = BeanUtils.copyList(records, LearningRecordDTO.class);
        dto.setRecords(learningRecordDTOS);
        return dto;
    }

    @Override
    @Transactional
    public void addLearningRecord(LearningRecordFormDTO recordFormDTO) {
        //1.获取当前用户
        Long userId = UserContext.getUser();
        //2.处理学习记录
        boolean finished = false;
        if(recordFormDTO.getSectionType() == SectionType.VIDEO){
            //2.1处理视频
            finished=handleVideoRecord(recordFormDTO, userId);
        }else{
            //2.2处理考试
            finished=handleExamRecord(recordFormDTO, userId);
        }
        if(!finished){
            return;
        }
        //3.处理课表数据
        handleLearningLessonChanges(recordFormDTO);
    }

    private void handleLearningLessonChanges(LearningRecordFormDTO recordFormDTO) {
        //1.查询课表
        LearningLesson lesson = lessonService.getById(recordFormDTO.getLessonId());
        if(lesson == null){
            throw new BizIllegalException("课表不存在");
        }
        //2.判断是否有新完成的小节
        boolean allLearned=false;
            //3.如果存在则需要查询课程数据
            CourseFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
            if(cInfo == null){
                throw new BizIllegalException("课程信息不存在");
            }
            //4.比较课程是否全部学完
            allLearned=lesson.getLearnedSections()+1>=cInfo.getSectionNum();
        //5.更新课表数据
        lessonService.lambdaUpdate()
                .set(lesson.getLearnedSections()==0, LearningLesson::getStatus, LessonStatus.LEARNING)
                .set(allLearned, LearningLesson::getStatus, LessonStatus.FINISHED)
                .setSql("learned_sections=learned_sections+1")
                .eq(LearningLesson::getId, lesson.getId())
                .update();
    }

    private boolean handleVideoRecord(LearningRecordFormDTO recordFormDTO, Long userId) {
        //1.查询旧的学习记录
        LearningRecord old =queryOldRecord(recordFormDTO.getLessonId(), recordFormDTO.getSectionId());
        //2.判断是否存在
        if(old == null){
            //3.不存在则新增
            LearningRecord record = BeanUtils.copyBean(recordFormDTO, LearningRecord.class);
            record.setUserId(userId);
            boolean success = save(record);
            if(!success){
                throw new RuntimeException("保存学习记录失败");
            }
            return false;
        }
        //4.存在则更新
        //判断是否是第一次完成
        boolean finished=!old.getFinished()&&recordFormDTO.getMoment()*2>=recordFormDTO.getDuration();
        if(!finished){
            LearningRecord record = new LearningRecord();
            record.setLessonId(recordFormDTO.getLessonId());
            record.setSectionId(recordFormDTO.getSectionId());
            record.setMoment(recordFormDTO.getMoment());
            record.setId(old.getId());
            record.setFinished(old.getFinished());
            taskHandler.addLearningRecordDelayTask(record);
        }
        boolean success = lambdaUpdate()
                .set(LearningRecord::getMoment, recordFormDTO.getMoment())
                .set(LearningRecord::getFinished, finished)
                .set(LearningRecord::getFinishTime, recordFormDTO.getCommitTime())
                .eq(LearningRecord::getId, old.getId())
                .update();
        if(!success){
            throw new RuntimeException("更新学习记录失败");
        }
        //4.3 清理缓存
        taskHandler.readRecordCache(recordFormDTO.getLessonId(), recordFormDTO.getSectionId());
        return true;
    }

    private LearningRecord queryOldRecord(Long lessonId, Long sectionId) {
        //1.查询缓存
        LearningRecord learningRecord = taskHandler.readRecordCache(lessonId, sectionId);

        //2.如果命中，则返回
        if(learningRecord != null){
            return learningRecord;
        }
        //3.如果没有命中，则查询数据库
        learningRecord = lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .eq(LearningRecord::getSectionId, sectionId)
                .one();
        //4.写入缓存
        taskHandler.writeRecordCache(learningRecord);
        return learningRecord;
    }

    private boolean handleExamRecord(LearningRecordFormDTO recordFormDTO, Long userId) {
        //转dto为po
        LearningRecord record = BeanUtils.copyBean(recordFormDTO, LearningRecord.class);
        record.setUserId(userId);
        record.setFinished(true);
        record.setFinishTime(recordFormDTO.getCommitTime());
        boolean success = save(record);
        if(!success){
            throw new RuntimeException("保存考试记录失败");
        }
        return true;
    }
}
