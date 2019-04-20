package com.thorn.bbsmain.testdemo;

import com.thorn.bbsmain.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TestRepository {

    @Autowired
    private UserMapper userMapper;

  /*  @Transactional
    public void Test() throws Exception {
        int i = userMapper.insertAttion("z1", "tz1");
    }*/

    @Cacheable(value = "user", key = "#id")
    public String get(String id) {
        ConcurrentHashMap map = new ConcurrentHashMap();

        return userMapper.getUserByUserName(id).toString();
    }
}
