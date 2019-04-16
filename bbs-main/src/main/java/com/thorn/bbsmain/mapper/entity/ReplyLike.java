package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ReplyLike {
    int pid;

    int floor;

    int liker;

    int likeTo;

    Date time;
}
