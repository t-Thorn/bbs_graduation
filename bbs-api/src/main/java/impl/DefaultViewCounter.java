package impl;

import interfaces.Fetcher;
import interfaces.ViewCounter;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class DefaultViewCounter implements ViewCounter {
    @Override
    public Object getID(int pid, Object object) {
        Object user = SecurityUtils.getSubject().getPrincipal();
        if (user != null) {
            return ((Fetcher) object).getUID(user) + ":" + pid;
        }
        //利用ip地址的负数作为id
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = ((ServletRequestAttributes) ra);
        HttpServletRequest request = sra.getRequest();
        return Math.negateExact(Integer.valueOf(getIpAddress((request)).replace(".",
                "").replace(":", ""))) + ":" + pid;
    }


    /**
     * 获取真实ip，可以避免代理
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
