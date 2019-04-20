package com.thorn.bbsmain.mapper.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.Date;

@Data
public class Post implements Serializable {

    Integer uid;

    Integer pid;

    @Length(min = 5, max = 50, message = "标题长度为5-50")
    String title;

    String nickname;

    @Range(min = 0, max = 4, message = "帖子分类错误")
    Integer type;

    Integer grade;

    Date postTime;

    Integer replyNum;

    Date lastReplyTime;

    Integer views;

    Integer collectionNum;

    Integer points;

    Boolean available;

    String img;

    Long hotPoint;
}
