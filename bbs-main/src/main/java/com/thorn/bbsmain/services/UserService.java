package com.thorn.bbsmain.services;


import com.thorn.bbsmain.confugurations.shiro.jwt.JWTToken;
import com.thorn.bbsmain.exceptions.UserException;
import com.thorn.bbsmain.exceptions.UserInfoException;
import com.thorn.bbsmain.mapper.UserMapper;
import com.thorn.bbsmain.mapper.entity.Post;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {


    private UserMapper userMapper;

    public UserService(@Autowired UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ModelAndView userReg(User user, BindingResult result, String repass,
                                HttpServletResponse response) throws UserException {
        MsgBuilder builder = new MsgBuilder();
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
            return builder.getMsg("/user/reg");
        }


        createNewUser(user);
        userLogin(user, response, builder);
        return builder.getMsg("redirect:/");
    }

    /**
     * 向shiro请求登录
     *
     * @param user 登录的user
     * @return
     */
    public String verifyTokenByShiro(Subject currentUser, User user) {

        String token = JWTUtil.sign(user.getEmail(), user.getPassword());
        currentUser.login(new JWTToken(token));
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

    public void createNewUser(User user) throws UserException {
        //检验user合法性
        isEmailExist(user);
        isNicknameExist(user);
        try {
            userMapper.createNewUser(user);
            userMapper.grantToNewUser(user.getEmail());
        } catch (Exception e) {
            //重新封装错误投出
            log.error("创建用户错误： user:{} error:{}", user, e.getMessage());
            throw new UserException("内部错误");
        }
    }

    public void isNicknameExist(User user) throws UserException {
        if (userMapper.checkExistOfNN(user.getNickname()) > 0 && !getCurrentUser().getNickname().equals(user.getNickname())) {
            throw new UserException("昵称已存在");
        }
    }

    private void isEmailExist(User user) throws UserException {
        if (userMapper.checkExistOfEmail(user.getEmail()) > 0) {
            throw new UserException("用户名已存在");
        }
    }

    public void userLogin(User user, HttpServletResponse response, MsgBuilder builder) {
        Subject currentUser = SecurityUtils.getSubject();
        String token = verifyTokenByShiro(currentUser, user);
        //向客户端cookie中加入token
        builder.addCookie(response, "token", token);
        builder.addCookie(response, "nickname", ((User) currentUser.getPrincipal()).getNickname());
        builder.addCookie(response, "img",
                ((User) currentUser.getPrincipal()).getImg());
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


        System.out.println();
        return (User) currentUser.getPrincipal();
    }

    public void updatePassword(String email, String password) throws UserInfoException {
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

    public ModelAndView buildUserHome(Integer uid) throws Exception {
        MsgBuilder builder = new MsgBuilder();
        User user = getCurrentUser();
        Integer from = user == null ? null : user.getUid();
        if (!Optional.ofNullable(uid).isPresent()) {
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
        builder.addData("posts", getUserPost(uid));
        builder.addData("replys", getUserReply(uid));
        return builder.getMsg("/user/home");
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
    public void createRelationship(int to) throws Exception {
        userMapper.createRelationship(getCurrentUser().getUid(), to);
    }

    /**
     * 取消关注用户
     *
     * @param to
     */
    public void delRelationship(int to) throws Exception {
        userMapper.delRelationship(getCurrentUser().getUid(), to);
    }

    private List<Post> getUserPost(int uid) {
        return userMapper.getUserPost(uid);
    }

    private List<Reply> getUserReply(int uid) {
        return userMapper.getUserReply(uid);
    }

    public void addPostNum() {
        userMapper.addPostNum(getCurrentUser().getUid());
    }
}
