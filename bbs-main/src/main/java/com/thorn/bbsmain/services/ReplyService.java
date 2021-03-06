package com.thorn.bbsmain.services;


import annotation.RefreshHotPost;
import com.thorn.bbsmain.exceptions.DeleteReplyException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.exceptions.PostNotFoundException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.ReplyMapper;
import com.thorn.bbsmain.mapper.entity.Message;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import com.thorn.bbsmain.utils.message.MessageObject;
import com.thorn.bbsmain.utils.review.ReviewInfo;
import impl.HotPointManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    private RabbitTemplate rabbitTemplate;

    private MessageService messageService;

    public ReplyService(ReplyMapper replyMapper, PostMapper postMapper,
                        UserService userService, RabbitTemplate rabbitTemplate,
                        MessageService messageService) {
        this.replyMapper = replyMapper;
        this.postMapper = postMapper;
        this.userService = userService;
        this.rabbitTemplate = rabbitTemplate;
        this.messageService = messageService;
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
            String imgFileOfCompress = MyUtil.compressImg(img, fileType, imgFile, imgPath);
            if (imgFileOfCompress != null) {
                return "/img/replyImg/" + imgFileOfCompress;
            }
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
    void createPostTopReply(Reply reply) {
        replyMapper.createPostTopReply(reply);
    }


    //@cacheable
    private List<Reply> getReplyByPid(int pid, int page) {
        int offset = page * ONE_PAGE_REPLY_NUM;
        return replyMapper.getReplyByPID(pid, offset,
                ONE_PAGE_REPLY_NUM);
    }

    private List<Integer> getLikesByPID(int pid, int uid) {
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
        //不给自己发消息
        if (!uid.equals(to)) {
            Message message = new Message();
            message.setPid(pid);
            message.setFloor(floor);
            message.setOwner(to);
            message.setFromUser(uid);
            message.setPostTitle(postMapper.getPost(pid).getTitle());
            message.setContent("");
            message.setType(1);
            messageService.addMessage(message);
            rabbitTemplate.convertAndSend("newMsg",
                    MessageObject.builder().uid(to).type(1).num(1).build());
        }
        return replyMapper.getLikesNum(pid, floor);
    }

    List<Reply> getReplies(Integer pid, MsgBuilder builder, int page) throws PostNotFoundException {
        int replyNum;
        //减一是去掉顶楼回复
        replyNum = replyMapper.getReplyNum(pid) - 1;
        if (page == -1) {
            //提供给新增回复一个接口跳转到最后一页
            page = MyUtil.getPage(replyNum, ONE_PAGE_REPLY_NUM);
        }
        List<Reply> replyList = getReplyByPid(pid, page - 1);
        //page可以包容-1和1
        if (replyList.size() == 0 && page != 1 && page != -1) {
            throw new PostNotFoundException("该页面不存在该页");
        }
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
            reply.setFloor(replyMapper.getFloorByID(reply.getId(), reply.getPostid()));
        } catch (Exception e) {
            throw new PostException("新增帖子错误" + e.getMessage());
        }
        /**
         * 获取帖子主人，和回复的层主id(如果存在)
         */
        int postOwner = postMapper.getPostOwner(reply.getPostid());
        //不给自己发消息
        if (postOwner != reply.getReplyer()) {
            addMessage(reply, postOwner);
        }
        if (reply.getReplyTo() != null) {
            int replyToID = replyMapper.getReplyOwner(reply.getReplyTo(), reply.getPostid());
            if (replyToID != reply.getReplyer() && replyToID != postOwner) {
                addMessage(reply, replyToID);
            }
        }
        rabbitTemplate.convertAndSend("review",
                ReviewInfo.builder().uid(reply.getReplyer()).pid(reply.getPostid()).floor(reply.getFloor()).content(reply.getContent()).build());
        return builder.getMsg("redirect:/post/" + reply.getPostid() + "/-1/" + reply.getId());
    }

    private void addMessage(Reply reply, int replyTo) {
        Message message = new Message();
        message.setPid(reply.getPostid());
        message.setFloor(reply.getFloor());
        message.setOwner(replyTo);
        message.setFromUser(reply.getReplyer());
        message.setPostTitle(postMapper.getPost(reply.getPostid()).getTitle());
        message.setContent(reply.getContent_show());
        message.setType(0);
        messageService.addMessage(message);
        rabbitTemplate.convertAndSend("newMsg",
                MessageObject.builder().uid(replyTo).type(1).num(1).build());
    }

    @RefreshHotPost(HotPointManager.DELREPLY)
    @Transactional(rollbackFor = DeleteReplyException.class)
    public String delReply(int pid, int floor) throws PostException, DeleteReplyException {
        if (floor < 1 || pid < 1) {
            throw new PostException("删除回复参数错误:楼层或帖子id非法");
        }
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new DeleteReplyException("请登录再试");
        }
        if (!isExist(pid, floor)) {
            throw new DeleteReplyException("删除回复参数错误，不存在该帖子或者楼层");
        }
        if (!userService.hasRole("admin") && replyMapper.hasPermission(user.getUid(), pid, floor) == 0) {
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

    public int getReplyOffsetByFloor(int pid, int floor) {
        return replyMapper.getOffsetByFloor(pid, floor);
    }

    public int getONE_PAGE_REPLY_NUM() {
        return ONE_PAGE_REPLY_NUM;
    }

    public int getReplyIDByFloor(int pid, int floor) {
        return replyMapper.getReplyIDByFloor(pid, floor);
    }
}
