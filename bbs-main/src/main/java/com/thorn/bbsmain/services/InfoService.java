package com.thorn.bbsmain.services;

import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.PostException;
import com.thorn.bbsmain.exceptions.UserException;
import com.thorn.bbsmain.exceptions.UserInfoException;
import com.thorn.bbsmain.mapper.PostMapper;
import com.thorn.bbsmain.mapper.UserMapper;
import com.thorn.bbsmain.mapper.entity.*;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class InfoService {


    @Value("${system.page.message}")
    private int ONE_MESSAGE_PAGE_NUM;

    @Value("${system.page.mypost}")
    private int ONE_PAGE_POST_NUM;

    @Value("${system.page.history}")
    private int ONE_PAGE_HISTORY_NUM;

    @Value("${system.path.avator}")
    private String IMG;

    private PostMapper postMapper;

    private UserService userService;

    private ReplyService replyService;

    private MessageService messageService;

    private UserMapper userMapper;

    public InfoService(PostMapper postMapper, UserMapper userMapper,
                       UserService userService, ReplyService replyService,
                       MessageService messageService) {
        this.postMapper = postMapper;
        this.userService = userService;
        this.replyService = replyService;
        this.userMapper = userMapper;
        this.messageService = messageService;
    }

    public void updateBasicInfo(User user) throws UserException {
        userService.isNicknameExist(user);
        userService.updateBasicInfo(user);
    }

    /**
     * 获取某用户的发帖
     *
     * @param uid
     * @return
     */
    @Cacheable(value = "posts", key = "'mypost_'+#uid", unless = "#result==null")
    public List<Post> getMyPost(int uid) throws Exception {
        return postMapper.getMyPost(uid);
    }

    /**
     * 获取某用户的收藏
     *
     * @param uid 用户id
     * @return
     */
    @Cacheable(value = "posts", key = "'collect_'+#uid", unless = "#result==null")
    public List<Collect> getMyCollection(Integer uid) throws Exception {
        return postMapper.getMyColletction(uid);
    }

    public ModelAndView getMyMessages(int page) throws PageException {
        if (page < 1) {
            throw new PageException("非法参数");
        }
        MsgBuilder builder = new MsgBuilder();
        User user = userService.getCurrentUser();
        builder.addData("page", page);
        List<Message> messages = userService.getMessages(user.getUid(),
                (page - 1) * ONE_MESSAGE_PAGE_NUM, ONE_MESSAGE_PAGE_NUM);
        messages.forEach(v -> {
            if (v.getType() != 2 && !replyService.isExist(v.getPid(), v.getFloor())) {
                v.setContent("<p>此回复已被删除</p>");
            }
            messageService.checkMessage(v.getId());
        });
        int pageNum = userService.getMessageNum(user.getUid());
        builder.addData("messages", messages);
        builder.addData("pageNum", MyUtil.getPage(pageNum, ONE_MESSAGE_PAGE_NUM));
        return builder.getMsg("user/message");
    }

    public ModelAndView updateUserPassword(String nowpass, User user, BindingResult result,
                                           String repass, HttpServletResponse response) throws UserInfoException {
        User currentUser = userService.getCurrentUser();
        MsgBuilder builder = new MsgBuilder();
        boolean error = false;
        //加入锚点
        builder.addData("loc", "#pass");
        //检验合法性
        if (result.hasErrors()) {
            userService.getErrors(result, builder);
            error = true;
        }
        if (nowpass == null || user.getPassword() == null || repass == null) {
            builder.addData("errorMsg", "请填写完整");
            error = true;
        }
        if (!user.getPassword().equals(repass)) {
            builder.addData("errorMsg", "两次输入的密码不一样");
            error = true;
        }
        if (!currentUser.getPassword().equals(nowpass)) {
            builder.addData("errorMsg", "密码错误");
            error = true;
        }
        if (error) {
            builder.addDatas(getInfo(currentUser.getEmail()));
            return builder.getMsg("user/set");
        }
        userService.updatePassword(currentUser.getEmail(), user.getPassword());
        //logout
        userService.logout(response);
        //重新登录shiro
        currentUser.setPassword(user.getPassword());
        userService.userLogin(currentUser, null, response, builder);
        builder.addData("errorMsg", "密码修改成功");
        builder.addDatas(getInfo(currentUser.getEmail()));
        builder.addData("loc", "password");
        return builder.getMsg("user/set");
    }

    public ModelAndView modifyBasicInfo(User user, BindingResult bindingResult) {
        MsgBuilder msgBuilder = new MsgBuilder();
        if (bindingResult.hasErrors()) {
            userService.getErrors(bindingResult, msgBuilder);
        } else {
            try {
                updateBasicInfo(user);
            } catch (UserException e) {
                msgBuilder.addData("errorMsg", "更新失败：" + e.getMessage());
            }
        }
        msgBuilder.addDatas(getInfo(user.getEmail()));
        msgBuilder.addData("loc", "basic");
        return msgBuilder.getMsg("user/set");
    }

    public ModelAndView getMyPosts(Integer page, Integer cpage) throws Exception {
        if (page < 1 || cpage < 1) {
            throw new Exception("非法参数");
        }
        MsgBuilder builder = new MsgBuilder();
        User user = userService.getCurrentUser();
        List<Post> myPost = getMyPost(user.getUid());
        if (!Objects.equals(myPost, null) && myPost.size() > 0) {
            builder.addData("currentPage", page);
            builder.addData("myposts", MyUtil.subList(myPost, page, ONE_PAGE_POST_NUM));
            builder.addData("mypostsNum", myPost.size());

        }
        List<Collect> myCollection = getMyCollection(user.getUid());
        if (!Objects.equals(myCollection, null) && myCollection.size() > 0) {
            builder.addData("currentCpage", cpage);
            builder.addData("collections", MyUtil.subList(myCollection, cpage, ONE_PAGE_POST_NUM));
            builder.addData("collectionsNum", myCollection.size());
        }

        return builder.getMsg("user/myPost");
    }

    /**
     * 根据email从数据库中取出对应的用户
     *
     * @param email
     * @return
     */
    public User getInfo(String email) {
        return userService.getInfo(email);
    }

    /**
     * 上传并更新头像，shiro保存的是旧头像，但前端是根据缓存来读取,所以不影响
     *
     * @param img
     * @return
     * @throws IOException
     */
    public String updataAvator(MultipartFile img, HttpServletResponse response) throws UserInfoException {
        Subject currentUser = SecurityUtils.getSubject();
        User user = (User) currentUser.getPrincipal();
        MsgBuilder msgBuilder = new MsgBuilder();
        if (img.getSize() == 0) {

            throw new UserInfoException("图片为空");
        }
        if (img.getSize() / 1024 >= 50) {
            throw new UserInfoException("图片大小需小于50k");
        }
        String fileType;
        String[] suffixs = {".jpg", ".gif", ".jpeg", ".png", ".JPG", ".GIF", ".JPEG", ".PNG"};
        if (Arrays.stream(suffixs).noneMatch(suffix -> img.getOriginalFilename().endsWith(suffix))) {
            throw new UserInfoException("图片格式错误");
        } else {
            fileType = img.getOriginalFilename().substring(img.getOriginalFilename().lastIndexOf(
                    "."));
        }

        File imgFile = new File(IMG + user.getEmail() + "_" + UUID.randomUUID() + fileType);
        String filename = null;
        try {
            if (!imgFile.exists()) {
                if (!imgFile.createNewFile()) {
                    throw new UserInfoException("服务器创建文件失败");
                }
            }
            img.transferTo(imgFile);
            if ((filename = MyUtil.CompressImg(img, fileType, imgFile, IMG)) == null) {
                filename = imgFile.getName();
            }
        } catch (IOException e) {
            throw new UserInfoException(e.getMessage());
        }


        try {
            userService.updateAvator(user.getEmail(), "/img/" + filename);
        } catch (Exception e) {
            log.error("更新头像发生错误：{} params：email:{},path:{}", e.getMessage(), user.getEmail(),
                    "/img/" + imgFile.getName());
            throw new UserInfoException("更新头像时发生错误");
        }
        String lastImg = IMG + user.getImg().substring(5);
        if (!"default.jpg".equals(user.getImg().substring(5))) {
            File file = new File(lastImg);
            if (!file.delete()) {
                log.error("旧头像删除失败，路径为：{}" + lastImg);
            }
        }
        msgBuilder.addCookie(response, "img", "/img/" + imgFile.getName());
        msgBuilder.addData("msg", "/img/" + imgFile.getName());
        return msgBuilder.getMsg();
    }

    public ModelAndView getMyHistory(int page) {

        int pageNum = 0;
        int uid = userService.getCurrentUser().getUid();
        List<History> histories;
        MsgBuilder builder = new MsgBuilder();
        histories = userService.getHistories(uid, (page - 1) * ONE_PAGE_HISTORY_NUM, ONE_PAGE_HISTORY_NUM);
        pageNum = userService.getHistoryNum(uid);

        builder.addData("histories", histories);
        builder.addData("currentPage", page);
        builder.addData("pageNum", pageNum);
        return builder.getMsg("user/history");
    }

    @Transactional(noRollbackFor = PostException.class)
    public void delCollect(Integer pid) throws PostException {
        Integer uid = userService.getCurrentUser().getUid();
        //检查是否存在

        if (!userService.collectRelationshipIsExist(uid, pid)) {
            log.warn("用户收藏帖子出现问题：uid：{} pid：{}", uid, pid);
            throw new PostException("未收藏该帖子");
        }
        //删除收藏
        userMapper.delCollect(uid, pid);
        //帖子收藏数-1
        postMapper.decreaseCollectNum(pid);
    }

    @Transactional(noRollbackFor = PostException.class)
    public void collect(Integer pid) throws PostException {
        Integer uid = userService.getCurrentUser().getUid();
        //检查是否存在
        if (userService.collectRelationshipIsExist(uid, pid)) {
            log.warn("用户取消收藏帖子出现问题：uid：{} pid：{}", uid, pid);
            throw new PostException("已收藏该帖子");
        }
        //新建收藏
        userMapper.Collect(uid, pid);
        //帖子收藏数+1
        postMapper.increaseCollectNum(pid);
    }
}
