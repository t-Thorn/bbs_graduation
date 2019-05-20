package com.thorn.bbsmain.mapper;

import com.thorn.bbsmain.mapper.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MessageMapper {

    @Update("update message set ischeck=true where id=#{id}")
    void checkMessage(int id);

    @Insert("insert into message (owner, type, fromUser, pid, floor, postTitle, content)" +
            " VALUES (#{owner},#{type},#{fromUser},#{pid},#{floor},#{postTitle},#{content})")
    void addMessage(Message message);

    @Select("select tmp.*, (case when type != 2 then nickname else '广播消息' end) ownerNickname" +
            " from (select (case when type != 2 then senderNickname else '系统' end) " +
            "senderNickname," +
            "             m.isCheck," +
            "             m.floor," +
            "             m.type," +
            "             m.pid," +
            "             m.id," +
            "             m.content," +
            "             m.postTitle," +
            "             m.createTime," +
            "             m.owner,m.fromUser" +
            "      from message m" +
            "             left join (select nickname senderNickname, uid" +
            "                        from user) u on m.fromUser = u.uid) tmp" +
            "       left join (select nickname, uid from user) owner on tmp.owner = owner.uid" +
            " order by id desc limit #{offset},#{step}")
    List<Message> getMessages(int offset, int step);

    @Select("select count(*) from message")
    int getMessageNum();

    @Select("select content from message where id=#{id}")
    String getMessageDetail(int id);
}
