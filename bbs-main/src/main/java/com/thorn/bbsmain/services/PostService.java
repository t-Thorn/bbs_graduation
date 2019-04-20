package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
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
     * 获取公告
     */

    public List<Post> getAnnouncements() {
        return postMapper.getAnnouncements();
    }

    /**
     * 获取置顶帖
     */

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

    public List<Post> getGoodPosts(int page) {
        return postMapper.getGoodPosts(page * ONE_PAGE_POST_NUM, ONE_PAGE_POST_NUM);
    }

    /**
     * 获取帖子列表按发帖时间倒序
     *
     * @return 帖子×8
     */

    public List<Post> getPosts(int page) {
        return postMapper.getPosts(page * ONE_PAGE_POST_NUM, ONE_PAGE_POST_NUM);
    }

    /**
     * 获取指定内容的帖子列表按发帖时间倒序
     *
     * @param target 搜索的内容
     * @return 帖子×8
     */

    public List<Post> getPosts(String target, int page) {
        return postMapper.getPostsForTarget(target, page * ONE_PAGE_POST_NUM, ONE_PAGE_POST_NUM);
    }

    /**
     * 创建新帖子
     *
     * @param post     帖子
     * @param result   错误信息
     * @param reply    回复
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
            if (titleError != null) {
                stringBuilder.append(titleError.getDefaultMessage());
            }
            FieldError typeError = result.getFieldError("type");
            if (typeError != null) {
                stringBuilder.append(",").append(typeError.getDefaultMessage());
            }
            builder.addData("errorMsg", stringBuilder.toString());

            return builder.getMsg("/post/newPost");
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
                postList = getPosts(page - 1);
                builder.addData("pageNum", PageUtil.getPage(getPostNum(), ONE_PAGE_POST_NUM));
            } else {
                postList = getGoodPosts(page - 1);
                builder.addData("pageNum", PageUtil.getPage(getGoodPostNum(), ONE_PAGE_POST_NUM));
            }
        } else {
            postList = getPosts(target, page - 1);
            builder.addData("pageNum", PageUtil.getPage(getPostNum(target), ONE_PAGE_POST_NUM));
        }

        //需要返回搜索内容
        builder.addData("type", type);
        builder.addData("page", page);
        builder.addData("target", target);
        builder.addData("posts", postList.size() == 0 ? null : postList);

        getHotPosts(builder);
        return builder.getMsg("index");
    }

    /**
     * 浏览帖子
     *
     * @param pid      帖子id
     * @param floor    偏移楼层
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

    public int getPostNum() {
        return postMapper.getPostNum();
    }

    public int getPostNum(String target) {
        return postMapper.getPostNumOfTarget(target);
    }

    public int getGoodPostNum() {
        return postMapper.getGoodPostNum();
    }

    public void delCollect(Integer to) throws PostException {
        //帖子收藏数-1

        //删除收藏
    }

    public void collect(Integer to) throws PostException {
        //帖子收藏数+1

        //新建收藏
    }
}
