package com.thorn.bbsmain.mapper.entity;

import lombok.Data;

import java.util.Date;

/**
 * 敏感词
 */
@Data
public class Sensitivity {
    int id;
    String word;
    Date time;
    boolean available;
}
