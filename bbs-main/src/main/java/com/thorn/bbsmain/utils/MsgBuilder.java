package com.thorn.bbsmain.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;

@Component
//范围为原型，每次获取都是新的实例，
@Scope("prototype")
@Slf4j
@NoArgsConstructor
public class MsgBuilder {

    private final JSONObject json = new JSONObject();

    private ResponseEntity entity;

    /**
     * 添加数据到消息中
     *
     * @param key   键
     * @param value 值
     */
    public void addData(String key, Object value) {
        json.put(key, value);
    }

    public void addDatas(Object object) {
        Class clazz = object.getClass();
        //获得属性
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(),
                        clazz);
                //获得get方法
                Method getMethod = pd.getReadMethod();
                //执行get方法返回一个Object
                Object o = getMethod.invoke(object);
                addData(field.getName(), o);
            }
        } catch (Exception e) {
            log.error("获取obj失败：{}" + e.getMessage());
            return;
        }
    }

    public void addDatas(Map<String, Object> map) {
        json.putAll(map);
    }

    /**
     * 获取构建好的消息,适用与spring的ajax
     *
     * @return
     */
    public String getMsg() {
        return this.json.toJSONString();
    }


    /**
     * 获取构建好的消息,适用与web
     *
     * @return
     */
    public ModelAndView getMsg(String view) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName(view);
        mv.addAllObjects(this.json);
        return mv;
    }

    /**
     * 获取构建好的消息,适用与web,可以选择是否加工（拆解）
     *
     * @return
     */
    public ModelAndView getMsg(String view, boolean process) {
        if (process) {
            return getMsg(view);
        }
        ModelAndView mv = new ModelAndView();
        mv.setViewName(view);
        mv.addObject("params", this.json);
        return mv;
    }


    public void addCookie(HttpServletResponse response, String key, String value, String scope) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath(scope);
        response.addCookie(cookie);
    }

    public void addCookie(HttpServletResponse response, String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 重定向到指定页面，通过控制器返回参数,控制器用uri接收uri ，params接收其他参数
     *
     * @param request
     * @param response
     * @param uri      跳转页面
     */
    public void redirectAndSendMsgByJump(HttpServletRequest request, HttpServletResponse response,
                                         String uri) {
        try {
            request.setAttribute("uri", uri);
            System.out.println(json.toJSONString());
            request.setAttribute("params", json);
            request.getRequestDispatcher("/Jump").forward(request, response);
        } catch (ServletException | IOException e) {
            log.error("转发失败：" + e.getMessage());
        }
    }

    /**
     * 重定向并缓存消息，参数名不易过长 使用的不是session传值，而是url传值
     *
     * @param response
     * @param uri
     * @param paramName 需要传递的参数的名称
     */

    public void redirectAndSendMsg(HttpServletResponse response,
                                   String uri, String paramName) {

        try {
            response.sendRedirect(uri + "?" + paramName + "="
                    + URLEncoder.encode(this.json.getString(paramName), "utf-8"));
        } catch (IOException e) {
            log.error("重定向失败失败：" + e.getMessage());
        }
    }

    /**
     * 返回当前的数据
     *
     * @return
     */
    public JSONObject getData() {
        return json;
    }

    public void delData(String key) {
        json.remove(key);
    }

    public void clear() {
        json.clear();
    }
}
