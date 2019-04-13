package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    int id;

    int owner;

    int type;

    int fromUser;

    int pid;

    int floor;

    String postTitle;

    String content;

    boolean isCheck;

    Date createTime;

    String nickname;
}
