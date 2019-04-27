package com.thorn.bbsmain.utils;

import com.alibaba.fastjson.JSONObject;
import com.thorn.bbsmain.controller.BasicController;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.services.PostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(BasicController.class)
public class MailServiceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PostService postService;

    @Test
    public void testSimpleMail() throws Exception {
        List<Post> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("posts", list);
        given(this.postService.buildHome(0, 1, null, null))
                .willReturn(new ModelAndView("/index"));
        this.mvc.perform(get("/index").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andExpect(content().json(jsonObject.toJSONString()));
    }
}