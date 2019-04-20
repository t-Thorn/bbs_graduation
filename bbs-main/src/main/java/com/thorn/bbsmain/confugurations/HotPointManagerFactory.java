package com.thorn.bbsmain.confugurations;

import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.User;
import impl.*;
import interfaces.Fetcher;
import interfaces.HotPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HotPointManagerFactory {
    @Value("${system.path.reload}")
    private String reloadPath;

    @Value("${system.period.reload}")
    private int reload;


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
                    synchronized (post) {
                        if (post.getReplyIncrement() == 0 && post.getViewIncrement() == 0) {
                            //无增量则跳过
                            return;
                        }
                        postMapper.updateViewAndReplyNum(((int) id), post.getViewIncrement(),
                                post.getReplyIncrement());
                        //清空
                        post.setViewIncrement(0);
                        post.setReplyIncrement(0);
                    }
                }
            }
        };
    }

    @Bean
    public HotPointManager getHotPointManager(DefaultDataSaver saver, DefaultHotPostHandler hotPostHandler) {
        HotPointManager manager = new HotPointManager<Post>(hotPostHandler);
        manager.addCycleSaveForReloadTask(reload, TimeUnit.SECONDS, reloadPath);
        manager.addDataSavor(saver);
        manager.addRefreshTask(1, TimeUnit.DAYS);
        return manager;
    }

    @Bean
    public DefaultHotPostHandler getHotPointHandler(Fetcher fetcher) {
        if (LoadRecord.reloadRecord(reloadPath)) {
            return new DefaultHotPostHandler<Post>(LoadRecord.getViewCache(),
                    LoadRecord.getHotPointCache(), fetcher, LoadRecord.getIndexCache(),
                    LoadRecord.getHotPostCache());
        }
        return new DefaultHotPostHandler(new DefaultViewCache(), new DefaultHotPointCache(), fetcher);
    }
}
