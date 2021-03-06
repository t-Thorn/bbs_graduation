package com.thorn.bbsmain.controller;

import com.thorn.bbsmain.exceptions.DeleteReplyException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.services.ReplyService;
import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@RequiresUser
@RequestMapping("reply")
@Controller
public class ReplyController {
    private ReplyService replyService;
    private UserService userService;

    public ReplyController(ReplyService replyService, UserService userService) {
        this.replyService = replyService;
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PostMapping("zan")
    public String zan(@RequestParam("pid") Integer pid, @RequestParam("floor") Integer floor,
                      @RequestParam("toUser") Integer to) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("no", replyService.zan(floor, pid,
                userService.getCurrentUser().getUid(), to));
        return builder.getMsg();
    }

    /**
     *
     * @param reply
     * @param result
     * @return
     * @throws PostException
     */
    @RequiresPermissions("reply")
    @PostMapping("addReply")
    public ModelAndView addReply(@Valid Reply reply, BindingResult result) throws PostException {
        return replyService.addReply(reply, result);
    }

    @ResponseBody
    @DeleteMapping("del/{pid}/{floor}")
    public String delReply(@PathVariable("pid") int pid,
                           @PathVariable("floor") int floor) throws PostException, DeleteReplyException {
        return replyService.delReply(pid, floor);
    }
}
