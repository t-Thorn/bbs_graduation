package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.Message;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
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
            "where owner = #{uid} " +
            "order by pid " +
            "  desc ")
    List<Message> getMessages(int uid);

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
            " avaliable=1 order by pid desc limit 10")
    List<Post> getUserPost(int uid);

    @Select("  select nickname replyToNickname, replyDetail.*" +
            "  from (select title, r.*" +
            "        from (select r.postid," +
            "                     r.floor," +
            "                     r.content_show," +
            "                     rex.content_show contentEx," +
            "                     rex.replyer      replyToId" +
            "              from reply r" +
            "                     left join reply rex on" +
            "                  rex.postid = r.postid" +
            "                  and rex.floor = r.replyTo" +
            "              where r.replyer = #{uid}" +
            "                and r.avaliable = 1" +
            "             ) r" +
            "               left join post on postid = pid" +
            "        where avaliable = 1) replyDetail" +
            "         left join user on" +
            "    replyToId = uid;")
    List<Reply> getUserReply(int uid);

    @Update("update user set postNum=postNum+1 where uid=#{uid}")
    void addPostNum(int uid);
}
