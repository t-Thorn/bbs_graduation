package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.Reply;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ReplyMapper {
    @Insert("insert into reply (postid,content,content_show,replyer) values(#{postid},#{content}," +
            "#{content_show},#{replyer})")
    void createPostTopReply(Reply reply);

    @Select("select nickname, img, r.*" +
            "from (select r1.*, contentEx, replyToId,replyToNickname" +
            "      from reply r1" +
            "             left join (select (case" +
            "                               when available=1 then content_show" +
            "                                 else '回复已被删除'" +
            "                                   end ) contentEx," +
            "                               floor," +
            "                               replyer      replyToId," +
            "                               nickname     replyToNickname" +
            "                        from reply left join user on replyer=uid" +
            "                        where postid = 1) r2 on r1.replyTo = r2.floor" +
            "      where postid = #{pid}" +
            "        and available = 1) r" +
            "       left join" +
            "     user u on uid = r.replyer and postid>=(select postid from post limit #{offset}," +
            "1) " +
            "order by floor asc limit #{step}")
    List<Reply> getReplyByPID(int pid, int offset, int step);


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


    @Select({"<script>",
            "select count(1) from reply",
            "where postid=#{postid} and available=true",
            "<if test='replyTo!=null and replyTo!=0'>",
            "AND floor = #{replyTo}",
            "</if>",
            "</script>"
    })
    int isLegal(Integer postid, Integer replyTo);

    @Options(useGeneratedKeys = true, keyProperty = "floor", keyColumn = "floor")
    @Insert("insert into reply (postid,content,content_show,replyer,replyTo) values(" +
            "#{postid},#{content}," +
            "#{content_show},#{replyer},#{replyTo})")
    void addReply(Reply reply);

    @Select("select count(1) from reply where postid=#{pid} and floor=#{floor} and available=1")
    int isExist(int pid, int floor);

    @Update("update reply set available=0 where postid=#{pid} and floor=#{floor} and available=1")
    int invalidReply(int pid, int floor);

    @Select("select (select count(1)" +
            "        from reply " +
            "        where replyer = #{uid}" +
            "          and postid = #{pid}" +
            "          and floor = #{floor}" +
            "       ) + (" +
            "         select count(1)" +
            "         from post " +
            "         where uid = #{uid}" +
            "           and pid = #{pid}) " +
            "from dual")
    int hasPermission(Integer uid, int pid, int floor);

    @Select("select count(*) from reply where postid=#{pid} and available=true")
    int getReplyNum(Integer pid);
}
