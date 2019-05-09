package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.Reply;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ReplyMapper {
    @Insert("insert into reply (postid,content,content_show,replyer) values(#{postid},#{content}," +
            "#{content_show},#{replyer})")
    void createPostTopReply(Reply reply);

    @Select("SELECT" +
            "  r.*," +
            "  contentEx," +
            "  replyToId," +
            "  replyToNickname" +
            " FROM" +
            "  (" +
            "    SELECT" +
            "      r1.*,nickname,img" +
            "    FROM" +
            "      reply r1" +
            "     left join user on uid=replyer" +
            "    WHERE" +
            "        postid = #{pid}" +
            "      AND id >= ( SELECT id FROM reply WHERE postid = #{pid} AND available = 1 ORDER BY" +
            "     id" +
            "     LIMIT" +
            "        #{offset}, 1 )" +
            "      AND r1.available = 1" +
            "    LIMIT #{step}" +
            "  ) r" +
            "    LEFT JOIN (" +
            "    SELECT" +
            "      ( CASE WHEN reply.available = 1 THEN content_show ELSE '回复已被删除' END ) " +
            "contentEx," +
            "      floor," +
            "      replyer replyToId," +
            "      nickname replyToNickname" +
            "    FROM" +
            "      reply" +
            "        LEFT JOIN user ON replyer = uid" +
            "    WHERE" +
            "          postid=#{pid} and" +
            " floor in (" +
            "        select replyTo from (" +
            "        SELECT" +
            "          replyTo,id" +
            "        FROM" +
            "          reply" +
            "        WHERE" +
            "            postid = #{pid}" +
            "          AND id >= ( SELECT id FROM reply WHERE postid = #{pid} AND available = 1" +
            "          ORDER BY id  LIMIT" +
            "            #{offset}, 1 )" +
            "        LIMIT #{step}" +
            "      )subReply)" +
            "  ) r2 ON r.replyTo = r2.floor;")
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

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into reply (postid,content,content_show,replyer,replyTo,floor) " +
            "select #{postid},#{content},#{content_show},#{replyer},#{replyTo},floor from (select" +
            " (max(floor)+1)as floor from reply where postid=#{postid}) r")
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

    @Select("select content,content_show,replyer,floor,postid from reply where postid=#{pid} " +
            "limit 1")
    Reply getTopReply(Integer pid);

    @Select("select replyer from reply where floor=#{replyTo} and postid=#{pid}")
    int getReplyOwner(Integer replyTo, int pid);

    @Select("select floor from reply where id=#{id} and postid=#{postid}")
    Integer getFloorByID(int id, Integer postid);
}
