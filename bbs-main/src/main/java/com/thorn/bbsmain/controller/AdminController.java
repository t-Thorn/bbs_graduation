package com.thorn.bbsmain.controller;

import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.exceptions.SqlException;
import com.thorn.bbsmain.exceptions.UserInfoException;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.services.AdminService;
import com.thorn.bbsmain.services.oa.PostOAService;
import com.thorn.bbsmain.services.oa.UserOAService;
import com.thorn.bbsmain.services.oa.ViolationService;
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

    private UserOAService userOAService;

    private ViolationService violationService;

    public AdminController(AdminService adminService, PostOAService postOAService,
                           UserOAService userOAService, ViolationService violationService) {
        this.adminService = adminService;
        this.postOAService = postOAService;
        this.userOAService = userOAService;
        this.violationService = violationService;
    }

    @GetMapping("OA")
    public ModelAndView OAView() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/index");
    }

    @GetMapping("post")
    public ModelAndView postView() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/post");
    }

    @GetMapping("editPost/{pid}")
    public ModelAndView jumpToEditPost(@PathVariable("pid") int pid) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("post", postOAService.getPostInfo(pid));
        return builder.getMsg("oa/editPost");
    }


    @ResponseBody
    @PutMapping("editPost")
    public String editPost(Post post) throws PostException {
        postOAService.update(post);
        return "";
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

    @GetMapping("user")
    public ModelAndView userView() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/user");
    }

    @ResponseBody
    @GetMapping("userTable")
    public String userOAView(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "limit", required = false) int step,
                             @RequestParam(value = "type", required = false, defaultValue = "1") int type) {
        return adminService.getUserTable(page, search, step, type);
    }

    @GetMapping("editUser/{uid}")
    public ModelAndView jumpToEditUser(@PathVariable("uid") int uid) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("user", userOAService.getUserInfo(uid));
        return builder.getMsg("oa/editUser");
    }


    @ResponseBody
    @PutMapping("editUser")
    public String editUser(User user, boolean gender_) throws UserInfoException {
        user.setGender(gender_ ? "男" : "女");
        userOAService.update(user);
        return "";
    }

    @GetMapping("message")
    public ModelAndView systemMessage() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/systemMessage");
    }

    @ResponseBody
    @GetMapping("messageTable")
    public String messages(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                           @RequestParam(value = "limit", required = false) int step) {
        return adminService.getMessageTable(page, step);
    }

    @ResponseBody
    @GetMapping("messageDetail/{id}")
    public String messageDetail(@PathVariable(value = "id") int id) {
        return adminService.getMessageDetail(id);
    }

    @ResponseBody
    @PostMapping("broadcast")
    public String broadcast(@RequestParam(value = "msg") String msg) {
        return adminService.broadcast(msg);
    }

    @GetMapping("sensitivity")
    public ModelAndView sensitivity() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/sensitivity");
    }

    @ResponseBody
    @GetMapping("sensitivityTable")
    public String sensitivities(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                                @RequestParam(value = "limit", required = false) int step,
                                @RequestParam(value = "search", required = false) String search) {
        return adminService.getSensitivities(page, step, search);
    }

    @ResponseBody
    @PatchMapping("sensitivity/set/{id}")
    public String setSensitivityStatus(@PathVariable("id") int id,
                                       @RequestParam("available") boolean available) throws SqlException {
        return violationService.setSensitivityStatus(id, available);
    }

    @ResponseBody
    @PostMapping("sensitivity/add")
    public String addSensitivity(@RequestParam("word") String word) {
        return violationService.addSensitivityStatus(word);
    }

    @GetMapping("violation")
    public ModelAndView violation() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/violation");
    }

    /**
     * 这里是违规统计
     *
     * @param page
     * @param step
     * @param search
     * @return
     */
    @ResponseBody
    @GetMapping("violationTable")
    public String violations(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                             @RequestParam(value = "limit", required = false) int step,
                             @RequestParam(value = "search", required = false) String search) {
        return adminService.getViolation(page, step, search);
    }

    @GetMapping("violationRecord")
    public ModelAndView violationRecord() {
        MsgBuilder builder = new MsgBuilder();
        return builder.getMsg("oa/violationRecord");
    }

    /**
     * 这里是违规记录
     *
     * @param page
     * @param step
     * @param search
     * @return
     */
    @ResponseBody
    @GetMapping("violationRecordTable")
    public String violationRecords(@RequestParam(value = "page", required = false,
            defaultValue = "1") int page,
                                   @RequestParam(value = "limit", required = false) int step,
                                   @RequestParam(value = "search", required = false) String search) {
        return adminService.getViolationRecord(page, step, search);
    }
}
