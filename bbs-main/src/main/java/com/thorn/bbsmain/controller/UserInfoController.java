package com.thorn.bbsmain.controller;


import com.thorn.bbsmain.exceptions.UserInfoException;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.services.InfoService;
import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RequiresUser
@RequestMapping("info")
@Controller
public class UserInfoController {


    private UserService userService;

    private InfoService infoService;

    public UserInfoController(@Autowired UserService userService, @Autowired InfoService infoService) {
        this.userService = userService;
        this.infoService = infoService;
    }

    @GetMapping("setting")
    public ModelAndView turoToSetting() {
        MsgBuilder msgBuilder = new MsgBuilder();

        User user = userService.getCurrentUser();

        msgBuilder.addDatas(infoService.getInfo(user.getEmail()));
        return msgBuilder.getMsg("/user/set");
    }


    @PatchMapping("basic")
    public ModelAndView modifyBasicInfo(@Valid User user, BindingResult bindingResult) {
        return infoService.modifyBasicInfo(user, bindingResult);
    }


    @PatchMapping("img")
    @ResponseBody
    public String changeAvator(@RequestParam("file") MultipartFile img,
                               HttpServletResponse response) throws UserInfoException {
        return infoService.updataAvator(img, response);
    }

    @PatchMapping("password")
    public ModelAndView updatePassword(@RequestParam("nowpass") String nowpass,
                                       @Valid User user, BindingResult result, @RequestParam("repass") String repass
            , HttpServletResponse response) throws UserInfoException {
        return infoService.updateUserPassword(nowpass, user, result, repass, response);
    }

    @GetMapping("myPost")
    public ModelAndView getMyPost(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "cpage", required = false, defaultValue = "1") Integer cpage) throws Exception {
        return infoService.getMyPosts(page, cpage);
    }

    @GetMapping("myCollection")
    public ModelAndView getMyCollection(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "cpage", required = false, defaultValue = "1") Integer cpage) {
        MsgBuilder builder = new MsgBuilder();
        //加入锚点
        builder.addData("loc", "#collection");
        builder.addData("cpage", cpage);
        builder.addData("page", page);
        return builder.getMsg("forward:myPost");
    }

    @GetMapping("message")
    public ModelAndView getMessage(@RequestParam(value = "page", defaultValue = "1") Integer page) throws Exception {
        return infoService.getMyMessages(page);
    }
}
