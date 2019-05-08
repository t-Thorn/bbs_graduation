package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.DeleteReplyException;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.ReplyMapper;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import impl.HotPointManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReplyService {


    @Value("${system.path.replyImg}")
    String imgPath;

    @Value("${system.page.reply}")
    private int ONE_PAGE_REPLY_NUM;

    private ReplyMapper replyMapper;

    private PostMapper postMapper;

    private UserService userService;

    public ReplyService(ReplyMapper replyMapper, PostMapper postMapper, UserService userService) {
        this.replyMapper = replyMapper;
        this.postMapper = postMapper;
        this.userService = userService;
    }

    /**
     * @param imgList 图片合集
     * @return 消息字符串
     */
    public String imgUpload(MultipartFile[] imgList) {
        MsgBuilder builder = new MsgBuilder();
        //返回的图片链接地址
        List<String> imgLinkList;
        //状态码
        int errno = 0;

        imgLinkList =
                Arrays.stream(imgList).map(this::createImgFile).filter(Objects::nonNull)
                        .collect(Collectors.toList());
        builder.addData("data", imgLinkList);
        builder.addData("errno", errno);
        //出错的图片数量
        builder.addData("errorNum", imgList.length - imgLinkList.size());
        return builder.getMsg();
    }

    private String createImgFile(MultipartFile img) {

        if (img.getSize() == 0 && img.getOriginalFilename() == null) {
            return null;
        }
        String fileType;
        String[] suffixs = {".jpg", ".gif", ".jpeg", ".png", ".JPG", ".GIF", ".JPEG", ".PNG"};
        if (Arrays.stream(suffixs).noneMatch(suffix -> img.getOriginalFilename().endsWith(suffix))) {
            return null;
        } else {
            fileType = img.getOriginalFilename().substring(img.getOriginalFilename().lastIndexOf(
                    "."));
        }

        File imgFile = new File(imgPath + UUID.randomUUID() + fileType);
        try {
            if (!imgFile.exists()) {
                if (!imgFile.createNewFile()) {
                    return null;
                }
            }
            img.transferTo(imgFile);
            String imgFileOfCompress = MyUtil.CompressImg(img, fileType, imgFile, imgPath);
            if (imgFileOfCompress != null) return "/img/replyImg/" + imgFileOfCompress;
        } catch (IOException e) {
            return null;
        }

        return "/img/replyImg/" + imgFile.getName();
    }

    /**
     * 新建帖子时的一楼回复
     *
     * @param reply 回复信息
     */
    public void createPostTopReply(Reply reply) {
        replyMapper.createPostTopReply(reply);
    }


    //@cacheable
    public List<Reply> getReplyByPid(int pid, int page) {
        int offset = page * ONE_PAGE_REPLY_NUM;
        offset = offset == 0 ? 1 : offset;
        return replyMapper.getReplyByPID(pid, offset,
                ONE_PAGE_REPLY_NUM);
    }

    public List<Integer> getLikesByPID(int pid, int uid) {
        return replyMapper.getLikesByPID(pid, uid);
    }

    @Transactional
    public int zan(Integer floor, int pid, Integer uid, Integer to) {
        if (replyMapper.exist(pid, uid, floor) > 0) {
            //存在则取消点赞
            replyMapper.unzan(pid, uid, floor);
            replyMapper.decreaseLikesNum(pid, floor);
            return replyMapper.getLikesNum(pid, floor);
        }
        replyMapper.zan(pid, uid, floor, to);
        replyMapper.increaseLikesNum(pid, floor);
        // TODO: 19-5-8 消息提醒
        return replyMapper.getLikesNum(pid, floor);
    }

    public List<Reply> getReplies(Integer pid, MsgBuilder builder, int page) throws PageException {
        int replyNum;
        replyNum = replyMapper.getReplyNum(pid);
        if (page == -1) {
            //提供给新增回复一个接口跳转到最后一页
            page = MyUtil.getPage(replyNum, ONE_PAGE_REPLY_NUM);
        }
        List<Reply> replyList = getReplyByPid(pid, page - 1);

        //获取帖子内容
        builder.addData("topReply", getTopReply(pid));

        //分页


        builder.addData("page", page);
        builder.addData("pageNum",
                MyUtil.getPage(replyNum, ONE_PAGE_REPLY_NUM));
        //获取点赞列表
        User user = userService.getCurrentUser();
        if (user != null) {
            List<Integer> likeList = getLikesByPID(pid, user.getUid());
            replyList.forEach(r -> {
                if (likeList.contains(r.getFloor())) {
                    r.setZan(true);
                }
            });
            builder.addData("user", user);
        }
        return replyList;
    }

    private Reply getTopReply(Integer pid) {
        return replyMapper.getTopReply(pid);
    }


    @RefreshHotPost(HotPointManager.REPLY)
    @Transactional(rollbackFor = PostException.class)
    public ModelAndView addReply(Reply reply, BindingResult result) throws PostException {
        MsgBuilder builder = new MsgBuilder();
        if (result.hasErrors()) {
            builder.addData("errorMsg", result.getModel());
            return builder.getMsg("forward:/post/" + reply.getPostid());
        }
        if (replyMapper.isLegal(reply.getPostid(), reply.getReplyTo()) == 0) {
            throw new PostException("回复错误，请刷新页面后重试");
        }
        reply.setReplyer(userService.getCurrentUser().getUid());
        try {
            //帖子回复数+1,最后回复时间更新
            postMapper.increaseReplyNum(reply.getPostid());
            postMapper.updateLastReplyTime(reply.getPostid());
            //新增回复
            replyMapper.addReply(reply);
            //将注入到对象中的id提取出来返回
            builder.addData("replyID", reply.getId());
            //todo 消息加入
        } catch (Exception e) {
            throw new PostException("新增帖子错误" + e.getMessage());
        }

        return builder.getMsg("forward:/post/" + reply.getPostid() + "/-1");
    }

    @RefreshHotPost(HotPointManager.DELREPLY)
    @Transactional(rollbackFor = DeleteReplyException.class)
    public String delReply(int pid, int floor) throws PostException, DeleteReplyException {
        if (floor < 1 || pid < 1) {
            throw new PostException("删除回复参数错误");
        }
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new DeleteReplyException("请登录再试");
        }
        if (!isExist(pid, floor)) {
            throw new DeleteReplyException("删除回复参数错误");
        }
        if (!userService.hasRole("admin") || replyMapper.hasPermission(user.getUid(), pid, floor) == 0) {
            throw new DeleteReplyException("没有权限删除");
        }

        MsgBuilder builder = new MsgBuilder();
        //回复可用置0
        if (replyMapper.invalidReply(pid, floor) > 0) {
            postMapper.decreaseReplyNum(pid);
        } else {
            throw new DeleteReplyException("已被删除");
        }
        //帖子回复数-1

        builder.addData("msg", "成功");
        return builder.getMsg();
    }

    boolean isExist(int pid, int floor) {
        return replyMapper.isExist(pid, floor) > 0;
    }
}
