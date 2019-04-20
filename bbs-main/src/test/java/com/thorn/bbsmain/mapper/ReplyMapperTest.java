package com.thorn.bbsmain.mapper;

import com.thorn.bbsmain.BbsMainApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BbsMainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReplyMapperTest {
    @Autowired
    ReplyMapper replyMapper;

    @Test
    public void hasPermission() {
        assert replyMapper.hasPermission(100000, 4, 1) > 0;
    }
}