package com.thorn.bbsmain.mapper;

import com.thorn.bbsmain.mapper.entity.Collect;
import com.thorn.bbsmain.mapper.entity.Post;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface PostMapper {

    @Select("select user.uid,pid," +
            "       title," +
            "       user.nickname," +
            "       type," +
            "       grade," +
            "       lastReplyTime," +
            "       replyNum," +
            "       img from post" +
            "       left JOIN user on post.uid = user.uid" +
            " where grade < 2" +
            "  and post.available = true and pid<=(select pid from post where available=true " +
            "order by pid desc limit #{offset},1)" +
            " order by pid desc limit #{step}")
    List<Post> getPosts(int offset, int step);

    @Select("select user.uid,pid," +
            "       title," +
            "       user.nickname," +
            "       type," +
            "       grade," +
            "       lastReplyTime," +
            "       replyNum," +
            "       img from post" +
            "       left JOIN user on post.uid = user.uid" +
//            屏蔽这个则可以显示全部" where grade < 2" +
            "  where post.available = true and title like concat('%',#{target}," +
            "    '%')  and pid<=(select pid from post where available=true order by pid desc limit #{offset},1)" +
            " order by pid desc limit #{step}")
    List<Post> getPostsForTarget(String target, int offset, int step);

    @Select("select user.uid,pid,title,user.nickname,type,grade,postTime,lastReplyTime,replyNum," +
            "img from user inner join post on  post.uid=user.uid where  type!=4 and grade>=2 and " +
            "post.available=true limit 8")
    @Cacheable(value = "posts", key = "'topposts'", unless = "#result==null")
    List<Post> getTopPosts();

    @Select("select pid,title,type,grade,lastReplyTime,replyNum from post where type=4 " +
            "and available=true order by pid")
    @Cacheable(value = "posts", key = "'announcements'", unless = "#result==null")
    List<Post> getAnnouncements();

    @Select("select user.uid,pid," +
            "       title," +
            "       user.nickname," +
            "       type," +
            "       grade," +
            "       lastReplyTime," +
            "       replyNum," +
            "       img" +
            " from post" +
            "       left JOIN user on post.uid = user.uid" +
            " where type != 4" +
            "  and grade = 1" +
            "  and post.available = true and pid<=(select pid from post where available=true order by pid desc" +
            " limit #{offset},1)" +
            " order by pid desc limit #{step}")
//    @Cacheable(value = "posts", key = "'goodposts'", unless = "#result==null")
    List<Post> getGoodPosts(int offset, int step);

    @Select("select pid,title,views,replyNum,lastReplytime from post where uid=#{uid} " +
            "and available=1 ")
    List<Post> getMyPost(int uid);

    @Select("select title, collection.pid, time from collection" +
            "       left join post on collection.uid=#{uid} where collection.pid = post.pid  and" +
            "      available = 1")
    List<Collect> getMyColletction(int uid);

    @Insert("insert into post (title, uid,type) values (#{title}, #{uid},#{type})")
    @Options(useGeneratedKeys = true, keyProperty = "pid", keyColumn = "pid")
    void createPost(Post post);

    /**
     * 获取热帖信息
     *
     * @param pid
     * @return 热帖缓存中保存的简单信息
     */
    @Select("select pid,title from post where pid=#{pid}")
    Post getPostForHotPost(int pid);

    @Select("select p.*,img,nickname from (select * from post where pid=#{pid})p left join user " +
            "on user.uid=p.uid limit 1")
    Post getPost(int pid);
    /**
     * 更新浏览量和回复数
     *
     * @param view  增量
     */
    @Update("update post set views=views+#{view}  where pid=#{pid}")
    void updateViewAndReplyNum(int pid, int view);

    @Update("update post set replyNum=replyNUm+1 where pid=#{postid}")
    void increaseReplyNum(int postid);

    @Update("update post set lastReplyTime=sysdate() where pid=#{postid}")
    void updateLastReplyTime(int postid);

    @Update("update post set replyNUm=replyNUm-1 where pid=#{pid}")
    void decreaseReplyNum(int pid);

    @Select("select count(*) from post where available=1 and  grade < 2")
    int getPostNum();

    @Select("select count(*) from post where available=1 and title like concat('%',#{target},'%')")
    int getPostNumOfTarget(String target);

    @Select("select count(*) from post where available=1 and type != 4 and grade = 1")
    int getGoodPostNum();

    @Update("update post set collectionNum=collectionNum-1 where pid=#{pid}")
    void decreaseCollectNum(Integer pid);

    @Update("update post set collectionNum=collectionNum+1 where pid=#{pid}")
    void increaseCollectNum(Integer pid);

    @Select("select post.*,nickname" +
            " from post left join user on post.uid=user.uid where pid>#{offset}" +
            " limit #{limit};")
    List<Post> getPostsOfAdmin(int offset, int limit);

    @Select("select post.*,nickname" +
            " from post left join user on post.uid=user.uid where " +
            "  title like concat('%',#{target},'%') limit #{offset},#{limit};")
    List<Post> getPostsOfAdminForTarget(int offset, int limit, String target);

    @Select("select post.*,nickname" +
            " from post left join user on post.uid=user.uid where " +
            " nickname like concat('%',#{target},'%') limit #{offset},#{limit};")
    List<Post> getPostsOfAdminForTargetByUsername(int offset, int limit, String target);

    @Select("select count(*) from post")
    int getPostNumOfAdmin();

    @Select("select count(*) from post where uid=#{uid} and pid=#{pid} and available=true")
    int hasPermission(Integer uid, int pid);

    @Select("select count(*) from post where pid=#{pid} and available=1")
    int isExist(int pid);

    @Update("update post set available=0 where pid=#{pid} and available=1")
    void invalidPost(int pid);

    @Update("update post set title=#{title} , type=#{type} , grade=#{grade}, " +
            "available=#{available} where pid=#{pid}")
    void updatePost(Post post);

    @Select("select count(*) from post where title like concat('%',#{target},'%')")
    int getPostNumForTargetOfAdmin(String target);

    @Select("select count(*) from post left join user on post.uid=user.uid where nickname like " +
            "concat('%',#{target},'%')")
    int getPostNumForTargetByUsernameOfAdmin(String target);

    @Select("select uid from post where pid=#{postid}")
    int getPostOwner(Integer postid);
}
