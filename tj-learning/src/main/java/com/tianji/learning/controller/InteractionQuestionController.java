package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 互动提问的问题表 前端控制器
 * </p>
 *
 * @author hh
 * @since 2026-02-02
 */
@RestController
@RequestMapping("/questions")
@Api(tags = "互动提问-问题管理接口")
@RequiredArgsConstructor
public class InteractionQuestionController {
    private final IInteractionQuestionService questionService;
    @ApiOperation("新增问题")
    @PostMapping
    public void saveQuestion(@Valid @RequestBody QuestionFormDTO questionDTO){
        questionService.saveQuestion(questionDTO);
    }
    @ApiOperation("修改问题")
    @PutMapping("/{id}")
    public void updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionFormDTO questionDTO){
        questionService.updateQuestion(id,questionDTO);
    }
    /**
     * 分页查询问题
     * @param
     */
    @ApiOperation("分页查询问题")
    @GetMapping("/page")
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query){
        return questionService.queryQuestionPage(query);
    }
    /**
     * 根据id查询问题详情
     */
    @ApiOperation("根据id查询问题详情")
    @GetMapping("/{id}")
    public QuestionVO queryQuestionById(@PathVariable Long id){
        return questionService.getQuestionById(id);
    }
    /**
     * 删除问题
     */
    @ApiOperation("删除问题")
    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id){
        questionService.deleteById(id);
    }

}
