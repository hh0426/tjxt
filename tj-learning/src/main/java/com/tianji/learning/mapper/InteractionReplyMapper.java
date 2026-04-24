package com.tianji.learning.mapper;

import com.tianji.learning.domain.po.InteractionReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 互动问题的回答或评论 Mapper 接口
 * </p>
 *
 * @author hh
 * @since 2026-02-02
 */
public interface InteractionReplyMapper extends BaseMapper<InteractionReply> {
    @Delete("delete from interaction_reply where question_id=#{id}")
    void deleteByQuestionId(Long id);
}
