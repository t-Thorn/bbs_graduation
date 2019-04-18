package com.thorn.bbsmain.mapper.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

@Data
public class Reply {
    @Range(min = 1, max = Integer.MAX_VALUE, message = "参数错误")
    Integer postid;

    String title;


    String contentEx;

    String content_show;

    @Length(min = 1, max = 65535, message = "内容太长")
    String content;

    @Range(min = 1, max = Integer.MAX_VALUE, message = "参数错误")
    Integer floor;

    Integer replyer;

    Date replyTime;

    @Range(min = 1, max = Integer.MAX_VALUE, message = "参数错误")
    Integer replyTo;

    Integer likesNum;

    Boolean avaliable;

    Integer replyToId;

    String replyToNickname;

    String img;

    String nickname;

    boolean zan;
}
