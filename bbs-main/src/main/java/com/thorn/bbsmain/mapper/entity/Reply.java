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

    int id;

    String contentEx;

    String content_show;

    @Length(min = 1, max = 65535, message = "内容太长")
    String content;

    @Range(min = 1, max = Integer.MAX_VALUE, message = "参数错误")
    Integer floor;

    Integer replyer;

    Date replyTime;

    //楼层
    @Range(min = 1, max = Integer.MAX_VALUE, message = "参数错误")
    Integer replyTo;

    Integer likesNum;

    Boolean available;

    //层主用户id
    Integer replyToId;

    String replyToNickname;

    String img;

    String nickname;

    boolean zan;
}
