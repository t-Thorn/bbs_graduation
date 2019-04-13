package com.thorn.bbsmain.mapper.entity.permission;


import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author lxy
 * @since 2018-12-10
 */
public class RolePermission {

    private static final long serialVersionUID = 1L;


    private Integer id;

    /**
     * 角色id
     */
    private Integer roleId;

    /**
     * 权限id
     */
    private Integer permissionId;

    private LocalDateTime createdatetime;

    private LocalDateTime modifydatetime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    public LocalDateTime getCreatedatetime() {
        return createdatetime;
    }

    public void setCreatedatetime(LocalDateTime createdatetime) {
        this.createdatetime = createdatetime;
    }

    public LocalDateTime getModifydatetime() {
        return modifydatetime;
    }

    public void setModifydatetime(LocalDateTime modifydatetime) {
        this.modifydatetime = modifydatetime;
    }


    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + id +
                ", roleId=" + roleId +
                ", permissionId=" + permissionId +
                ", createdatetime=" + createdatetime +
                ", modifydatetime=" + modifydatetime +
                "}";
    }
}
