package com.tianji.learning.mapper;

import com.tianji.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 学生课程表 Mapper 接口
 * </p>
 *
 * @author hh
 * @since 2026-01-27
 */
public interface LearningLessonMapper extends BaseMapper<LearningLesson> {
    @Delete("delete from learning_lesson where course_id = #{courseId} and user_id = #{userId}")
    void removeBycourseId(Long courseId, Long userId);
    @Select("select id from learning_lesson where course_id = #{courseId} and user_id = #{userId} and status != 3")
    Long validLesson(Long courseId, Long userId);
    @Select("select count(1) from learning_lesson where course_id = #{courseId}")
    Integer countLearningLessonByCourse(Long courseId);
    @Select("select status from learning_lesson where course_id = #{courseId} and user_id=#{userId}")
    Integer queryLearningStatus(Long courseId,Long userId);
    @Select("select * from learning_lesson where user_id = #{userId} and course_id = #{courseId}")
    LearningLesson getOneByUserAndCourse(Long userId, Long courseId);
    Integer queryTotalPlan(@Param("userId") Long userId);
}
