package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.Reply;
import org.apache.ibatis.annotations.Insert;

public interface ReplyMapper {
    @Insert("insert into reply (postid,content,content_show,replyer) values(#{postid},#{content}," +
            "#{content_show},#{replyer})")
    void createPostTopReply(Reply reply);
}
