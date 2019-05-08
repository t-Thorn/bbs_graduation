package com.thorn.bbsmain.mapper;

import org.apache.ibatis.annotations.Update;

public interface MessageMapper {

    @Update("update message set ischeck=true where id=#{id}")
    void checkMessage(int id);

}
