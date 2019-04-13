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
public class Role {

    private static final long serialVersionUID = 1L;


    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    private LocalDateTime createdatetime;

    private LocalDateTime modifydatetime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
        return "Role{" +
                "id=" + id +
                ", roleName=" + roleName +
                ", createdatetime=" + createdatetime +
                ", modifydatetime=" + modifydatetime +
                "}";
    }
}
