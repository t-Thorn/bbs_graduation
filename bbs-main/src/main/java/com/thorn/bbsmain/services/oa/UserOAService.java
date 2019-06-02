package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.exceptions.UserInfoException;
import com.thorn.bbsmain.mapper.UserMapper;
import com.thorn.bbsmain.mapper.entity.User;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserOAService {
    private UserMapper userMapper;

    public UserOAService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User getUserInfo(int uid) {
        return userMapper.getInfoByID(uid);
    }

    public List getList(int page, String target, int limit, int type, MsgBuilder builder) {
        if (target != null && !"".equals(target)) {
            builder.addData("count", userMapper.getUserNumForTargetOfAdmin(target));
            return userMapper.getUserByNickname((page - 1) * limit, limit, target);
        }
        builder.addData("count", userMapper.getUserNumOfAdmin());
        return userMapper.getUsersOfAdmin((page - 1) * limit, limit);
    }

    public Object getDetail(Object id) {
        return null;
    }

    public void update(Object object) throws UserInfoException {
        User user = (User) object;
        if (userMapper.checkExistOfEmailForUpdate(user.getEmail(), user.getUid()) != 0) {
            throw new UserInfoException("已存在相同的邮箱");
        }

        if (userMapper.checkExistOfNNForUpdate(user.getNickname(), user.getUid()) != 0) {
            throw new UserInfoException("已存在相同的昵称");
        }
        try {
            userMapper.updateInfoByAdmin(user);

        } catch (Exception e) {
            throw new UserInfoException("更新失败：" + e.getMessage());
        }
    }

    public boolean delete(Object id) {
        return false;
    }

    public MsgBuilder addData() {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("code", 0);
        builder.addData("msg", "");
        return builder;
    }
}
