package com.thorn.bbsmain.confugurations;

import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import impl.DefaultHotPointCache;
import impl.DefaultViewCache;
import impl.HotPostHandler;
import interfaces.Fetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.TimeUnit;

/**
 * 新的实现会造成资源文件mapping错误
 */
@Configuration
public class ApplicationConfig extends WebMvcConfigurerAdapter {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        //yaml.setResources(new FileSystemResource("config.yml"));//File引入
        yaml.setResources(new ClassPathResource("application-setting.yml"));//class引入
        configurer.setProperties(yaml.getObject());
        return configurer;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /*
         * 说明：增加虚拟路径(经过本人测试：在此处配置的虚拟路径，用springboot内置的tomcat时有效，
         * 用外部的tomcat也有效;所以用到外部的tomcat时不需在tomcat/config下的相应文件配置虚拟路径了,阿里云linux也没问题)
         */
        registry.addResourceHandler("/img/**").addResourceLocations(
                "file:/run/media/thorn/Thorn Passport/毕设/img/");
        //registry.addResourceHandler("/upload/video/**").addResourceLocations("file:G:/upload" +
        //       "/video/");

        //阿里云(映射路径去除盘符)
        //registry.addResourceHandler("/ueditor/image/**").addResourceLocations("/upload/image/");
        //registry.addResourceHandler("/ueditor/video/**").addResourceLocations("/upload/video/");
        super.addResourceHandlers(registry);
    }

    @Bean
    public HotPostHandler<Post> getHopPostHandler(Fetcher fetcher) {
        HotPostHandler handler = new HotPostHandler<Post>(new DefaultViewCache(),
                new DefaultHotPointCache(), fetcher);
        handler.addTask(1, TimeUnit.DAYS);
        return handler;
    }

    @Bean
    public Fetcher<Post> getFetch(@Autowired PostMapper postMapper) {
        return new Fetcher<Post>() {
            @Override
            public Post getInfo(int pid) {
                return postMapper.getPost(pid);
            }

            @Override
            public Integer getID(Post post) {
                return post.getPid();
            }
        };
    }
}

