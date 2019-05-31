package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.services.PostService;
import com.thorn.bbsmain.services.UserService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostOAService implements OAService<Post> {
    private PostService postService;

    private PostMapper postMapper;

    private UserService userService;

    public PostOAService(PostService postService, PostMapper postMapper, UserService userService) {
        this.postService = postService;
        this.postMapper = postMapper;
        this.userService = userService;
    }

    public Post getPostInfo(int pid) {
        return postMapper.getPost(pid);
    }

    @Override
    public List<Post> getList(int page, String target, int limit, int type, MsgBuilder builder) {
        if (target != null && !"".equals(target)) {
            if (type == 1) {
                builder.addData("count", postMapper.getPostNumForTargetOfAdmin(target));
                return postMapper.getPostsOfAdminForTarget((page - 1) * limit, limit, target);
            }
            builder.addData("count", postMapper.getPostNumForTargetByUsernameOfAdmin(target));
            return postMapper.getPostsOfAdminForTargetByUsername((page - 1) * limit, limit, target);
        }
        builder.addData("count", postMapper.getPostNumOfAdmin());
        return postMapper.getPostsOfAdmin((page - 1) * limit, limit);
    }

    @Override
    public Post getDetail(Object id) {
        return null;
    }

    @Override
    public boolean update(Post post) {
        if (post.getAvailable() == null) {
            post.setAvailable(false);
        }
        if (postMapper.isAvailable(post.getPid())) {
            if (!post.getAvailable()) {
                postMapper.disablePost(post.getPid());
                userService.decreasePostnum(post.getUid());
                postService.delPostOfHotPoint(post.getPid());
            }
        } else {
            if (post.getAvailable()) {
                userService.increasePostNum(post.getUid());
                postMapper.enablePost(post.getPid());
            }
        }
        postMapper.updatePost(post);
        return true;
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

        return builder;
    }
}
