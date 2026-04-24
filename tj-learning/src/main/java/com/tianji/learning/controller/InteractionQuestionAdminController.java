package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 互动提问的问题表 前端控制器
 * </p>
 *
 * @author hh
 */
@RestController
@RequestMapping("/admin/questions")
@Api(tags = "管理端：互动问答相关接口")
@RequiredArgsConstructor
public class InteractionQuestionAdminController {

    private final IInteractionQuestionService questionService;

    @ApiOperation("管理端分页查询互动问题")
    @GetMapping("page")
    public PageDTO<QuestionAdminVO> queryQuestionPageAdmin(QuestionAdminPageQuery query){
        return questionService.queryQuestionPageAdmin(query);
    }
    /**
     * 隐藏或显示问题
     * @param id 问题id
     * @param hidden 是否隐藏
     */
    @ApiOperation("隐藏或显示问题")
    @PutMapping("/{id}/hidden/{hidden}")
    public void updateQuestionHidden(@ApiParam("问题id") @PathVariable("id") Long id,
                                     @ApiParam("是否隐藏") @PathVariable("hidden") Boolean hidden){
        questionService.updateQuestionHidden(id, hidden);
    }
    /**
     * 根据id查询问题详情
     * @param id 问题id
     */
    @ApiOperation("根据id查询问题详情")
    @GetMapping("/{id}")
    public QuestionAdminVO getQuestionById(@ApiParam("问题id") @PathVariable("id") Long id){
        return questionService.getAdminQuestionById(id);
    }
}