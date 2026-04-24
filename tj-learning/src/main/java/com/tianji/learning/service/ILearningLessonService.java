package com.tianji.learning.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;

import java.util.List;

/**
 * <p>
 * 学生课程表 服务类
 * </p>
 *
 * @author hh
 * @since 2026-01-27
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    void addUserLessons(Long userId, List<Long> courseIds);

    PageDTO<LearningLessonVO> queryMylessons(PageQuery page);

    LearningLessonVO queryNowLearningLesson();

    void removeBycourseId(Long courseId);

    Long validLesson(Long courseId);

    Integer countLearningLessonByCourse(Long courseId);

    Integer queryLearningStatus(Long courseId);

    LearningLesson getOneByUserAndCourse(Long userId, Long courseId);

    void createUserLessons(Long courseId, Integer freq);

    LearningPlanPageVO queryLearningPlans(PageQuery page);
}
