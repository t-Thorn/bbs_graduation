package com.thorn.bbsmain.exceptions;


import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

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
    @ExceptionHandler({AuthenticationException.class, UnauthenticatedException.class})
    public ModelAndView defaultAuthorizedExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
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
    @ExceptionHandler({AuthorizationException.class, UnauthorizedException.class})
    public ModelAndView defaultunAuthorizedExceptionHandler(HttpServletRequest request,
                                                      HttpServletResponse response, Exception e) {
        return defaultException(request, response, e);
    }


    private ModelAndView defaultException(HttpServletRequest request, HttpServletResponse response,
                                          Exception msg) {

        String uri = MyUtil.getReferer(request);
        MsgBuilder builder = new MsgBuilder();
        builder.addData("errorMsg", msg.getMessage());
        if (msg instanceof AuthorizationException) {
            //用户访问只有游客才能访问的页面时则返回主页面，并且不提示
/*            if (uris.stream().anyMatch(uri -> request.getRequestURI().contains(uri))) {
                log.info("游客界面");
                builder.clear();
            } else {*/
                builder.addData("errorMsg", "权限不足");
            if (!isAjax(request)) {
                if (msg.getMessage().contains("The current Subject is not a user")) {
                    builder.addData("errorMsg", "请登录后再试");
                    builder.addData("uri", uri);
                    return builder.getMsg("user/login");
                }
                //权限不够则跳转到主页
                return builder.getMsg("redirect:" + uri);
            } else {
                return builder.getMsgForAjax();
            }
        } else {
            //msg.printStackTrace();
            log.info("身份认证错误");
            //返回请求页面，并附上参数
            //用户访问只有游客才能访问的页面时则返回主页面，并且不提示
            if (!isAjax(request)) {
                userService.delUserCookie(response);
                return builder.getMsg("redirect:" + ("".equals(uri) ? "/" : uri));
            } else {
                return builder.getMsgForAjax();
            }
        }
    }


    public boolean isAjax(ServletRequest request) {
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @ExceptionHandler(UserInfoException.class)
    public ModelAndView userInfoExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                                 Throwable ex) {
        return defaultHandler(request, response, ex);
    }

    @ExceptionHandler(UserException.class)
    public ModelAndView userExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                             Throwable ex) {

        return defaultHandler(request, response, ex);
    }

    @ExceptionHandler(PageException.class)
    public ModelAndView pageExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                             Throwable ex) {

        return defaultHandler(request, response, ex);
    }

    @ExceptionHandler(PostException.class)
    public ModelAndView postExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                             Throwable ex) {
        return defaultHandler(request, response, ex);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ModelAndView postNotFoundExceptionHandler(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Throwable ex) {
        return defaultHandler(request, response, ex);
    }

    @ResponseBody
    @ExceptionHandler(DeleteReplyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView delReplyExceptionHandler(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Throwable ex) {
        return defaultHandler(request, response, ex);
    }


    /**
     * 捕捉其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView globalException(HttpServletRequest request, HttpServletResponse response,
                                        Throwable ex) {
        MsgBuilder builder = new MsgBuilder();
        log.error("默认异常处理:{} {}", ex.getMessage(), ex.getStackTrace());
        builder.addData("errorMsg", "服务器内部错误");
        if (isAjax(request))
            return builder.getMsgForAjax();
        return builder.getMsg("other/404");
    }


    private ModelAndView defaultHandler(HttpServletRequest request, HttpServletResponse response,
                                        Throwable ex) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("errorMsg", ex.getMessage());
        log.warn("出现异常：{}", ex.getMessage());
        if (isAjax(request)) {
            return builder.getMsgForAjax();
        }
        return builder.getMsg("redirect:" + ("".equals(MyUtil.getReferer(request)) ? "/" :
                MyUtil.getReferer(request)));
    }
}