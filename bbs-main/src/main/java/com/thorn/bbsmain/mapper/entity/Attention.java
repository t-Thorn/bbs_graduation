package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Attention {
    String toUserNickname;
    int fromUser;
    int toUser;
    Date createTime;
}
