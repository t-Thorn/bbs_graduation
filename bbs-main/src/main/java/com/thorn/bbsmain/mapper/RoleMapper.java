package com.thorn.bbsmain.mapper;


import org.apache.ibatis.annotations.Select;

public interface RoleMapper {

    @Select("select rname from role,user_role where  uid=(select uid from user where " +
            "email=#{email} limit 1) and role.rid=user_role.rid limit 1")
    String getRoleByUserName(String email);
}
