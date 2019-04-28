package com.thorn.bbsmain.controller;

import com.thorn.bbsmain.services.AdminService;
import com.thorn.bbsmain.services.oa.PostOAService;
import com.thorn.bbsmain.utils.MsgBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@RequiresRoles("admin")
@RequestMapping("admin")
public class AdminController {

    private AdminService adminService;

    private PostOAService postOAService;

    public AdminController(AdminService adminService, PostOAService postOAService) {
        this.adminService = adminService;
        this.postOAService = postOAService;
    }

    @GetMapping("OA")
    public ModelAndView OAView() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("/oa/index");
    }

    @GetMapping("post")
    public ModelAndView postView() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("/oa/post");
    }

    @GetMapping("editPost/{pid}")
    public ModelAndView editPost(@PathVariable("pid") int pid) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("post", postOAService.getPostInfo(pid));
        return builder.getMsg("/oa/editPost");
    }

    @ResponseBody
    @GetMapping("postTable")
    public String postOAView(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "limit", required = false) int step,
                             @RequestParam(value = "type", required = false, defaultValue = "1") int type) {
        return adminService.getPostTable(page, search, step, type);
    }
}
