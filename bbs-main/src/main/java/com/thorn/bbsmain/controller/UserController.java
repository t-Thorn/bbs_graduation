package com.thorn.bbsmain.controller;


import com.thorn.bbsmain.exceptions.UserException;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(tags = "用户模块")
@Slf4j
@RequestMapping("user")
@Controller
public class UserController {


    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @RequiresGuest
    @GetMapping(value = {"login/{uri}", "login"})
    public ModelAndView turnToLogin(@Autowired MsgBuilder builder,
                                    @PathVariable(value = "uri", required = false) String uri) {
        builder.addData("uri", uri);
        return builder.getMsg("user/login");
    }

    @RequiresGuest
    @PostMapping("login")
    public ModelAndView login(@Valid User user, BindingResult result,
                              String uri,
                              HttpServletResponse response, @Autowired MsgBuilder builder) {
        if (result.hasErrors()) {
            userService.getErrors(result, builder);
            return builder.getMsg("user/login");
        }
        return userService.userLogin(user, uri, response, builder);
    }


    @RequiresGuest
    @GetMapping(value = {"reg/{uri}", "reg"})
    public String turnToReg(@PathVariable(value = "uri", required = false) String uri,
                            MsgBuilder builder) {
        builder.addData("uri", uri);
        return "user/reg";
    }

    @RequiresGuest
    @PostMapping("reg")
    public ModelAndView reg(@Valid User user, BindingResult result, String uri, @RequestParam String repass,
                            HttpServletResponse response) throws UserException {
        return userService.userReg(user, result, uri, repass, response);
    }


    @RequiresGuest
    @GetMapping("forget")
    public String turnToForget() {

        return "user/forget";
    }

    @RequiresGuest
    @PostMapping("forget")
    public String forget() {

        return "user/forget";
    }

    @RequiresUser
    @GetMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        //单点登录需要在redis中缓存最新ip
        userService.logout(response);
        return "redirect:/";
    }

    @RequiresUser
    @GetMapping("home")
    public ModelAndView buildUserHome() throws Exception {
        return userService.buildUserHome(null, 1, 1);
    }

    /**
     * 构建用户主页
     *
     * @param uid   用户ID
     * @param page  用户帖子页码
     * @param rpage 用户回复页码
     * @return 主页页面
     * @throws Exception 数据库操作错误+各类错误
     */
    @GetMapping(value = {"home/{uid}/{page}/{rpage}", "home/{uid}"})
    public ModelAndView buildUserHome(@PathVariable(value = "uid", required = false) Integer uid,
                                      @PathVariable(value = "page", required = false) Integer page,
                                      @PathVariable(value = "rpage", required = false) Integer rpage) throws Exception {
        return userService.buildUserHome(uid, page == null ? 1 : page, rpage == null ? 1 : rpage);
    }

    @RequiresUser
    @PostMapping("fan")
    @ResponseBody
    public String createRelationship(@RequestParam("toUser") Integer to, @Autowired MsgBuilder builder) {
        userService.createRelationship(to);
        builder.addData("msg", "成功");
        return builder.getMsg();
    }

    @RequiresUser
    @DeleteMapping("cancelFan")
    @ResponseBody
    public String delRelationship(@RequestParam("toUser") Integer to, @Autowired MsgBuilder builder) {
        userService.delRelationship(to);
        builder.addData("msg", "成功");
        return builder.getMsg();
    }
}
