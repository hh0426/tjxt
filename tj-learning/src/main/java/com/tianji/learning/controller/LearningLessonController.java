package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 学生课程表 前端控制器
 * </p>
 *
 * @author hh
 * @since 2026-01-27
 */
@Slf4j
@RestController
@RequestMapping("/lessons")
@Api(tags = "我的课表相关接口")
@RequiredArgsConstructor
public class LearningLessonController {
    private final ILearningLessonService lessonService;
    /**
     * 分页查询我的课程列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询我的课程列表")
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery page) {
        log.info("分页查询我的课程列表，参数：{}", page);
        return lessonService.queryMylessons(page);
    }
    /**
     * 查询正在学习的课程
     */
    @GetMapping("/now")
    @ApiOperation("查询正在学习的课程")
    public LearningLessonVO queryNowLearningLesson() {
        return lessonService.queryNowLearningLesson();
    }
    /**
     * 删除课程
     */
    @DeleteMapping("/{courseId}")
    @ApiOperation("删除课程")
    public void deleteLesson(@PathVariable Long courseId) {
        log.info("删除课程{}", courseId);
        lessonService.removeBycourseId(courseId);
    }
    /**
     * 校验指定课程是否是课表中的有效课程
     */
    @GetMapping("/{courseId}/valid")
    @ApiOperation("校验指定课程是否是课表中的有效课程")
    public Long isLessonValid(@PathVariable Long courseId) {
        log.info("校验课程{}", courseId);
        Long id= lessonService.validLesson(courseId);
        log.info("课程{}有效", id);
        return id;
    }
    /**
     * 统计课程人数
     */
    @GetMapping("/{courseId}/count")
    @ApiOperation("统计课程人数")
    public Integer countLearningLessonByCourse(@PathVariable Long courseId) {
        log.info("统计课程{}人数", courseId);
        Integer count = lessonService.countLearningLessonByCourse(courseId);
        log.info("课程{}人数为{}", courseId, count);
        return count;
    }
    /**
     * 根据id查询课程的学习状态
     */
    @GetMapping("/{courseId}")
    @ApiOperation("根据id查询课程的学习状态")
    public Integer queryLearningStatus(@PathVariable Long courseId) {
        log.info("查询课程{}学习状态", courseId);
        Integer status = lessonService.queryLearningStatus(courseId);
        log.info("课程{}学习状态为{}", courseId, status);
        return status;
    }
    /**
     * 添加学习计划
     */
    @PostMapping("/plans")
    @ApiOperation("添加学习计划")
    public void createLearningPlan(@Valid  @RequestBody LearningPlanDTO plan) {
        log.info("添加学习计划{}", plan);
        lessonService.createUserLessons(plan.getCourseId(), plan.getFreq());
    }
    /**
     * 获取学习计划
     */
    @GetMapping("/plans")
    @ApiOperation("获取学习计划")
    public LearningPlanPageVO queryLearningPlans(PageQuery page) {
        log.info("获取学习计划", page);
        return lessonService.queryLearningPlans(page);
    }

}
