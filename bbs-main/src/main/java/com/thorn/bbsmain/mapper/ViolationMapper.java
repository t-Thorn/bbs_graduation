package com.thorn.bbsmain.mapper;

import com.thorn.bbsmain.mapper.entity.Sensitivity;
import com.thorn.bbsmain.mapper.entity.Violation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ViolationMapper {

    @Select("select * from sensitivity where word like concat('%',#{search},'%') limit #{offset}," +
            "#{step}")
    List<Sensitivity> getSensitivities(int offset, int step, String search);

    @Select("select word from sensitivity")
    List<String> getSensitiveWord();

    @Select("select count(1) from sensitivity where word like concat('%',#{search},'%')")
    int getSensitivitiesNum(String search);

    @Update("update sensitivity set available=#{available} where id=#{id}")
    void setSensitivityStatus(int id, boolean available);


    @Select({"<script>",
            "select count(1) num from (select count(1) from violation where TO_DAYS(NOW()",
            ") - TO_DAYS(time)&lt;= 30",
            "<if test='search!=null'>",
            "and uid=#{search}",
            "</if>",
            " group by uid)tmp",
            "</script>"
    })
    int getViolationNum(@Param("search") String search);

    /**
     * 获取违规名单
     *
     * @param offset 偏移
     * @param step   每页数量
     * @param search 搜索目标
     * @return
     */
    @Select({"<script> ",
            "select count(1) num,uid from violation where TO_DAYS(NOW()) - TO_DAYS(time)",
            "  &lt;= 30 ",
            "<if test='search!=null'>",
            "AND uid=#{search}",
            "</if>",
            " group by uid order by num desc limit #{offset},#{step}",
            "</script>"
    })
    List<Violation> getViolations(int offset, int step, @Param("search") String search);


    @Select({"<script>",
            " select count(1)",
            "    FROM violation",
            "<if test='search!=null'>",
            "where uid=#{search}",
            "</if>",
            "</script>"
    })
    int getViolationRecordNum(@Param("search") String search);

    @Select({"<script>",
            "  select violation.*,title from violation left join post on violation" +
                    ".pid=post.pid",
            "<if test='search!=null'>",
            "where violation.uid=#{search}",
            "</if>",
            "order by time desc limit #{offset},#{step}",
            "</script>"
    })
    List<Violation> getViolationRecords(int offset, int step, @Param("search") String search);

    @Insert("insert into violation (uid,pid,floor,word) values(#{uid},#{pid},#{floor},#{word})")
    void addRecordOfViolation(int uid, int pid, int floor, String word);

    @Insert("insert into sensitivity (word) values(#{word})")
    void addSensitivity(String word);
}
