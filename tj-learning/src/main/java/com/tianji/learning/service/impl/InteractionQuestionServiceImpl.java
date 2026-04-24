package com.tianji.learning.service.impl;

import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.search.SearchClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CatalogueDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.service.IInteractionQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 * @author hh
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {
    private final InteractionReplyMapper replyMapper;
    private final UserClient userClient;
    private final CourseClient courseClient;
    private final SearchClient searchClient;
    private final CatalogueClient catalogueClient;
    private final CategoryCache categoryCache;
    /**
     * 保存问题
     * @param questionDTO 问题表单信息
     */
    @Override
    public void saveQuestion(QuestionFormDTO questionDTO) {
        //1.获取用户id
        Long userId = UserContext.getUser();
        //2.拷贝属性
        InteractionQuestion question = BeanUtils.copyBean(questionDTO, InteractionQuestion.class);
        question.setUserId(userId);
        save(question);
    }
    /**
     * 修改问题
     * @param id 问题id
     * @param questionDTO 问题表单信息
     */
    @Override
    public void updateQuestion(Long id, QuestionFormDTO questionDTO) {
        lambdaUpdate()
                .eq(InteractionQuestion::getId, id)
                .set(InteractionQuestion::getTitle, questionDTO.getTitle())
                .set(InteractionQuestion::getDescription, questionDTO.getDescription())
                .set(InteractionQuestion::getAnonymity, questionDTO.getAnonymity())
                .update();
    }
    /**
     * 查询问题列表
     * @param query 查询条件
     * @return 问题列表
     */
    @Override
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        Long courseId=query.getCourseId();
        Long sectionId=query.getSectionId();
        if(courseId==null&&sectionId==null){
            throw new BadRequestException("课程和小节id不能都为空");
        }
        //1.分页查询问题列表
        Page<InteractionQuestion> page = lambdaQuery()
                .select(InteractionQuestion.class,info -> !info.getProperty().equals("description"))//忽略description字段
                .eq(query.getOnlyMine(), InteractionQuestion::getUserId, UserContext.getUser())
                .eq(courseId != null, InteractionQuestion::getCourseId, courseId)
                .eq(sectionId != null, InteractionQuestion::getSectionId, sectionId)
                .eq(InteractionQuestion::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //2.根据id查询提问者和最后一次回答的信息
        Set<Long> userIds = new HashSet<>();
        Set<Long> answerIds = new HashSet<>();
        for(InteractionQuestion question : records){
            if(!question.getAnonymity()){
                userIds.add(question.getUserId());
            }
            answerIds.add(question.getLatestAnswerId());
        }
        //3.根据id查询最后一次回答的信息
        answerIds.remove(null);
        Map<Long, InteractionReply> replyMap=new HashMap<>(answerIds.size());
        if(CollUtils.isNotEmpty(answerIds)){
            List<InteractionReply> replies = replyMapper.selectBatchIds(answerIds);
            for (InteractionReply reply : replies){
                replyMap.put(reply.getId(), reply);
                if(!reply.getAnonymity()){
                    userIds.add(reply.getUserId());
                }
            }
        }

        //4.根据id获取提问用户信息
        userIds.remove(null);
        Map<Long, UserDTO> userMap = new HashMap<>(userIds.size());
        if(CollUtils.isNotEmpty(userIds)){
            List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
            userMap=userDTOS.stream().collect(Collectors.toMap(UserDTO::getId,u->u));
        }
        //5.封装vo
        List<QuestionVO> voList=new ArrayList<>(records.size());
        for(InteractionQuestion question : records){
            QuestionVO vo = BeanUtils.copyBean(question, QuestionVO.class);
            vo.setUserId(null);
            voList.add(vo);
            //封装提问者的信息
            if(!question.getAnonymity()){
                UserDTO userDTO=userMap.get(question.getUserId());
                if(userDTO!=null){
                    vo.setUserName(userDTO.getName());
                    vo.setUserIcon(userDTO.getIcon());
                    vo.setUserId(UserContext.getUser());
                }
            }
            //封装最后一次回答的信息
            InteractionReply reply = replyMap.get(question.getLatestAnswerId());
            if(reply!=null){
                vo.setLatestReplyContent(reply.getContent());
                if(!reply.getAnonymity()){
                    UserDTO userDTO=userMap.get(reply.getUserId());
                    vo.setLatestReplyUser(userDTO.getName());
                }
            }
        }
        return PageDTO.of(page,voList);
    }
    /**
     * 根据id查询问题
     * @param id 问题id
     * @return 问题信息
     */
    @Override
    public QuestionVO getQuestionById(Long id) {
        InteractionQuestion question = getById(id);
        if(question==null||question.getHidden()){
            return null;
        }
        //1.根据id查询提问者信息
        UserDTO userDTO =null;
        if(!question.getAnonymity()){
            userDTO=userClient.queryUserById(question.getUserId());
        }
        QuestionVO vo = BeanUtils.copyBean(question, QuestionVO.class);
        if(userDTO!=null){
            vo.setUserName(userDTO.getName());
            vo.setUserIcon(userDTO.getIcon());
        }
        return vo;
    }
    /**
     * 删除问题
     * @param id 问题id
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        //1.判断问题是否存在
        InteractionQuestion question = getById(id);
        if(question==null){
            return;
        }
        //2.判断问题是否属于当前用户
        if(!question.getUserId().equals(UserContext.getUser())){
            throw new BadRequestException("只能删除自己的问题");
        }
        //3.删除问题
        removeById(id);
        //4.删除问题下的回答及评论
        replyMapper.deleteByQuestionId(id);
    }

    @Override
    public PageDTO<QuestionAdminVO> queryQuestionPageAdmin(QuestionAdminPageQuery query) {
        //1.处理课程名称，得到课程id
        List<Long>courseIds=null;
        if(StringUtils.isNotEmpty(query.getCourseName())){
            courseIds = searchClient.queryCoursesIdByName(query.getCourseName());
            if(CollUtils.isEmpty(courseIds)){
                return PageDTO.empty(0L,0L);
            }
        }
        //2.分页查询
        Integer status=query.getStatus();
        LocalDateTime begin=query.getBeginTime();
        LocalDateTime end=query.getEndTime();
        Page<InteractionQuestion> page = lambdaQuery()
                .in(courseIds != null, InteractionQuestion::getCourseId, courseIds)
                .eq(status != null, InteractionQuestion::getStatus, status)
                .gt(begin != null, InteractionQuestion::getCreateTime, begin)
                .lt(end != null, InteractionQuestion::getCreateTime, end)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //3.准备vo需要的数据：用户数据，课表数据，章节数据
        //3.1获取各种数据的id集合
        Set<Long> userIds=new HashSet<>();
        Set<Long> cIds=new HashSet<>();
        Set<Long> chapterIds=new HashSet<>();
        for(InteractionQuestion question : records){
            userIds.add(question.getUserId());
            cIds.add(question.getCourseId());
            chapterIds.add(question.getChapterId());
            chapterIds.add(question.getSectionId());
        }
        //3.2根据id查询用户信息
        List<UserDTO> users = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO>userMap=new HashMap<>(users.size());
        if(CollUtils.isNotEmpty(users)){
            userMap=users.stream().collect(Collectors.toMap(UserDTO::getId,u->u));
        }
        //3.3根据id查询课表信息
        List<CourseSimpleInfoDTO> courses = courseClient.getSimpleInfoList(cIds);
        Map<Long, CourseSimpleInfoDTO> courseMap=new HashMap<>(courses.size());
        if(CollUtils.isNotEmpty(courses)){
            courseMap=courses.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId,c->c));
        }
        //3.4根据id查询章节信息
        List<CataSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(chapterIds);
        Map<Long, String> cataMap=new HashMap<>(catas.size());
        if(CollUtils.isNotEmpty(catas)){
            cataMap=catas.stream().collect(Collectors.toMap(CataSimpleInfoDTO::getId,CataSimpleInfoDTO::getName));
        }
        //3.5根据id查询分类信息
        //4.转换为vo
        List<QuestionAdminVO> voList = new ArrayList<>(records.size());
        for(InteractionQuestion question : records){
            QuestionAdminVO vo = BeanUtils.copyBean(question, QuestionAdminVO.class);
            voList.add(vo);
            UserDTO user=userMap.get(question.getUserId());
            if(user!=null){
                vo.setUserName(user.getName());
            }
            CourseSimpleInfoDTO course = courseMap.get(question.getCourseId());
            if(course!=null){
                vo.setCourseName(course.getName());
                List<Long> categoryIds = course.getCategoryIds();
                //获取分类名称
                vo.setCategoryName(categoryCache.getCategoryNames(categoryIds));
            }
            vo.setChapterName(cataMap.getOrDefault(question.getChapterId(), ""));
            vo.setSectionName(cataMap.getOrDefault(question.getSectionId(), ""));
        }

        return PageDTO.of(page,voList);
    }

    @Override
    public void updateQuestionHidden(Long id, Boolean hidden) {
        lambdaUpdate()
                .eq(InteractionQuestion::getId,id)
                .set(InteractionQuestion::getHidden,hidden)
                .update();
    }

    @Override
    public QuestionAdminVO getAdminQuestionById(Long id) {
        InteractionQuestion question = getById(id);
        if(question==null){
            return null;
        }
        QuestionAdminVO vo = BeanUtils.copyBean(question, QuestionAdminVO.class);
        //设置提问者姓名
        vo.setUserName(userClient.queryUserById(question.getUserId()).getName());
        //设置课程名称和分类
        CourseFullInfoDTO courseInfoById = courseClient.getCourseInfoById(question.getCourseId(), true, true);
        if(courseInfoById!=null){
            vo.setCourseName(courseInfoById.getName());
            vo.setCategoryName(categoryCache.getCategoryNames(courseInfoById.getCategoryIds()));
            List<Long> teacherIds =courseInfoById.getTeacherIds();
            List<UserDTO> userDTOS = userClient.queryUserByIds(teacherIds);
            if(CollUtils.isNotEmpty(userDTOS)){
                vo.setTeacherName(userDTOS.stream().map(UserDTO::getName).collect(Collectors.joining(",")));
            }
            //设置章节名称
            List<CatalogueDTO> chapters = courseInfoById.getChapters();
            for (CatalogueDTO chapter : chapters){
//                System.out.println(chapter);
                if(chapter.getId().equals(question.getChapterId())) {
                    vo.setChapterName(chapter.getName());
                }
                List<CatalogueDTO> sections = chapter.getSections();
                for (CatalogueDTO section : sections){
//                    System.out.println(section);
                    if(section.getId().equals(question.getSectionId())){
                        vo.setSectionName(section.getName());
                        break;
                    }
                }
            }
        }
        //将状态改为已查看
        lambdaUpdate()
                .eq(InteractionQuestion::getId,id)
                .set(InteractionQuestion::getStatus, QuestionStatus.CHECKED)
                .update();
        return vo;
    }
}
