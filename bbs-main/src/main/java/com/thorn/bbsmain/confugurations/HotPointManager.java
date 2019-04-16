package com.thorn.bbsmain.confugurations;

import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.User;
import impl.DefaultDataSaver;
import impl.DefaultHotPointCache;
import impl.DefaultHotPostHandler;
import impl.DefaultViewCache;
import interfaces.AbstractHotPostHandler;
import interfaces.Fetcher;
import interfaces.HotPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HotPointManager {
    @Bean
    public Fetcher<Post> getFetch(PostMapper postMapper) {
        return new Fetcher<Post>() {
            @Override
            public Post getInfo(int pid) {
                return postMapper.getPostForHotPost(pid);
            }

            @Override
            public Integer getUID(Object object) {
                return ((User) object).getUid();
            }
        };
    }

    @Bean
    public DefaultDataSaver getMybatisDataSaver(PostMapper postMapper) {
        return new DefaultDataSaver() {
            @Override
            public void save(Object id, Object hotPoint) {
                if ((int) id > 0) {
                    //负数为ip地址，正数为pid
                    HotPoint post = ((HotPoint) hotPoint);
                    postMapper.updateViewAndReplyNum(((int) id), post.getView(), post.getReply());
                }
            }
        };
    }

    @Bean
    public AbstractHotPostHandler<Post> getHopPostHandler(Fetcher fetcher, DefaultDataSaver dataSaver) {
        DefaultHotPostHandler handler = new DefaultHotPostHandler<Post>(new DefaultViewCache(),
                new DefaultHotPointCache(), fetcher);
        handler.setDataSaver(dataSaver);
        handler.addReFreshTask(1, TimeUnit.DAYS);
        return handler;
    }

    @Bean
    public impl.HotPointManager getHotPointManager(AbstractHotPostHandler handler) {
        return new impl.HotPointManager<Post>(handler);
    }
}
