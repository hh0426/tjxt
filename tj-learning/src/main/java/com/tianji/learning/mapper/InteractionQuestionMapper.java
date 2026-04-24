package com.tianji.learning.mapper;

import com.tianji.learning.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 互动提问的问题表 Mapper 接口
 * </p>
 *
 * @author hh
 * @since 2026-02-02
 */
public interface InteractionQuestionMapper extends BaseMapper<InteractionQuestion> {
    @Update("update interaction_question set latest_answer_id = #{answerId}, answer_times = answer_times + 1 where id = #{questionId}")
    void updateLatestIdAndAnswerTimes(Long answerId,Long questionId);
    @Update("update interaction_question set status = 0 where id = #{questionId}")
    void updateStatusById(Long questionId);
}
