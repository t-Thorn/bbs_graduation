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

            @Override
            public Class<Post> getPostClass() {
                return Post.class;
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
                        System.out.println("保存当前增量到数据库 pid:" + id + " increment:" + increment);
                        //清空
                        post.setViewIncrement(post.getViewIncrement() - increment);
                    }
                }
            }
        };
    }

    @Bean
    public HotPointManager<Post> getHotPointManager(DefaultDataSaver saver,
                                                    DefaultHotPostHandler<Post> hotPostHandler) {
        HotPointManager<Post> manager = new HotPointManager<>(hotPostHandler);
        manager.addCycleSaveForReloadTask(reload, TimeUnit.SECONDS, reloadPath);
        manager.addDataSavor(saver);
        manager.addRefreshTask(refreshPeriod * 60 * 60 * 1000);
        /**
         * 1分钟一次全量备份，作为测试时使用
         */
        manager.addCycleSaveThread(savePeriod, TimeUnit.MINUTES);
        return manager;
    }

    @Bean
    public DefaultHotPostHandler<Post> getHotPointHandler(Fetcher<Post> fetcher) {
        LoadRecord<Post> loadRecord = new LoadRecord<>();
        if (loadRecord.reloadRecord(reloadPath)) {
            return new DefaultHotPostHandler<>(loadRecord.getViewCache(),
                    loadRecord.getHotPointCache(), fetcher, loadRecord.getIndexCache(),
                    loadRecord.getHotPostCache());
        }
        return new DefaultHotPostHandler<>(new DefaultViewCache(), new DefaultHotPointCache(),
                fetcher);
    }
}
