package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.Reply;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ReplyMapper {
    @Insert("insert into reply (postid,content,content_show,replyer) values(#{postid},#{content}," +
            "#{content_show},#{replyer})")
    void createPostTopReply(Reply reply);

    @Select("select nickname, img, r.*" +
            "from (select r1.*, contentEx, replyToId,replyToNickname" +
            "      from reply r1" +
            "             left join (select (case" +
            "                               when avaliable=1 then content_show" +
            "                                 else '回复已被删除'" +
            "                                   end ) contentEx," +
            "                               floor," +
            "                               replyer      replyToId," +
            "                               nickname     replyToNickname" +
            "                        from reply left join user on replyer=uid" +
            "                        where postid = 1) r2 on r1.replyTo = r2.floor" +
            "      where postid = #{pid}" +
            "        and avaliable = 1) r" +
            "       left join" +
            "     user u on uid = r.replyer " +
            "order by floor asc")
    List<Reply> getReplyByPID(int pid);


    @Select("select floor from replyLike where pid=#{pid} and liker=#{uid}")
    List<Integer> getLikesByPID(int pid, int uid);

    @Select("select count(1) from replyLike where pid=#{pid} and liker=#{uid} and floor=#{floor}")
    int exist(int pid, int uid, Integer floor);

    @Insert("insert into replyLike (pid,floor,liker,liketo) values(#{pid},#{floor},#{uid},#{to})")
    void zan(int pid, Integer uid, Integer floor, Integer to);

    @Delete("delete from replyLike where pid=#{pid} and liker=#{uid} and floor=#{floor}")
    void unzan(int pid, Integer uid, Integer floor);

    @Update("update reply set likesNum=likesNum+1 where postid=#{pid} and floor=#{floor}")
    void increaseLikesNum(int pid, Integer floor);

    @Update("update reply set likesNum=likesNum-1 where postid=#{pid} and floor=#{floor}")
    void decreaseLikesNum(int pid, Integer floor);

    @Select("select likesNum from reply where postid=#{pid} and floor=#{floor}")
    int getLikesNum(int pid, int floor);
}
