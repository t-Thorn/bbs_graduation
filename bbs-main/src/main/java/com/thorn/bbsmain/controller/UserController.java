package com.thorn.bbsmain.controller;


import com.thorn.bbsmain.exceptions.UserException;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
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

@Slf4j
@RequestMapping("user")
@Controller
public class UserController {


    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @RequiresGuest
    @GetMapping("login")
    public ModelAndView turnToLogin(HttpServletRequest request, @Autowired MsgBuilder builder) {
        System.out.println("login");
        builder.addData("uri", request.getRequestURI());
        return builder.getMsg("/user/login");
    }

    @RequiresGuest
    @PostMapping("login")
    public ModelAndView login(@Valid User user, BindingResult result,
                              @RequestParam(value = "uri") String uri,
                              HttpServletResponse response, @Autowired MsgBuilder builder) {
        if (result.hasErrors()) {
            userService.getErrors(result, builder);
            return builder.getMsg("/user/login");
        }
        userService.userLogin(user, response, builder);
        return builder.getMsg("redirect:" + uri);
    }


    @RequiresGuest
    @GetMapping("reg")
    public String turnToReg() {
        System.out.println("reg");
        return "/user/reg";
    }

    @RequiresGuest
    @PostMapping("reg")
    public ModelAndView reg(@Valid User user, BindingResult result, @RequestParam String repass,
                            HttpServletResponse response) throws UserException {
        return userService.userReg(user, result, repass, response);
    }


    @RequiresGuest
    @GetMapping("forget")
    public String turnToForget() {
        System.out.println("reg");
        return "/user/forget";
    }

    @RequiresGuest
    @PostMapping("forget")
    public String forget() {
        System.out.println("reg");
        return "/user/forget";
    }

    @RequiresUser
    @GetMapping("logout")
    public String logout(HttpServletResponse response) {
        //单点登录需要在redis中缓存最新ip
        userService.logout(response);
        return "redirect:/";
    }

    @RequiresUser
    @GetMapping("home")
    public ModelAndView buildUserHome() throws Exception {
        return userService.buildUserHome(null);
    }

    @GetMapping("home/{uid}")
    public ModelAndView buildUserHome(@PathVariable("uid") Integer uid) throws Exception {
        return userService.buildUserHome(uid);
    }

    @RequiresUser
    @PostMapping("fan")
    @ResponseBody
    public String createRelationship(@RequestParam("toUser") Integer to, @Autowired MsgBuilder builder) throws Exception {
        userService.createRelationship(to);
        builder.addData("msg", "成功");
        return builder.getMsg();
    }

    @RequiresUser
    @DeleteMapping("cancelFan")
    @ResponseBody
    public String delRelationship(@RequestParam("toUser") Integer to, @Autowired MsgBuilder builder) throws Exception {
        userService.delRelationship(to);
        builder.addData("msg", "成功");
        return builder.getMsg();
    }
}
