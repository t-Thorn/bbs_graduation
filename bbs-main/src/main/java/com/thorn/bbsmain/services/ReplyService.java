package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.ReplyMapper;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.PageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
     * 回复列表框上传图片 todo 需要压缩图片
     *
     * @param imgList 图片合集
     * @return 消息字符串
     */
    public String imgUpload(MultipartFile[] imgList) {
        MsgBuilder builder = new MsgBuilder();
        //返回的图片链接地址
        List<String> imgLinkList = new ArrayList<>();
        //状态码
        int errno = 0;

        imgLinkList =
                Arrays.stream(imgList).map(this::createImgFile).filter(imgFile -> imgFile != null)
                        .collect(Collectors.toList());
        builder.addData("data", imgLinkList);
        builder.addData("errno", errno);
        //出错的图片数量
        builder.addData("errorNum", imgList.length - imgLinkList.size());
        return builder.getMsg();
    }

    private String createImgFile(MultipartFile img) {
        if (img.getSize() == 0 || img.getSize() / 1024 >= 50) {
            return null;
        }
        String fileType;
        String[] suffixs = {".jpg", ".gif", ".jpeg", ".png", ".JPG", ".GIF", ".JPEG", ".PNG"};
        if (!Arrays.stream(suffixs).anyMatch(suffix -> img.getOriginalFilename().endsWith(suffix))) {
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
    public List<Reply> getReplyByPid(int pid) {
        return replyMapper.getReplyByPID(pid);
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
        return replyMapper.getLikesNum(pid, floor);
    }

    public List<Reply> getReplies(Integer pid, MsgBuilder builder, int page) throws PageException {
        List<Reply> replyList = getReplyByPid(pid);

        //获取帖子内容
        builder.addData("topReply", replyList.get(0));
        replyList.remove(0);

        //分页
        if (page == -1) {
            //提供给新增回复一个接口跳转到最后一页
            page = PageUtil.getPage(replyList, ONE_PAGE_REPLY_NUM);
        }
        replyList = PageUtil.subList(replyList, page, ONE_PAGE_REPLY_NUM);
        builder.addData("page", page);
        builder.addData("pageNum",
                PageUtil.getPage(replyList, ONE_PAGE_REPLY_NUM));


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

    @RefreshHotPost("reply")
    @Transactional
    public ModelAndView addReply(Reply reply, BindingResult result) throws PostException {
        MsgBuilder builder = new MsgBuilder();
        if (result.hasErrors()) {
            result.getModel().forEach((k, v) -> System.out.println(k + ":" + v));
            builder.addData("errorMsg", result.getModel());
            return builder.getMsg("forward:/post/" + reply.getPostid());
        }
        if (replyMapper.isLegal(reply.getPostid(), reply.getReplyTo()) == 0) {
            throw new PostException("回复错误，请刷新页面后重试");
        }
        reply.setReplyer(userService.getCurrentUser().getUid());
        try {
            //帖子回复数+1
            postMapper.increaseReplyNum(reply.getPostid());
            //新增回复
            replyMapper.addReply(reply);
            //将注入到对象中的floor提取出来返回
            builder.addData("floor", reply.getFloor());
            //todo 消息加入
        } catch (Exception e) {
            throw new PostException("新增帖子错误");
        }

        return builder.getMsg("forward:/post/" + reply.getPostid() + "/-1");
    }

    @Transactional
    public ModelAndView delReply(int pid, int floor) {
        MsgBuilder builder = new MsgBuilder();

        return builder.getMsg("redirect:/post/" + pid);
    }
}
