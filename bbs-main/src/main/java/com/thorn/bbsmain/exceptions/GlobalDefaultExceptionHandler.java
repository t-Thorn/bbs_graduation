package com.thorn.bbsmain.exceptions;


import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * Shiro异常处理类,(@ControllerAdvice用来处理异常)
 */
@Slf4j
@ControllerAdvice
public class GlobalDefaultExceptionHandler {

    @Autowired
    MsgBuilder builder;
    /**
     * 禁止用户访问的uri
     */
    @Value("#{'${shiro.noUser}'.split(',')}")
    List<String> uris;

    @Autowired
    UserService userService;

    /**
     * 认证过程有异常时触发
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public String defaultAuthorizedExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        return defaultException(request, response, e);
    }

    /**
     * 权限有问题时触发
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(AuthorizationException.class)
    @ResponseBody
    public String defaultunAuthorizedExceptionHandler(HttpServletRequest request,
                                                      HttpServletResponse response, Exception e) {
        return defaultException(request, response, e);
    }


    private String defaultException(HttpServletRequest request, HttpServletResponse response,
                                    Exception msg) {

        System.out.println("认证错误:" + msg.getMessage());
        MsgBuilder builder = new MsgBuilder();
        builder.addData("errorMsg", msg.getMessage());
//fixme  错误信息优化
        if (msg instanceof AuthorizationException) {
            //用户访问只有游客才能访问的页面时则返回主页面，并且不提示
            if (uris.stream().anyMatch(uri -> request.getRequestURI().contains(uri))) {
                System.out.println("游客界面");
                builder.clear();
            } else {
                builder.addData("errorMsg", "权限不足");
            }
            //权限不够则跳转到主页
            if (!isAjax(request)) {
                if (msg.getMessage().contains("user")) {
                    System.out.println("需要的是用户，而不是缺乏权限时跳转登录");
                    builder.redirectAndSendMsgByJump(request, response, "/user/login");
                }
                builder.redirectAndSendMsgByJump(request, response, "/");
            } else {
                return builder.getMsg();
            }
        } else {
            System.out.println("身份认证错误");
            //返回请求页面，并附上参数
            //用户访问只有游客才能访问的页面时则返回主页面，并且不提示
            if (!isAjax(request)) {
                userService.delUserCookie(response);
                builder.redirectAndSendMsgByJump(request, response,
                        request.getRequestURI());
            } else {
                return builder.getMsg();
            }
        }

        //返回空，否则会造成二次使用response
        return null;
    }


    public boolean isAjax(ServletRequest request) {
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @ExceptionHandler(UserInfoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String userInfoExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                           Throwable ex) {
        return defualtHandler(request, response, ex);
    }

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String userExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                       Throwable ex) {

        return defualtHandler(request, response, ex);
    }

    @ExceptionHandler(PageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String pageExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                       Throwable ex) {

        return defualtHandler(request, response, ex);
    }

    @ExceptionHandler(PostException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String postExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                       Throwable ex) {

        return defualtHandler(request, response, ex);
    }

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String postNotFoundExceptionHandler(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Throwable ex) {
        System.out.println("postnotfound");
        request.setAttribute("errorMsg", ex.getMessage());
        return "/other/404";
    }

    /**
     * 捕捉其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void globalException(HttpServletRequest request, HttpServletResponse response,
                                Throwable ex) {
        if ("/error_404".equals(request.getRequestURI())) {
            //防止过多重定向
            return;
        }
        log.error("默认异常处理:" + ex.getMessage());
        builder.addData("errorMsg", "服务器内部错误");

        builder.redirectAndSendMsg(response, "/error_404", "errorMsg");
    }


    private String defualtHandler(HttpServletRequest request, HttpServletResponse response, Throwable ex) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("errorMsg", ex.getMessage());
        log.warn("出现异常：{}" + ex.getMessage());
        if (isAjax(request)) {
            return builder.getMsg();
        } else {
            //
            builder.redirectAndSendMsg(response, request.getRequestURI(), "errorMsg");
        }
        return null;
    }
}