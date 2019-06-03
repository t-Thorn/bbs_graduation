package com.thorn.bbsmain.services;


import com.thorn.bbsmain.confugurations.shiro.jwt.JWTToken;
import com.thorn.bbsmain.exceptions.PageException;
import com.thorn.bbsmain.exceptions.UserException;
import com.thorn.bbsmain.exceptions.UserInfoException;
import com.thorn.bbsmain.exceptions.UserRegException;
import com.thorn.bbsmain.mapper.ReplyMapper;
import com.thorn.bbsmain.mapper.UserMapper;
import com.thorn.bbsmain.mapper.entity.*;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {


    private UserMapper userMapper;

    @Value("${system.page.post}")
    int replyStep;
    private ReplyMapper replyMapper;

    public ModelAndView userReg(User user, BindingResult result, String uri, String repass,
                                HttpServletRequest request, HttpServletResponse response) throws UserRegException {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("uri", uri);
        user.setNickname(user.getNickname().replace(" ", ""));
        //规范验证
        boolean hasError = false;
        if (repass == null || !repass.equals(user.getPassword())) {
            builder.addData("repassError", "第二次输入的密码不正确");
            hasError = true;
        }
        if (result.hasErrors()) {
            getErrors(result, builder);
            hasError = true;
        }
        if (hasError) {

            return builder.getMsg("user/reg");
        }


        createNewUser(user, uri);
        return userLogin(user, uri, request, response, builder);
    }

    /**
     * 向shiro请求登录
     *
     * @param user 登录的user
     * @return
     */
    private String verifyTokenByShiro(Subject currentUser, User user) throws UserException {
        if (userMapper.checkValidOfEmail(user.getEmail()) == 0) {
            throw new UserException("不存在或该用户不可用");
        }
        String token = JWTUtil.sign(user.getEmail(), user.getPassword());
        try {
            currentUser.login(new JWTToken(token));
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
        return token;
    }

    /**
     * 删除用户信息
     *
     * @param response
     */
    public void delUserCookie(HttpServletResponse response) {
        Cookie nickname = new Cookie("nickname", "");
        nickname.setPath("/");
        nickname.setMaxAge(0);
        response.addCookie(nickname);
        Cookie token = new Cookie("token", "");
        token.setPath("/");
        token.setMaxAge(0);
        response.addCookie(token);
        Cookie img = new Cookie("img", "");
        img.setPath("/");
        img.setMaxAge(0);
        response.addCookie(img);
    }

    public UserService(@Autowired UserMapper userMapper, ReplyMapper replyMapper) {
        this.userMapper = userMapper;
        this.replyMapper = replyMapper;
    }

    void isNicknameExist(User user) throws UserException {
        if (userMapper.checkExistOfNN(user.getNickname()) > 0 && !getCurrentUser().getNickname().equals(user.getNickname())) {
            throw new UserException("昵称已存在");
        }
    }


    private void createNewUser(User user, String url) throws UserRegException {

        //检验user合法性
        if (userMapper.checkExistOfEmail(user.getEmail()) > 0) {
            throw new UserRegException("用户名已存在." + url);
        }
        if (userMapper.checkExistOfNN(user.getNickname()) > 0) {
            throw new UserRegException("昵称已存在." + url);
        }
        try {
            userMapper.createNewUser(user);
            userMapper.grantToNewUser(user.getEmail());
        } catch (Exception e) {
            //重新封装错误投出
            log.error("创建用户错误： user:{} error:{}", user, e.getMessage());
            throw new UserRegException("内部错误." + url);
        }
    }

    public void getErrors(BindingResult result, MsgBuilder builder) {
        FieldError usernameError = result.getFieldError("email");
        if (usernameError != null) {
            builder.addData("usernameError", usernameError.getDefaultMessage());
        }
        FieldError passwordError = result.getFieldError("password");
        if (passwordError != null) {
            builder.addData("passwordError", passwordError.getDefaultMessage());
        }
        FieldError nicknameError = result.getFieldError("nickname");
        if (nicknameError != null) {
            builder.addData("nnError", nicknameError.getDefaultMessage());
        }
        FieldError ageError = result.getFieldError("age");
        if (ageError != null) {
            builder.addData("ageError", ageError.getDefaultMessage());
        }
    }

    /**
     * 从shiro中取出当前的用户
     *
     * @return
     */
    public User getCurrentUser() {
        Subject currentUser = SecurityUtils.getSubject();
        return (User) currentUser.getPrincipal();
    }

    /**
     * 从shiro缓存中判断当前用户是否存在角色
     *
     * @return
     */
    boolean hasRole(String roleName) {
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.hasRole(roleName);
    }

    void updatePassword(String email, String password) throws UserInfoException {
        try {
            userMapper.updatePassword(email, password);
        } catch (Exception e) {
            throw new UserInfoException("更新密码失败");
        }
    }

    public void logout(HttpServletResponse response) {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();

        delUserCookie(response);
    }

    public ModelAndView userLogin(User user, String uri, HttpServletRequest request,
                                  HttpServletResponse response,
                                  MsgBuilder builder) {
        Subject currentUser = SecurityUtils.getSubject();
        String token;
        String url = MyUtil.getReferer(request);
        if (url.contains("/user/login") || url.contains("/user/reg")) {
            //放回之前的页面 主页或者帖子详情页
            if (!"".equals(uri)) {
                uri = uri.split(",")[0];
                if ("home".equals(uri)) {
                    uri = "/";
                } else {
                    //检测是否是数字
                    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                    if (pattern.matcher(uri).matches()) {
                        uri = "/post/" + uri;
                    } else {
                        uri = "/";
                    }
                }
            } else {
                uri = "/";
            }
        } else {
            uri = url;
        }
        try {
            token = verifyTokenByShiro(currentUser, user);
        } catch (UserException e) {
            builder.addData("errorMsg", "登录失败：" + e.getMessage());
            builder.addData("uri", uri);
            return builder.getMsg("redirect:/user/login");
        }
        //向客户端cookie中加入token
        builder.addCookie(response, "token", token);
        builder.addCookie(response, "nickname", ((User) currentUser.getPrincipal()).getNickname());
        builder.addCookie(response, "img",
                ((User) currentUser.getPrincipal()).getImg());

        //跳转到登录前的页面
        return builder.getMsg("redirect:" + uri);
    }

    public ModelAndView buildUserHome(Integer uid, int page, int rpage) throws Exception {
        if (page < 1 || rpage < 1) {
            throw new PageException("页码参数错误");
        }
        MsgBuilder builder = new MsgBuilder();
        User user = getCurrentUser();
        Integer from = user == null ? null : user.getUid();
        if (!Optional.ofNullable(uid).isPresent()) {
            if (from == null) {
                throw new UserInfoException("请登录后使用");
            }
            uid = from;
            builder.addData("myself", 1);
        }
        isUidExist(uid);
        builder.addData("userInfo", getUserInfoOfHome(uid));

        if (from != null && !from.equals(uid)) {
            builder.addData("myself", 0);
            //是否已关注
            builder.addData("fan", isAlreadyFan(from, uid));
        } else {
            builder.addData("myself", 1);
        }

        builder.addData("posts", getUserPost(uid, page - 1));
        builder.addData("postNum", userMapper.getUserPostNum(uid));
        List<Reply> userReplys = getUserReply(uid, rpage - 1);
        //设置回复所在的页面数（为了定位）
        userReplys.forEach(v -> {
            v.setPage(MyUtil.getPage(replyMapper.getOffsetByFloor(v.getPostid(), v.getFloor()),
                    replyStep));
        });
        builder.addData("replys", userReplys);
        builder.addData("replyNum", userMapper.getUserReplyNum(uid));
        builder.addData("page", page);
        builder.addData("rpage", rpage);
        builder.addData("uid", uid);
        return builder.getMsg("user/home");
    }

    /**
     * 获取用户空间中的信息
     *
     * @param uid
     * @return
     */
    private User getUserInfoOfHome(Integer uid) {
        return userMapper.getUserInfoOfHome(uid);
    }

    private void isUidExist(int uid) throws UserException {
        if (userMapper.checkExistOfUid(uid) != 1) {
            throw new UserException("访问的用户不存在");
        }
    }

    /**
     * 检查关注关系
     *
     * @param from
     * @param to
     * @return
     */
    private boolean isAlreadyFan(int from, int to) {
        return userMapper.isFan(from, to);
    }

    /**
     * 关注用户
     *
     * @param to
     */
    public void createRelationship(int to) {
        userMapper.createRelationship(getCurrentUser().getUid(), to);
    }

    /**
     * 取消关注用户
     *
     * @param to
     */
    public void delRelationship(int to) {
        userMapper.delRelationship(getCurrentUser().getUid(), to);
    }

    private List<Post> getUserPost(int uid, int page) {
        return userMapper.getUserPost(uid, page * 10);
    }

    private List<Reply> getUserReply(int uid, int rpage) {
        return userMapper.getUserReply(uid, rpage * 10);
    }

    void increasePostNum() {
        userMapper.addPostNum(getCurrentUser().getUid());
    }

    public void increasePostNum(int uid) {
        userMapper.addPostNum(uid);
    }

    void updateBasicInfo(User user) {
        userMapper.updateBasicInfo(user);
    }

    List<Message> getMessages(Integer uid, int page, int step) {
        return userMapper.getMessages(uid, page, step);
    }

    int getMessageNum(Integer uid) {
        return userMapper.getMessageNum(uid);
    }

    User getInfo(String email) {
        return userMapper.getInfo(email);
    }

    void updateAvator(String email, String s) {
        userMapper.updateAvator(email, s);
    }

    List<History> getHistories(Integer uid, int offset, int step) {
        return userMapper.getHistories(uid, offset, step);
    }

    int getHistoryNum(int uid) {
        return userMapper.getHistoryNum(uid);
    }

    boolean collectRelationshipIsExist(Integer uid, Integer pid) {
        return userMapper.collectRelationshipIsExist(uid, pid) > 0;
    }

    void createHistory(Integer uid, Integer pid, String title) {
        if (userMapper.hasHistory(uid, pid) > 0) {
            userMapper.updateHistory(uid, pid, new Date());
        } else {
            userMapper.createHistory(uid, pid, title);
        }
    }

    public void decreasePostnum(Integer uid) {
        userMapper.decreasePostnum(uid);
    }

    /**
     * 获取用户状态
     *
     * @param uid 用户id
     * @return 是否启用
     */
    boolean getStatus(Integer uid) {
        return userMapper.getStatus(uid);
    }
}
