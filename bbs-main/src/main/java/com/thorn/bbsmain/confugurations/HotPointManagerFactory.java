package com.thorn.bbsmain.confugurations;

import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
import domain.HotPoint;
import impl.*;
import interfaces.Fetcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HotPointManagerFactory {
    @Value("${hotPoint.path.reload}")
    private String reloadPath;

    @Value("${hotPoint.period.reload}")
    private int reload;

    @Value("${hotPoint.period.refreshPeriod}")
    private int refreshPeriod;

    @Value("${hotPoint.period.savePeriod}")
    private int savePeriod;

    @Bean
    public Fetcher<Post> getFetch(PostMapper postMapper) {
        return new Fetcher<Post>() {
            @Override
            public Integer getPID(Object object) {
                return ((Reply) object).getPostid();
            }

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
                    if (post.getViewIncrement() == 0) {
                        //无增量则跳过
                        return;
                    }
                    synchronized (hotPoint) {
                        int increment = 0;
                        if ((increment = post.getViewIncrement()) == 0) {
                            //无增量则跳过
                            return;
                        }
                        //只保存取出时的量，减掉取出时的量。。可能没必要，但安全
                        postMapper.updateViewAndReplyNum(((int) id), increment);
                        //清空
                        post.setViewIncrement(post.getViewIncrement() - increment);
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
        manager.addRefreshTask(refreshPeriod, TimeUnit.DAYS);
        /**
         * 1分钟一次全量备份，作为测试时使用
         */
        manager.addCycleSaveThread(savePeriod, TimeUnit.HOURS);
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
