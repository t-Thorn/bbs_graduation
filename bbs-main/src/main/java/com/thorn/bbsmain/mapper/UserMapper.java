package com.thorn.bbsmain.mapper;


import com.thorn.bbsmain.mapper.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select img,email,password,nickname,uid from user where email=#{email} and " +
            " available=1 limit 1")
    User getUserByUserName(String email);

    @Insert({"insert into user (email,nickname,password) values(#{email},#{nickname},#{password})"})
    void createNewUser(User user);


    @ResultType(Integer.class)
    @Select("select count(*) from user where email=#{email}")
    Integer checkExistOfEmail(String email);

    @ResultType(Integer.class)
    @Select("select count(*) from user where email=#{email} and available=true")
    Integer checkValidOfEmail(String email);

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
    void grantToNewUser(String email);

    @Select("select email,nickname,age,gender,img from user where email=#{email} limit 1")
    User getInfo(String email);

    @Select(" select user.*,rid grade from user  left join user_role" +
            " on user_role.uid = user.uid where  user.uid=#{uid} limit 1")
    User getInfoByID(int uid);

    @Update("update user set nickname=#{nickname},gender=#{gender},age=#{age} where email=#{email}")
    void updateBasicInfo(User user);

    @Update("update user set img=#{imgPath} where email=#{email}")
    void updateAvator(String email, String imgPath);

    @Update("update user set password=#{password} where email=#{email}")
    void updatePassword(String email, String password);

    @Select(" select message.*, user.nickname " +
            "from message " +
            "       left join user on uid = fromUser " +
            "where (owner = #{uid} or owner=-1) " +
            " order by id  desc limit #{offset},#{step}")
    List<Message> getMessages(int uid, int offset, int step);

    @Select("select uid,nickname,regdate,gender from user where uid=#{uid} and available=true limit 1")
    User getUserInfoOfHome(@Param("uid") int uid);

    @ResultType(Integer.class)
    @Select("select count(*) from user where uid=#{uid} and available=true")
    Integer checkExistOfUid(int uid);

    @ResultType(Integer.class)
    @Select("select count(*) from attention where fromUser=#{from} and toUser=#{to}")
    boolean isFan(int from, int to);

    @Insert("insert into attention (fromUser,toUser) values(#{from},#{to})")
    void createRelationship(int from, int to);

    @Delete("delete  from attention where fromUser=#{from} and toUser=#{to}")
    void delRelationship(int from, int to);

    @Select("select pid,title,postTime,grade,replyNum,views from post where uid=#{uid} and " +
            " available=1 order by pid desc limit #{offset},10")
    List<Post> getUserPost(int uid, int offset);

    @Select(" select nickname replyToNickname, replyDetail.*" +
            " from (select title, r.*" +
            "      from (select r1.*," +
            "                   rex.content_show contentEx," +
            "                   rex.replyer      replyToId" +
            "            from (select postid," +
            "                         floor," +
            "                         content_show," +
            "                         replyTo,id" +
            "                  from reply" +
            "                  where replyer = #{uid}" +
            "                    and reply.available = true" +
            "                  order by id desc" +
            "                  limit #{offset},5) r1" +
            "                   left join reply rex" +
            "                             on rex.postid = r1.postid and r1.replyTo = rex.floor) r" +
            "             left join post on postid = pid" +
            "      where available = true) replyDetail" +
            "       left join user on replyToId = uid")
    List<Reply> getUserReply(int uid, int offset);

    @Update("update user set postNum=postNum+1 where uid=#{uid}")
    void addPostNum(int uid);


    @Select("select count(1) from message where owner=#{uid}")
    int getMessageNum(Integer uid);

    @Select("select pid, title, time" +
            " from history" +
            " where uid = #{uid}" +
            "  and id<=(select id from history where uid=#{uid} order by id desc limit #{offset}," +
            "1) order by time desc limit #{step} ")
    List<History> getHistories(Integer uid, int offset, int step);

    @Select("select count(*) from history where uid=#{uid}")
    int getHistoryNum(int uid);

    @Select("select count(*) from collection where uid=#{uid} and pid=#{pid}")
    int collectRelationshipIsExist(Integer uid, Integer pid);

    @Delete("delete from collection where uid=#{uid} and pid=#{pid}")
    void delCollect(Integer uid, Integer pid);

    @Insert("insert into collection (uid,pid) values(#{uid},#{pid})")
    void collect(Integer uid, Integer pid);

    @Select("select user.*,rid grade" +
            " from user" +
            "       left join user_role" +
            "                 on user_role.uid = user.uid" +
            " where nickname like concat" +
            "  ('%',#{target}, '%')" +
            "  and user.uid > #{offset} " +
            "  limit #{limit}")
    List<User> getUserByNickname(int offset, int limit, String target);

    @Select("select user.*,rid grade from user " +
            " left join user_role" +
            " on user_role.uid = user.uid" +
            " where user.uid>#{offset}  limit #{limit}")
    List<User> getUsersOfAdmin(int offset, int limit);

    @Select("select count(*) from user")
    int getUserNumOfAdmin();

    @Select("select count(*) from user where nickname like concat('%',#{target},'%')")
    int getUserNumForTargetOfAdmin(String target);

    @Update("update user,user_role" +
            " set email=#{email}," +
            "    nickname=#{nickname}," +
            "    age=#{age}," +
            "    gender=#{gender}," +
            "    password=#{password}," +
            "    rid=#{grade}," +
            "    available=#{available}" +
            " where user.uid = #{uid}" +
            "  and user_role.uid=#{uid}")
    int updateInfoByAdmin(User user);

    /**
     * 检查邮箱是否变更，若变更则检查是否存在
     *
     * @param email
     * @param uid
     * @return
     */
    @Select("select count(1) from user where email=#{email} and uid!=#{uid}")
    int checkExistOfEmailForUpdate(String email, int uid);

    /**
     * 检查昵称是否变更，若变更则检查是否存在
     *
     * @param nickname
     * @param uid
     * @return
     */
    @Select("select count(1) from user where email=#{nickname} and uid!=#{uid}")
    int checkExistOfNNForUpdate(String nickname, int uid);

    @Insert("insert into history (uid,pid,title) values(#{uid},#{pid},#{title})")
    void createHistory(Integer uid, Integer pid, String title);

    @Select("select count(1) from post where uid=#{uid} and available=1")
    int getUserPostNum(Integer uid);

    @Select("select count(1) from reply where replyer=#{uid} and available=1")
    int getUserReplyNum(Integer uid);

    @Update("update user set postNum=postNum-1 where uid=#{uid} and postNum>0")
    void decreasePostnum(Integer uid);

    @Select("select attention.*,nickname toUserNickname from attention left join user on " +
            "uid=toUser  where fromUser=#{uid}")
    List<Attention> getMyAttentions(Integer uid);

    @Select("select available from user where uid=#{uid}")
    boolean getStatus(Integer uid);

    @Select("select count(1) from history where uid=#{uid} and pid=#{pid}")
    int hasHistory(Integer uid, Integer pid);

    @Update("update history set time=#{time} where uid=#{uid} and pid=#{pid}")
    void updateHistory(Integer uid, Integer pid, Date time);
}
