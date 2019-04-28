package com.thorn.bbsmain.controller;

import com.alibaba.fastjson.JSONObject;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.services.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BasicController {

    /**
     * 1.修复删除回复不减少热度
     * 2.完成OA搜索以及编辑删除
     * 3.帖子页面实时加入浏览量
     */
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
    @GetMapping(value = {"index/{type}/{page}/{search}", "index/{type}/{page}", "/"})
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


}
