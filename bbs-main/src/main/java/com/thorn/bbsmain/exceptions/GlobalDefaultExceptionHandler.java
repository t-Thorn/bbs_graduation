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

    public static final String GUEST_OPERATION = "guest-only";
    public static final String USER_OPERATION = "The current Subject is not a user";
    public static final String UNATHORIZATION_STRING = "Subject does not have permission";
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
    public ModelAndView defaultUnauthorizedExceptionHandler(HttpServletRequest request,
                                                            HttpServletResponse response, Exception e) {
        return defaultException(request, response, e);
    }


    private ModelAndView defaultException(HttpServletRequest request, HttpServletResponse response,
                                          Exception msg) {

        String uri = MyUtil.getReferer(request);
        MsgBuilder builder = new MsgBuilder();

        if (msg instanceof AuthorizationException) {
            //用户访问只有游客才能访问的页面时则返回主页面，并且不提示
            if (!isAjax(request)) {
                if (msg.getMessage().contains(GUEST_OPERATION)) {
                    return builder.getMsg("redirect:" + uri);
                } else if (msg.getMessage().contains(USER_OPERATION)) {
                    builder.addData("errorMsg", "请登录后再试");
                    builder.addData("uri", uri);
                    return builder.getMsg("user/login");
                }
                getUnauthorizationMsg(msg, builder);
                //权限不够
                return builder.getMsg("redirect:" + uri);
            } else {

                getUnauthorizationMsg(msg, builder);
                return builder.getMsgForAjax();
            }
        } else {
            builder.addData("errorMsg", msg.getMessage());
            //返回请求页面，并附上参数
            //用户访问只有游客才能访问的页面时则返回主页面，并且不提示
            if (!isAjax(request)) {
                userService.delUserCookie(response);
                return builder.getMsg("redirect:/user/login");
            } else {
                return builder.getMsgForAjax();
            }
        }
    }

    private void getUnauthorizationMsg(Exception msg, MsgBuilder builder) {
        String message = msg.getMessage();
        String errorMsg = "权限不足";
        if (message.contains(UNATHORIZATION_STRING)) {
            message = message.substring(message.indexOf("[") + 1, message.lastIndexOf("]"));
            if ("reply".equalsIgnoreCase(message) || "createPost".equalsIgnoreCase(message)) {
                errorMsg = "你已经被禁言";
            } else {
                errorMsg = "权限不足";
            }
        }
        builder.addData("errorMsg", errorMsg);
    }

    @ExceptionHandler({UserInfoException.class, PageException.class, UserException.class, PostException.class})
    public ModelAndView userInfoExceptionHandler(HttpServletRequest request, HttpServletResponse response,
                                                 Throwable ex) {
        return defaultHandler(request, response, ex);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ModelAndView postNotFoundExceptionHandler(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Throwable ex) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("errorMsg", ex.getMessage());
        //log.warn("出现异常：{}", ex.getMessage());
        return builder.getMsg("forward:/error_404");
    }

    @ResponseBody
    @ExceptionHandler(DeleteReplyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView delReplyExceptionHandler(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Throwable ex) {
        return defaultHandler(request, response, ex);
    }

    @ExceptionHandler(UserRegException.class)
    public ModelAndView userRegExceptionHandler(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Throwable ex) {
        MsgBuilder builder = new MsgBuilder();
        String errorMsg = ex.getMessage();
        String uri = errorMsg.substring(errorMsg.lastIndexOf(".") + 1);
        errorMsg = errorMsg.substring(0, errorMsg.lastIndexOf("."));
        builder.addData("errorMsg", errorMsg);
        builder.addData("uri", uri);
        //log.warn("出现异常：{}", ex.getMessage());
        return builder.getMsg("redirect:" + MyUtil.getReferer(request));
    }

    private ModelAndView defaultHandler(HttpServletRequest request, HttpServletResponse response,
                                        Throwable ex) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("errorMsg", ex.getMessage());
        //log.warn("出现异常：{}", ex.getMessage());
        if (isAjax(request)) {
            return builder.getMsgForAjax();
        }
        return builder.getMsg("redirect:" + MyUtil.getReferer(request));
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
        if (isAjax(request)) {
            return builder.getMsgForAjax();
        }
        return builder.getMsg("other/404");
    }

    private boolean isAjax(ServletRequest request) {
        String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(header)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

}