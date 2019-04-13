package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class PostService {
    @Value("${system.page.post}")
    private int ONE_PAGE_POST_NUM;

    private PostMapper postMapper;

    private ReplyService replyService;

    private UserService userService;

    public PostService(@Autowired PostMapper postMapper, @Autowired ReplyService replyService,
                       @Autowired UserService userService) {
        this.postMapper = postMapper;
        this.replyService = replyService;
        this.userService = userService;
    }

    /**
     * 获取首页中显示的帖子信息
     *
     * @param builder
     * @param page    帖子页数偏移
     */

    public void getIndexPost(Integer page, String target, Integer type, MsgBuilder builder) {


    }

    /**
     * 获取公告
     */
//    @Cacheable(value = "posts", key = "'announcements'", unless = "#result==null")
    public List<Post> getAnnouncements() {
        return postMapper.getAnnouncements();
    }

    /**
     * 获取置顶帖
     */
//    @Cacheable(value = "posts", key = "'topposts'", unless = "#result==null")
    public List<Post> getTopPosts() {
        return postMapper.getTopPosts();
    }

    /**
     * 获取热帖，后期实现 todo 热帖实现
     */
//    @Cacheable(value = "posts", key = "'hotposts'", unless = "#result==null")
    public void getHotPosts() {

    }

    /**
     * 获取精品贴
     *
     * @return 帖子×8
     */

    public List<Post> getGoodPosts() {
        return postMapper.getGoodPosts();
    }

    /**
     * 获取帖子列表按发帖时间倒序
     *
     * @return 帖子×8
     */

    public List<Post> getPosts() {
        return postMapper.getPosts();
    }

    /**
     * 获取指定内容的帖子列表按发帖时间倒序
     *
     * @param target 搜索的内容
     * @return 帖子×8
     */
    //todo 加入回复内容模糊搜索
    public List<Post> getPosts(String target) {
        return postMapper.getPostsForTarget(target);
    }

    /**
     * 获取帖子列表按回复时间倒序
     *
     * @param offset 页码
     * @return 帖子×8
     */
    public List<Post> getPostsOrderByLastReplyTime(int offset) {
        return null;
    }

    /**
     * 创建新帖子
     *
     * @param post    帖子
     * @param result  错误信息
     * @param reply   回复
     * @param request
     * @return
     */
    @Transactional
    public ModelAndView createPost(Post post, BindingResult result, Reply reply,
                                   HttpServletResponse response) throws PostException {
        MsgBuilder builder = new MsgBuilder();
        //错误处理
        if (result.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            FieldError titleError = result.getFieldError("title");
            if (titleError != null) {
                stringBuilder.append(titleError.getDefaultMessage());
            }
            FieldError typeError = result.getFieldError("type");
            if (typeError != null) {
                stringBuilder.append(",").append(typeError.getDefaultMessage());
            }
            builder.addData("errorMsg", stringBuilder.toString());
            //fixme 可能需要考虑是否会重置表单，导致体验降低
            builder.redirectAndSendMsg(response, "/post/newPost", "errorMsg");
            return null;
        }

        //用户帖子数+1
        userService.addPostNum();

        //post表新增
        post.setUid(userService.getCurrentUser().getUid());
        postMapper.createPost(post);

        //reply表新增
        reply.setPostid(post.getPid());
        replyService.createPostTopReply(reply);

        //todo 消息处理（get：关注表 set：队列+数据库）
        return builder.getMsg("forward:/post/" + post.getPid());
    }

    /**
     * 构建主页
     *
     * @param type   类型0=普通 1=精品贴
     * @param page   页数
     * @param target 搜索内容
     * @return
     * @throws PageException
     */
    public ModelAndView buildHome(Integer type, Integer page, String target) throws PageException {
        MsgBuilder builder = new MsgBuilder();
        List<Post> postList;
        builder.addData("announcements", getAnnouncements());
        builder.addData("topPosts", getTopPosts());
        if (target.equals("")) {
            if (type == 0) {
                postList = getPosts();
            } else {
                postList = getGoodPosts();
            }
            builder.addData("type", type);
        } else {
            postList = getPosts(target);

        }
        //需要返回搜索内容
        builder.addData("page", page);
        builder.addData("target", target);
        if (postList != null && postList.size() > 0) {
            builder.addData("pageNum",
                    PageUtil.getPage(postList, ONE_PAGE_POST_NUM));
        }
        builder.addData("posts", PageUtil.subList(postList, page, ONE_PAGE_POST_NUM));
        return builder.getMsg("index");
    }

    /**
     * 浏览帖子
     *
     * @param pid   帖子id
     * @param floor 偏移楼层
     * @return
     */
    @RefreshHotPost(1)
    public ModelAndView viewPost(Integer pid, int floor) {
        MsgBuilder builder = new MsgBuilder();
        //todo 浏览量计算
        /**
         * 帖子信息获取
         */
        return builder.getMsg("/jie/detail");
    }

    private void computeView(HttpServletRequest request) {
        String ip = getIpAddress(request);
    }

    /**
     * 获取真实ip，可以避免代理
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
