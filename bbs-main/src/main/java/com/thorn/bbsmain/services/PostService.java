package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.exceptions.PostNotFoundException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import domain.HotPoint;
import impl.HotPointManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
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
    private void getHotPosts(MsgBuilder builder) {
        List<Post> hotPostList = manager.getTopPost();
        Map<Integer, Long> hotPoints = manager.getHotPoint();
        if (Objects.nonNull(hotPostList) && Objects.nonNull(hotPoints)) {
            hotPostList.forEach(element -> {
                Long hotPoint = Optional.ofNullable(hotPoints.get(element.getPid())).orElse(0l);
                element.setHotPoint(hotPoint);
            });
        }
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
        return builder.getMsg("forward:/post/" + post.getPid());
    }

    /**
     * 构建主页
     *
     * @param type   类型0=普通 1=精品贴
     * @param page   页数
     * @param target 搜索内容
     * @param errorMsg
     * @return
     * @throws PageException
     */
    public ModelAndView buildHome(Integer type, Integer page, String target, String errorMsg) throws PageException {
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
                builder.addData("pageNum", MyUtil.getPage(getPostNum(), ONE_PAGE_POST_NUM));
            } else {
                postList = getGoodPosts(page - 1);
                builder.addData("pageNum", MyUtil.getPage(getGoodPostNum(), ONE_PAGE_POST_NUM));
            }
        } else {
            postList = getPosts(target, page - 1);
            builder.addData("pageNum", MyUtil.getPage(getPostNum(target), ONE_PAGE_POST_NUM));
        }

        //需要返回搜索内容
        builder.addData("errorMsg", errorMsg);
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
     * @param replyID    偏移楼层
     * @param errorMsg
     * @return
     */
    @RefreshHotPost()
    public ModelAndView viewPost(Integer pid, Integer replyID, int page, String errorMsg) throws PostNotFoundException,
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
        User currentUser = userService.getCurrentUser();
        //检测是否收藏帖子
        if (currentUser != null) {
            if (post.getUid().equals(currentUser.getUid())) {
                builder.addData("myself", true);
            } else {
                builder.addData("myself", false);
                if (userService.collectRelationshipIsExist(currentUser.getUid(), pid)) {
                    builder.addData("collect", true);
                } else {
                    builder.addData("collect", false);
                }
            }

        }

        //构建帖子头信息
        HotPoint hotPoint = manager.getHotPoint(pid);
        if (hotPoint != null) {
            builder.addData("hotPoint", hotPoint.getTotal());
            post.setViews(hotPoint.getView() + post.getViews());
        }
        builder.addData("post", post);

        builder.addData("replys", replyService.getReplies(pid, builder, page));
        builder.addData("replyID", replyID);
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


    public String delPost(int pid) throws PostException {
        if (pid < 1) {
            throw new PostException("删除帖子参数错误");
        }
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new PostException("请登录再试");
        }
        if (!isExist(pid)) {
            throw new PostException("帖子不存在");
        }
        if (!userService.hasRole("admin") || postMapper.hasPermission(user.getUid(), pid) == 0) {
            throw new PostException("没有权限删除");
        }

        MsgBuilder builder = new MsgBuilder();
        //回复可用置0
        if (postMapper.invalidReply(pid) > 0) {
            postMapper.decreaseReplyNum(pid);
        } else {
            throw new PostException("已被删除");
        }
        //帖子回复数-1

        builder.addData("msg", "成功");
        return builder.getMsg();
    }

    private boolean isExist(int pid) {
        return postMapper.isExist(pid) > 0;
    }
}
