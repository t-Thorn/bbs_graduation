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
public class Permission {

    private static final long serialVersionUID = 1L;


    private Integer id;

    /**
     * 权限名称
     */
    private String permissionName;

    private LocalDateTime createdatetime;

    private LocalDateTime modifydatetime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
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
        return "Permission{" +
                "id=" + id +
                ", permissionName=" + permissionName +
                ", createdatetime=" + createdatetime +
                ", modifydatetime=" + modifydatetime +
                "}";
    }
}
