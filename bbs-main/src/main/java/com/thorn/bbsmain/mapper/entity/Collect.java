package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Collect implements Serializable {
    int uid;

    int pid;

    String title;

    Date time;
}
