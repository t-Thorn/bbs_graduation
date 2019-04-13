package com.thorn.bbsmain.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface PermissionMapper {

    /**
     * 获取权限
     *
     * @param email 账户邮箱
     * @return
     */
    @Select("select pname from permission,permission_role where rid=" +
            "(select rid from user_role where uid=(select uid from user where" +
            " email=#{email} limit 1) limit 1) and permission.pid=permission_role.pid")
    Set<String> getPermissionNameByUserName(String email);
}
