package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 互动问题的回答或评论 前端控制器
 * </p>
 *
 * @author hh
 * @since 2026-02-02
 */
@RestController
@RequestMapping("/admin/replies")
@Api(tags = "管理端互动回复相关接口")
@RequiredArgsConstructor
public class InteractionReplyAdminController {
    private final IInteractionReplyService replyService;
    /**
     * 分页查询回答或评论
     */
    @GetMapping("/page")
    @ApiOperation("管理端分页查询回答或评论")
    public PageDTO<ReplyVO>queryReplyAdminPage(ReplyPageQuery query) {
        return replyService.queryReplyAdminPage(query);
    }
    /**
     * 显示或隐藏回答或评论
     */
    @PostMapping("/{id}/hidden/{hidden}")
    @ApiOperation("管理端显示或隐藏回答或评论")
    public void updateReplyHidden(@PathVariable("id") Long id, @PathVariable("hidden") Boolean hidden) {
        replyService.updateHidden(id, hidden);
    }

}
