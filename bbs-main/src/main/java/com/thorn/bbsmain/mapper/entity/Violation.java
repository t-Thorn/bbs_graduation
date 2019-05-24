package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Violation {
    int id;
    int uid;
    int pid;
    int floor;
    String word;
    Date time;
    int num;
    String title;
    //回复的id(用于浏览帖子时的定位)
    int replyID;
    //回复所在的页数(用于浏览帖子时的定位)
    int page;
}
