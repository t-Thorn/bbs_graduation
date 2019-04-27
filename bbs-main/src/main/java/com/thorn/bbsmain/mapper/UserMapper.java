package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select(" select img,email,password,nickname,uid from user where email=#{email} " +
            "limit 1")
    User getUserByUserName(String email);

    @Insert({"insert into user (email,nickname,password) values(#{email},#{nickname},#{password})"})
    int createNewUser(User user);


    @ResultType(Integer.class)
    @Select("select count(*) from user where email=#{email}")
    Integer checkExistOfEmail(String email);

    /**
     * 昵称是否存在
     *
     * @param nickname
     * @return
     */
    @ResultType(Integer.class)
    @Select("select count(*) from user where nickname=#{nickname}")
    Integer checkExistOfNN(String nickname);

    /**
     * 给新用户授权
     *
     * @param email
     * @return
     */
    @Insert("insert into user_role values((select uid from user where email=#{email} limit 1),2)")
    int grantToNewUser(String email);

    @Select("select email,nickname,age,gender,img from user where email=#{email} limit 1")
    User getInfo(String email);

    @Update("update user set nickname=#{nickname},gender=#{gender},age=#{age} where email=#{email}")
    void updateBasicInfo(User user);

    @Update("update user set img=#{imgPath} where email=#{email}")
    void updateAvator(String email, String imgPath);

    @Update("update user set password=#{password} where email=#{email}")
    void updatePassword(String email, String password);

    @Select(" select message.*, user.nickname " +
            "from message " +
            "       left join user on uid = fromUser " +
            "where owner = #{uid} and id>=(select id from message where owner=#{uid} limit " +
            "#{offset},1) order by pid  desc limit #{step}")
    List<Message> getMessages(int uid, int offset, int step);

    @Select("select uid,nickname,regdate,gender from user where uid=#{uid} limit 1")
    User getUserInfoOfHome(@Param("uid") int uid);

    @ResultType(Integer.class)
    @Select("select count(*) from user where uid=#{uid}")
    Integer checkExistOfUid(int uid);

    @ResultType(Integer.class)
    @Select("select count(*) from attention where fromUser=#{from} and toUser=#{to}")
    boolean isFan(int from, int to);

    @Insert("insert into attention (fromUser,toUser) values(#{from},#{to})")
    void createRelationship(int from, int to);

    @Delete("delete  from attention where fromUser=#{from} and toUser=#{to}")
    void delRelationship(int from, int to);

    @Select("select pid,title,postTime,grade,replyNum,views from post where uid=#{uid} and " +
            " available=1 order by pid desc limit 10")
    List<Post> getUserPost(int uid);

    @Select(" select nickname replyToNickname, replyDetail.*" +
            " from (select title, r.*" +
            "      from (select r1.*," +
            "                   rex.content_show contentEx," +
            "                   rex.replyer      replyToId" +
            "            from (select postid," +
            "                         floor," +
            "                         content_show," +
            "                         replyTo" +
            "                  from reply" +
            "                  where replyer = 1" +
            "                    and available = 1" +
            "                  order by id desc" +
            "                  limit 5) r1" +
            "                   left join reply rex" +
            "                             on rex.postid = r1.postid and r1.replyTo = rex.floor) r" +
            "             left join post on postid = pid" +
            "      where available = 1) replyDetail" +
            "       left join user on replyToId = uid")
    List<Reply> getUserReply(int uid);

    @Update("update user set postNum=postNum+1 where uid=#{uid}")
    void addPostNum(int uid);


    @Select("select count(1) from message where owner=1")
    int getMessageNum(Integer uid);

    @Select("select pid, title, time" +
            " from history" +
            " where uid = #{uid}" +
            "  and id<=(select id from history where uid=#{uid} order by id desc limit #{offset}," +
            "1) order by id desc limit #{step} ")
    List<History> getHistories(Integer uid, int offset, int step);

    @Select("select count(*) from history where uid=#{uid}")
    int getHistoryNum(int uid);

    @Select("select count(*) from collection where uid=#{uid} and pid=#{pid}")
    int collectRelationshipIsExist(Integer uid, Integer pid);

    @Delete("delete from collection where uid=#{uid} and pid=#{pid}")
    void delCollect(Integer uid, Integer pid);

    @Insert("insert into collection (uid,pid) values(#{uid},#{pid})")
    void Collect(Integer uid, Integer pid);
}
