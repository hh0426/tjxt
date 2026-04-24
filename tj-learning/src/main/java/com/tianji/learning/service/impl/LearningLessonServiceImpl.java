package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.IdAndNumDTO;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningLessonMapper;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生课程表 服务实现类
 * </p>
 *
 * @author hh
 * @since 2026-01-27
 */
@SuppressWarnings("ALL")
@Service
@Slf4j
@RequiredArgsConstructor
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {
    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    private final LearningLessonMapper learningLessonMapper;
    private final LearningRecordMapper recordMapper;

    @Override
    @Transactional
    public void addUserLessons(Long userId, List<Long> courseIds) {
        //1. 查询课程信息
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(simpleInfoList)) {
            // 课程不存在，无法添加
            log.error("课程信息不存在，无法添加到课表");
            return;
        }
        //2.循环遍历课程信息
        List<LearningLesson> list=new ArrayList<>(simpleInfoList.size());
        for (CourseSimpleInfoDTO info : simpleInfoList) {
            LearningLesson lesson = new LearningLesson();
            //2.1获取过期时间
            Integer validDuration = info.getValidDuration();
            lesson.setCreateTime(LocalDateTime.now());
            if(validDuration != null&& validDuration > 0){
                LocalDateTime expireTime = LocalDateTime.now().plusMonths(validDuration);
                lesson.setExpireTime(expireTime);
            }
            lesson.setUserId(userId);
            lesson.setCourseId(info.getId());
            list.add( lesson);
        }
        saveBatch( list);
    }

    @Override
    public PageDTO<LearningLessonVO> queryMylessons(PageQuery page) {
        //1.获取当前用户
        Long userId = UserContext.getUser();
        //2.分页查询
        Page<LearningLesson> lessonPage = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .page(page.toMpPage("latest_learn_time", false));
        //3.查询课程信息
        List<LearningLesson> list = lessonPage.getRecords();
        if(CollUtils.isEmpty(list)){
            return PageDTO.empty(lessonPage);
        }
        Map<Long, CourseSimpleInfoDTO> map = queryCourseSimpleInfoList(list);
        //4.封装vo返回
        List<LearningLessonVO> volist=new ArrayList<>(list.size());
        for(LearningLesson lesson : list){
            LearningLessonVO vo = BeanUtils.copyProperties(lesson, LearningLessonVO.class);
            vo.setCourseName(map.get(lesson.getCourseId()).getName());
            vo.setCourseCoverUrl(map.get(lesson.getCourseId()).getCoverUrl());
            vo.setSections(map.get(lesson.getCourseId()).getSectionNum());
            volist.add(vo);
        }
        return PageDTO.of(lessonPage, volist);
    }

    private Map<Long, CourseSimpleInfoDTO> queryCourseSimpleInfoList(List<LearningLesson> list) {
        Set<Long> cIds = list.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(cIds);
        if(CollUtils.isEmpty(simpleInfoList)){
            throw new RuntimeException("课程信息不存在");
        }
        //把课程信息转换成map
        Map<Long, CourseSimpleInfoDTO> map = simpleInfoList.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, v -> v));
        return map;
    }

    @Override
    public LearningLessonVO queryNowLearningLesson() {
        //1.获取当前用户
        Long userId = UserContext.getUser();
        //2.查询当前正在学习的课程
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                .orderByDesc(LearningLesson::getLatestLearnTime)
                .last("limit 1")
                .one();
        if(lesson == null){
            return null;
        }
        //3.拷贝po基础属性到vo
        LearningLessonVO vo = BeanUtils.copyProperties(lesson, LearningLessonVO.class);
        //4.查询课程信息
        CourseFullInfoDTO courseInfoById = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if(courseInfoById == null){
            throw new RuntimeException("课程信息不存在");
        }
        vo.setCourseName(courseInfoById.getName());
        vo.setCourseCoverUrl(courseInfoById.getCoverUrl());
        vo.setSections(courseInfoById.getSectionNum());
        //5.统计课程数量
        Integer count = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .count();
        vo.setCourseAmount(count);
        //6.查询课程小节信息
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(CollUtils.singletonList(lesson.getLatestSectionId()));
        if(!CollUtils.isEmpty(cataSimpleInfoDTOS)){
            CataSimpleInfoDTO cataSimpleInfoDTO = cataSimpleInfoDTOS.get(0);
            vo.setLatestSectionName(cataSimpleInfoDTO.getName());
            vo.setLatestSectionIndex(cataSimpleInfoDTO.getCIndex());
        }
        return vo;
    }

    @Override
    public void removeBycourseId(Long courseId) {
        Long userId = UserContext.getUser();
        learningLessonMapper.removeBycourseId(courseId,userId);
    }

    @Override
    public Long validLesson(Long courseId) {
        //1.获取当前用户
        Long userId = UserContext.getUser();
        return learningLessonMapper.validLesson(courseId,userId);
    }

    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        return learningLessonMapper.countLearningLessonByCourse(courseId);
    }

    @Override
    public Integer queryLearningStatus(Long courseId) {
        Long userId = UserContext.getUser();
        return learningLessonMapper.queryLearningStatus(courseId,userId);
    }

    @Override
    public LearningLesson getOneByUserAndCourse(Long userId, Long courseId) {
        return learningLessonMapper.getOneByUserAndCourse(userId,courseId);
    }

    @Override
    public void createUserLessons(Long courseId, Integer freq) {
        Long userId = UserContext.getUser();
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson == null){
            throw new BadRequestException("课程信息不存在");
        }
        LearningLesson l=new LearningLesson();
        l.setId(lesson.getId());
        l.setWeekFreq(freq);
        if(lesson.getPlanStatus()== PlanStatus.NO_PLAN){
            l.setPlanStatus(PlanStatus.PLAN_RUNNING);
        }
        updateById(l);
    }

    @Override
    public LearningPlanPageVO queryLearningPlans(PageQuery page) {
        LearningPlanPageVO pageVO = new LearningPlanPageVO();
        //1.获取当前用户
        Long userId = UserContext.getUser();
        //2.获取本周起始时间
        LocalDateTime weekStart = DateUtils.getWeekBeginTime(LocalDate.now());
        LocalDateTime weekEnd = DateUtils.getWeekEndTime(LocalDate.now());
        //3.查询总的统计信息: 本周计划数，本周完成数，本周学习积分
        Integer weekFinished = recordMapper.selectCount(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getFinished, true)
                .between(LearningRecord::getCreateTime, weekStart, weekEnd)
        );
        pageVO.setWeekFinished(weekFinished);
        Integer weekTotalPlan = getBaseMapper().queryTotalPlan(userId);
        pageVO.setWeekTotalPlan(weekTotalPlan);
        //4.分页查询
        Page<LearningLesson> p = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .in(LearningLesson::getStatus, LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .orderByDesc(LearningLesson::getCreateTime)
                .page(page.toMpPage("latest_learn_time", false));
        List<LearningLesson> records =p.getRecords();
        if(CollUtils.isEmpty(records)){
            return pageVO.pageInfo(PageDTO.empty(p));
        }
        Map<Long, CourseSimpleInfoDTO> map = queryCourseSimpleInfoList(records);
        //统计每个课程每周学习的小节数
        List<IdAndNumDTO> list=recordMapper.countLearnedSections(userId,weekStart,weekEnd);
        Map<Long, Integer> countMap = IdAndNumDTO.toMap(list);
        System.out.println(countMap);
        //组装vo
        List<LearningPlanVO> voList = new ArrayList<>(records.size());
        for (LearningLesson record : records) {
            LearningPlanVO vo = BeanUtils.copyBean(record, LearningPlanVO.class);
            CourseSimpleInfoDTO cInfo = map.get(record.getCourseId());
            if(cInfo != null){
                vo.setCourseName(cInfo.getName());
                vo.setSections(cInfo.getSectionNum());
            }
            vo.setWeekLearnedSections(countMap.getOrDefault(record.getId(), 0));
            voList.add(vo);
        }
        return pageVO.pageInfo(p.getTotal(),p.getPages(),voList);
    }
}
