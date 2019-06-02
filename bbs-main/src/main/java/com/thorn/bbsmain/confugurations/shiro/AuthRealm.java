package com.thorn.bbsmain.confugurations.shiro;


import com.thorn.bbsmain.confugurations.shiro.jwt.JWTToken;
import com.thorn.bbsmain.mapper.PermissionMapper;
import com.thorn.bbsmain.mapper.RoleMapper;
import com.thorn.bbsmain.mapper.UserMapper;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Slf4j
public class AuthRealm extends AuthorizingRealm {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;


    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * @param auth 用户登录的token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String email = JWTUtil.getEmail(token);
        if (email == null) {
            throw new AuthenticationException("token invalid");
        }

        User userBean = userMapper.getUserByUserName(email);
        if (userBean == null) {
            throw new AuthenticationException("用户不存在或者被禁用");
        }
        if (JWTUtil.isTokenExpired(token)) {
            throw new AuthenticationException("用户信息已经过期");
        }
        if (!JWTUtil.verify(token, email, userBean.getPassword())) {
            throw new AuthenticationException("密码错误");
        }
        return new SimpleAuthenticationInfo(userBean, token, "authrealm");
    }

    /**
     * 权限信息认证是用户访问controller的时候才进行验证(redis存储的也是权限信息)
     *
     * @param principals 身份信息
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String email = principals.oneByType(User.class).getEmail();
        if (email == null) {
            throw new AuthorizationException("用户信息保存错误,请尝试重新登录");
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(roleMapper.getRoleByUserName(email));
        Set<String> permission = permissionMapper.getPermissionNameByUserName(email);
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    /**
     * 清除当前用户的权限认证缓存
     *
     * @param principals 权限信息
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }
}
