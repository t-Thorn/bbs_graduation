package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Reply {
    int postid;

    String title;

    String contentEx;

    String content_show;

    String content;

    int floor;

    int replyer;

    Date replyTime;

    int replyto;

    int likesNum;

    boolean avaliable;

    int replyToId;

    String replyToNickname;
}
