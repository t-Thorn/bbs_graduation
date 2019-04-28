package com.thorn.bbsmain.controller;


import com.thorn.bbsmain.exceptions.DeleteReplyException;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.exceptions.PostNotFoundException;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.services.PostService;
import com.thorn.bbsmain.services.ReplyService;
import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("post")
@Controller
public class PostController {

    @Value("${system.path.replyimg}")
    private String REPLY_IMG_PATH;

    private PostService postService;

    private ReplyService replyService;


    public PostController(@Autowired PostService postService, @Autowired ReplyService replyService,
                          @Autowired UserService userService) {
        this.postService = postService;
        this.replyService = replyService;
    }

    /**
     * 跳转帖子详情页面
     *
     * @param pid      帖子id
     * @param ID        跳转楼层
     * @param replyID    跳转添加回复后的楼层，接收自转发来的
     * @param page     页数
     * @param errorMsg 错误信息
     * @return 帖子详情
     * @throws PostNotFoundException 未知帖子错误
     * @throws PageException         帖子内容错误
     */
    @RequestMapping(value = {"/{pid}/{page}", "/{pid}"})
    public ModelAndView viewPost(@PathVariable("pid") int pid,
                                 @RequestParam(value = "replyID", required = false) Integer ID,
                                 @RequestAttribute(value = "replyID", required = false) Integer replyID,
                                 @PathVariable(value = "page", required = false) Integer page,
                                 @RequestAttribute(value = "errorMsg", required = false) String errorMsg)
            throws PostNotFoundException, PageException {
        return postService.viewPost(pid, replyID == null ? ID : replyID, page == null ? 1 : page,
                errorMsg);
    }

    @RequiresPermissions("createPost")
    @GetMapping("newPost")
    public ModelAndView newPost(@Autowired MsgBuilder builder) {
        return builder.getMsg("/jie/add");
    }

    @RequiresPermissions("createPost")
    @PostMapping("createPost")
    public ModelAndView createPost(@Valid Post post, BindingResult result, Reply reply,
                                   HttpServletResponse response) {
        return postService.createPost(post, result, reply, response);
    }


    @RequiresPermissions("reply")
    @PostMapping("imgUpload")
    @ResponseBody
    public String imgUpload(@RequestParam("img") MultipartFile[] imgs) {
        return replyService.imgUpload(imgs);
    }

    @ResponseBody
    @DeleteMapping("del/{pid}")
    public String delReply(@PathVariable("pid") int pid) throws PostException, DeleteReplyException {
        return postService.delPost(pid);
    }
}
