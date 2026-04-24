package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.client.remark.RemarkClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.service.IInteractionReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 互动问题的回答或评论 服务实现类
 * </p>
 *
 * @author hh
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply> implements IInteractionReplyService {
    private final InteractionQuestionMapper questionMapper;
    private final UserClient userClient;
    private final RemarkClient remarkClient;
    @Override
    public void addReply(ReplyDTO replyDTO) {
        //1.写入回答或评论
        Long userId = UserContext.getUser();
        InteractionReply reply = BeanUtils.copyBean(replyDTO, InteractionReply.class);
        reply.setUserId(userId);
        //2.写入数据库并返回id
        save(reply);
        InteractionQuestion question = questionMapper.selectById(replyDTO.getQuestionId());
        //3.判断是否是回答
        boolean answer = replyDTO.getAnswerId() == 0;//为空表示是回答，不空为评论
        if (answer){
            //3.1如果是回答则更新最新的回答id和回答数量
            questionMapper.updateLatestIdAndAnswerTimes(reply.getId(),replyDTO.getQuestionId());
        }else {
            //3.2否则是评论则更新评论数量
            lambdaUpdate()
                    .eq(InteractionReply::getId,replyDTO.getTargetReplyId())
                    .setSql("reply_times=reply_times+1")
                    .update();
        }
        //4.判定是否是学生提交
        if(replyDTO.getIsStudent()){
            //更新问题表状态为未查看
            questionMapper.updateStatusById(replyDTO.getQuestionId());
        }
    }

    @Override
    public PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery query) {
        //分页查询
        Page<InteractionReply> page = lambdaQuery()
                .eq(query.getQuestionId() != null, InteractionReply::getQuestionId, query.getQuestionId())
                .eq(query.getAnswerId() != null, InteractionReply::getAnswerId, query.getAnswerId())
                .eq(InteractionReply::getHidden, false)
                .orderByDesc(InteractionReply::getLikedTimes)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionReply> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        List<ReplyVO> replyVOList=new ArrayList<>(records.size());
        List<Long> bizIds=new ArrayList<>(records.size());
        for (InteractionReply record : records) {
            bizIds.add(record.getId());
            ReplyVO replyVO = new ReplyVO();
            replyVO.setId(record.getQuestionId());
            replyVO.setContent(record.getContent());
            replyVO.setAnonymity(record.getAnonymity());
            replyVO.setCreateTime(record.getCreateTime());
            replyVO.setReplyTimes(record.getReplyTimes());
            replyVO.setLikedTimes(record.getLikedTimes());
            replyVOList.add(replyVO);
            if(!record.getAnonymity()){
                replyVO.setUserId(UserContext.getUser());
                UserDTO userDTO = userClient.queryUserById(record.getUserId());
                replyVO.setUserName(userDTO.getName());
                replyVO.setUserIcon(userDTO.getIcon());
            }
            if(record.getTargetUserId()!=0){
                UserDTO userDTO = userClient.queryUserById(record.getTargetUserId());
                replyVO.setTargetUserName(userDTO.getName());
            }
        }
        Set<Long> bizLikeds = remarkClient.isBizLiked(bizIds);
        //bizliked更新点赞状态为true
        for (ReplyVO replyVO : replyVOList) {
            replyVO.setLiked(bizLikeds.contains(replyVO.getId()));
        }
        return PageDTO.of(page,replyVOList);
    }

    @Override
    public PageDTO<ReplyVO> queryReplyAdminPage(ReplyPageQuery query) {
        //分页查询
        Page<InteractionReply> page = lambdaQuery()
                .eq(query.getQuestionId() != null, InteractionReply::getQuestionId, query.getQuestionId())
                .eq(query.getAnswerId() != null, InteractionReply::getAnswerId, query.getAnswerId())
                .orderByDesc(InteractionReply::getLikedTimes)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionReply> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        List<ReplyVO> replyVOList=new ArrayList<>(records.size());
        for (InteractionReply record : records) {
            ReplyVO replyVO = new ReplyVO();
            BeanUtils.copyProperties(record, replyVO);
            replyVO.setId(record.getQuestionId());
            replyVOList.add(replyVO);
            replyVO.setUserId(UserContext.getUser());
            UserDTO userDTO = userClient.queryUserById(record.getUserId());
            replyVO.setUserName(userDTO.getName());
            replyVO.setUserIcon(userDTO.getIcon());
            if(record.getTargetUserId()!=0){
                UserDTO userDTO1 = userClient.queryUserById(record.getTargetUserId());
                replyVO.setTargetUserName(userDTO1.getName());
            }
        }
        return PageDTO.of(page,replyVOList);
    }

    @Override
    public void updateHidden(Long id, Boolean hidden) {
        //判断是否是隐藏回答
        InteractionReply reply = getById(id);
        if(reply.getAnswerId()==0){
            lambdaUpdate()
                    .eq(InteractionReply::getId,id)
                    .set(InteractionReply::getHidden,hidden)
                    .update();
            //更新评论为隐藏状态
            lambdaUpdate()
                    .eq(InteractionReply::getAnswerId,id)
                    .set(InteractionReply::getHidden,hidden)
                    .update();
        }else{
            lambdaUpdate()
                    .eq(InteractionReply::getId,id)
                    .set(InteractionReply::getHidden,hidden)
                    .update();
        }
    }
}
