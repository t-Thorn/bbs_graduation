package com.thorn.bbsmain.confugurations.shiro.jwt;

import com.thorn.bbsmain.utils.MysqlJdbc;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
public class JWTFilter extends BasicHttpAuthenticationFilter {


    /**
     * 父类会在请求进入拦截器后调用该方法，返回true则继续，返回false则会调用onAccessDenied()。这里在不通过时，还调用了isPermissive()方法，我们后面解释。
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        //log.info("放行鉴定 uri：" + servletRequest.getRequestURI());
/*       if (this.isLoginRequest(request, response)) {
            return true;
        }*/
        boolean allowed = false;

        if (Objects.equals(getRequestToken(servletRequest),
                null)) {
            return super.isPermissive(mappedValue);
        }
        try {
            allowed = executeLogin(request, response);
        } catch (IllegalStateException e) { //not found any token
            log.info(e.getMessage());
        } catch (Exception e) {
            log.error("Error occurs when login", e);
        }
        //避免token过期导致的无法访问
        return allowed || super.isPermissive(mappedValue);
    }

    /**
     * 这里重写了父类的方法，使用我们自己定义的Token类，提交给shiro。这个方法返回null的话会直接抛出异常，进入isAccessAllowed（）的异常处理逻辑。
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        String jwtToken = getRequestToken((HttpServletRequest) (servletRequest));
        return new JWTToken(jwtToken);
    }

    private boolean isNotBlank(String jwtToken) {
        return jwtToken != null && !"".equals(jwtToken);
    }


    /**
     * 如果Shiro Login认证成功，会进入该方法，等同于用户名密码登录成功，我们这里还判断了是否要刷新Token
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        //log.info("认证成功");
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        String newToken = null;
        if (token instanceof JWTToken) {
            String email = JWTUtil.getEmail(token.getCredentials().toString());
            String password = MysqlJdbc.getPassword(email);
            boolean shouldRefresh = JWTUtil.isTokenNeedRefresh(token.getCredentials().toString());
            if (shouldRefresh) {
                newToken = JWTUtil.sign(email, password);
            }
            if (isNotBlank(newToken)) {
                {
                    Cookie ctoken = new Cookie("token", newToken);
                    ctoken.setPath("/");
                    httpResponse.addCookie(ctoken);
                    log.info("重新刷新shiro中的 token");
                    //需要重新登录不然会出错
                    Subject currentUser = SecurityUtils.getSubject();
                    currentUser.logout();
                    currentUser.login(new JWTToken(newToken));
                }

            }
        }
        return true;
    }


    /**
     * 如果调用shiro的login认证失败，会回调这个方法，这里我们什么都不做，因为逻辑放到了onAccessDenied（）中。
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        log.info("Validate token fail, token:{}, error:{}", token.getCredentials().toString(),
                e.getMessage());
        return false;
    }


    private String getRequestToken(HttpServletRequest httpRequest) {
        //从header中获取token
        String token = httpRequest.getHeader("token");
        //如果header中不存在token，则从参数中获取token
        if (Objects.equals(token, null)) {
            token = httpRequest.getParameter("token");
            if (Objects.equals(token, null)) {
                // 从 cookie 获取 token
                Cookie[] cookies = httpRequest.getCookies();
                if (null == cookies || cookies.length == 0) {
                    return null;
                }
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        return token;
    }
}
