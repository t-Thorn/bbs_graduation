package com.thorn.bbsmain.mapper.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@Data
public class User implements Serializable {
    @Email(message = "请正确输入邮箱")
    String email;

    @Length(min = 3, max = 18, message = "密码长度在3-18之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,18}$", message = "密码只能包含字母数字下划线")
    String password;


    @Length(min = 5, max = 30, message = "昵称长度在5-30之间")
    String nickname;

    String img;


    Integer uid;

    @Range(min = 0, max = 500, message = "年龄需在0-500岁内")
    Integer age;


    String gender;

    Date regdate;

    Integer postNum;

    int grade;

    boolean available;

    public User(@NotNull(message = "用户名不能为空") @Email(message = "请正确输入邮箱") String email, @NotNull @Length(min = 3, max = 18, message = "密码长度在3-18之间") @Pattern(regexp = "^[a-zA-Z0-9_]{3,18}$", message = "密码只能包含字母数字下划线") String password, @NotNull @Length(min = 5, max = 30, message = "密码长度在5-30之间") String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
}
