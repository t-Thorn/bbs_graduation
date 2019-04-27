package com.thorn.bbsmain.controller;

import com.thorn.bbsmain.services.AdminService;
import com.thorn.bbsmain.utils.MsgBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@RequiresRoles("admin")
@RequestMapping("admin")
public class AdminController {

    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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

    @ResponseBody
    @GetMapping("postTable")
    public String postOAView(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "limit", required = false) int step) {
        return adminService.getPostTable(page, search, step);
    }
}
