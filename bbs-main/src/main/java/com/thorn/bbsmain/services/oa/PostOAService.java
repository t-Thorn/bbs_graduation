package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.services.PostService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostOAService implements OAService<Post> {
    private PostService postService;

    private PostMapper postMapper;

    public PostOAService(PostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    @Override
    public List<Post> getList(int page, String target, int limit) {
        if (target != null && !"".equals(target)) {
            return postMapper.getPostsOfAdminForTarget((page - 1) * limit, limit, target);
        }
        return postMapper.getPostsOfAdmin((page - 1) * limit, limit);
    }

    @Override
    public Post getDetail(Object id) {
        return null;
    }

    @Override
    public boolean update(Post post) {
        return false;
    }

    @Override
    public boolean delete(Object id) {
        return false;
    }

    @Override
    public MsgBuilder addData() {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("code", 0);
        builder.addData("msg", "");
        builder.addData("count", postMapper.getPostNumOfAdmin());
        return builder;
    }
}
