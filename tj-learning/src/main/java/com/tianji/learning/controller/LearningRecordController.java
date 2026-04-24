package com.tianji.learning.controller;


import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.service.ILearningRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 学习记录表 前端控制器
 * </p>
 *
 * @author hh
 * @since 2026-01-30
 */
@RestController
@RequestMapping("/learning-records")
@Api(tags = "学习记录的相关接口")
@RequiredArgsConstructor
public class LearningRecordController {
    public final ILearningRecordService learningRecordService;
    /**
     * 查询当前用户指定课程的学习进度
     * @param courseId 课程id
     * @return 课表信息、学习记录及进度信息
     */
    @GetMapping("/course/{courseId}")
    @ApiOperation("查询当前用户指定课程的学习进度")
    public LearningLessonDTO queryLearningRecordByCourse(
            @ApiParam (value="课程id",example = "1",required = true) @PathVariable("courseId") Long courseId){
        return learningRecordService.queryLearningRecordByCourse(courseId);
    }
    /**
     * 新增学习记录
     */
    @PostMapping
    @ApiOperation("新增学习记录")
    public void addLearningRecord(@RequestBody LearningRecordFormDTO formDTO){
        learningRecordService.addLearningRecord(formDTO);
    }

}
