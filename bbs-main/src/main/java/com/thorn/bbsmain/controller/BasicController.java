package com.thorn.bbsmain.controller;

import com.alibaba.fastjson.JSONObject;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BasicController {


    private PostService postService;


    public BasicController(@Autowired PostService postService) {
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
    @GetMapping("/")
    public ModelAndView home(@RequestParam(value = "type", required = false, defaultValue = "0") Integer type,
                             @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                             @RequestParam(value = "target", required = false, defaultValue = "") String target)
            throws PageException {
        return postService.buildHome(type, page, target);
    }

    @GetMapping("/error_404")
    public ModelAndView error404() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/other/404");
        return mv;
    }

    //fixme 重定向都得带参数，则这个很多余

    /**
     * @param uri    跳转到哪个页面
     * @param params 参数集合
     * @return
     */
    @Deprecated
    @RequestMapping("/Jump")
    public ModelAndView error(@RequestAttribute("uri") String uri,
                              @RequestAttribute("params") JSONObject params) {
        System.out.println("中转中心" + params.toJSONString());
        ModelAndView mv = new ModelAndView();
        mv.addAllObjects(params);
        mv.setViewName("redirect:" + uri);
        return mv;
    }
}
