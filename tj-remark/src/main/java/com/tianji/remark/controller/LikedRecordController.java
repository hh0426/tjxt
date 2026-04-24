package com.tianji.remark.controller;


import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.service.ILikedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 点赞记录表 前端控制器
 * </p>
 *
 * @author hh
 * @since 2026-02-05
 */
@RestController
@RequestMapping("/likes")
@Api(tags = "点赞记录接口")
@RequiredArgsConstructor
public class LikedRecordController {
    private final ILikedRecordService likedRecordService;
    /**
     * 点赞/取消点赞
     * @param recordDTO
     * @return
     */
    @PostMapping
    @ApiOperation("点赞/取消点赞")
    public void addlikeRecord(@Valid @RequestBody LikeRecordFormDTO recordDTO) {
        likedRecordService.addlikeRecord(recordDTO);
    }
    /**
     * 查询指定业务id的点赞状态
     * @param bizIds
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询指定业务id的点赞状态")
    public Set<Long> isBizliked(@RequestParam List<Long> bizIds) {
        return likedRecordService.isBizLiked(bizIds);
    }


}
