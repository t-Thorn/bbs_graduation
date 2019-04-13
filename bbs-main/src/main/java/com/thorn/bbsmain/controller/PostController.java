package com.thorn.bbsmain.controller;


import com.thorn.bbsmain.exceptions.PostException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("post")
@Controller
public class PostController {

    @Value("${system.path.replyimg}")
    private String REPLY_IMG_PATH;

    private PostService postService;

    private ReplyService replyService;

    private UserService userService;

    public PostController(@Autowired PostService postService, @Autowired ReplyService replyService,
                          @Autowired UserService userService) {
        this.postService = postService;
        this.replyService = replyService;
        this.userService = userService;
    }

    @RequestMapping("/{pid}")
    public ModelAndView viewPost(@PathVariable("pid") int pid, @RequestParam(value = "floor",
            required = false, defaultValue = "0") int floor, HttpServletRequest request) {
        return postService.viewPost(pid, floor);
    }

    @RequiresPermissions("createPost")
    @GetMapping("newPost")
    public ModelAndView newPost(@Autowired MsgBuilder builder) {

        return builder.getMsg("/jie/add");
    }

    @RequiresPermissions("createPost")
    @PostMapping("createPost")
    public ModelAndView createPost(@Valid Post post, BindingResult result, Reply reply,
                                   HttpServletResponse response) throws PostException {
        return postService.createPost(post, result, reply, response);
    }


    @RequiresPermissions("reply")
    @PostMapping("imgUpload")
    @ResponseBody
    public String imgUpload(@RequestParam("img") MultipartFile[] imgs) {
        return replyService.imgUpload(imgs);
    }
}
