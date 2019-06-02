package com.thorn.bbsmain.controller;

import com.alibaba.fastjson.JSONObject;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.services.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Api(tags = "基本模块")
@Controller
public class BasicController {


    private PostService postService;

    /**
     * @param postService
     */

    public BasicController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 显示首页
     *
     * @param type   1=精贴，默认普通
     * @param page   第几页
     * @param target 搜索内容，为空则默认
     * @return
     */
    @ApiOperation(value = "主页显示", notes = "主页")
    @ApiImplicitParam(name = "type", value = "帖子类型", required = false,
            paramType = ".path", dataType = "int", defaultValue = "0")
    @RequestMapping(value = {"index/{type}/{page}/{search}", "index/{type}/{page}", "/"})
    public ModelAndView home(@PathVariable(value = "type", required = false) Integer type,
                             @PathVariable(value = "page", required = false) Integer page,
                             @PathVariable(value = "search", required = false) String search,
                             @RequestParam(value = "target", required = false, defaultValue = "") String target,
                             @RequestAttribute(value = "errorMsg", required = false) String errorMsg) throws PageException {
        return postService.buildHome(type == null ? 0 : type, page == null ? 1 : page, search == null ? target :
                search, errorMsg);
    }

    @RequestMapping("/error_404")
    public ModelAndView error404() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/other/404");
        return mv;
    }

    /**
     * @param uri    跳转到哪个页面
     * @param params 参数集合
     * @return
     */
    @Deprecated
    @RequestMapping("/Jump")
    public ModelAndView error(@RequestAttribute("uri") String uri,
                              @RequestAttribute("params") JSONObject params) {
        ModelAndView mv = new ModelAndView();
        mv.addAllObjects(params);
        mv.setViewName("redirect:" + uri);
        return mv;
    }

    @GetMapping("message")
    public String msg() {
        return "/message";
    }
}
