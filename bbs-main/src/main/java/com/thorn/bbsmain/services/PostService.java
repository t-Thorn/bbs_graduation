package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostNotFoundException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.PageUtil;
import impl.HotPointManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    @Value("${system.page.post}")
    private int ONE_PAGE_POST_NUM;


    private PostMapper postMapper;

    private ReplyService replyService;

    private UserService userService;

    private HotPointManager manager;

    public PostService(@Autowired PostMapper postMapper, @Autowired ReplyService replyService,
                       @Autowired UserService userService, HotPointManager manager) {
        this.postMapper = postMapper;
        this.replyService = replyService;
        this.userService = userService;
        this.manager = manager;
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
     * 获取热帖
     */
//    @Cacheable(value = "posts", key = "'hotposts'", unless = "#result==null")
    private void getHotPosts(MsgBuilder builder) {
        List<Post> hotPostList = manager.getTopPost();
        Map<Integer, Long> hotPoints = manager.getHotPoint();
        hotPostList.forEach(element -> element.setHotPoint(hotPoints.get(element.getPid())));
        builder.addData("hotPosts", Collections.unmodifiableList(hotPostList));
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
     * 创建新帖子
     *
     * @param post    帖子
     * @param result  错误信息
     * @param reply   回复
     * @param response
     * @return
     */
    @Transactional
    public ModelAndView createPost(Post post, BindingResult result, Reply reply,
                                   HttpServletResponse response) {
        MsgBuilder builder = new MsgBuilder();
        //错误处理
        if (result.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            FieldError titleError = result.getFieldError("title");
            //todo errormsg用layer.msg显示
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
        reply.setReplyer(userService.getCurrentUser().getUid());
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
        if (page < 1) {
            throw new PageException("非法参数");
        }
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

        getHotPosts(builder);
        return builder.getMsg("index");
    }

    /**
     * 浏览帖子
     *
     * @param pid   帖子id
     * @param floor 偏移楼层
     * @param errorMsg
     * @return
     */
    @RefreshHotPost()
    public ModelAndView viewPost(Integer pid, Integer floor, int page, String errorMsg) throws PostNotFoundException,
            PageException {
        if (page == 0) {
            throw new PageException("页数参数错误");
        }
        MsgBuilder builder = new MsgBuilder();
        /**
         * 帖子信息获取
         */
        Post post = postMapper.getPost(pid);
        if (post == null) {
            System.out.println("未找到页面");
            throw new PostNotFoundException("未找到页面");
        }
        //构建帖子头信息
        builder.addData("hotPoint", manager.getHotPoint(pid));
        builder.addData("post", post);

        builder.addData("replys", replyService.getReplies(pid, builder, page));
        builder.addData("floor", floor);
        builder.addData("errorMsg", errorMsg);
        getHotPosts(builder);
        return builder.getMsg("/jie/detail");
    }


}
