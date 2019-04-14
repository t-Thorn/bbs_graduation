package com.thorn.bbsmain.mapper;

import com.thorn.bbsmain.mapper.entity.Collect;
import com.thorn.bbsmain.mapper.entity.Post;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PostMapper {

    @Select("select user.uid,pid," +
            "       title," +
            "       user.nickname," +
            "       type," +
            "       grade," +
            "       lastReplyTime," +
            "       replyNum," +
            "       img from user inner join post on post.uid = user.uid" +
            " where grade < 2" +
            "  and avaliable = true " +
            "order by pid desc ")
//    @Cacheable(value = "posts", key = "'posts_'+'normal'", unless = "#result==null")
    List<Post> getPosts();

    @Select("select user.uid,pid," +
            "       title," +
            "       user.nickname," +
            "       type," +
            "       grade," +
            "       lastReplyTime," +
            "       replyNum," +
            "       img from user inner join post on post.uid = user.uid" +
            " where grade < 2" +
            "  and avaliable = true and title like concat('%',#{target}," +
            "    '%')" +
            " order by pid desc ")
    List<Post> getPostsForTarget(String target);

    @Select("select user.uid,pid,title,user.nickname,type,grade,postTime,lastReplyTime,replyNum," +
            "img from user inner join post on  post.uid=user.uid where  type!=4 and grade>=2 and " +
            "avaliable=true limit 8")
    List<Post> getTopPosts();

    @Select("select pid,title,type,grade,lastReplyTime,replyNum from post where type=4 " +
            "and avaliable=true order by pid")
    List<Post> getAnnouncements();

    @Select("select user.uid,pid," +
            "       title," +
            "       user.nickname," +
            "       type," +
            "       grade," +
            "       lastReplyTime," +
            "       replyNum," +
            "       img" +
            " from user" +
            "       inner join post on post.uid = user.uid" +
            " where type != 4" +
            "  and grade = 1" +
            "  and avaliable = true" +
            " order by pid desc")
//    @Cacheable(value = "posts", key = "'goodposts'", unless = "#result==null")
    List<Post> getGoodPosts();

    @Select("select pid,title,views,replyNum,lastReplytime from post where uid=#{uid} " +
            "and avaliable=1 ")
    List<Post> getMyPost(int uid);

    @Select("select title, collection.pid, time from collection" +
            "       left join post on collection.uid=#{uid} where collection.pid = post.pid  and" +
            "      avaliable = 1")
    List<Collect> getMyColletction(int uid);

    @Insert("insert into post (title, uid,type) values (#{title}, #{uid},#{type})")
    @Options(useGeneratedKeys = true, keyProperty = "pid", keyColumn = "pid")
    void createPost(Post post);

    @Select("select * from post where pid=#{pid}")
    Post getPost(int pid);

    /**
     * 更新浏览量和回复数
     *
     * @param view  增量
     * @param reply 增量
     */
    @Update("update post set views=#{view} ,replyNum=#{reply} where pid=#{pid}")
    void updateViewAndReplyNum(int pid, int view, int reply);
}
