package com.thorn.bbsmain.services;

import com.thorn.bbsmain.services.oa.PostOAService;
import com.thorn.bbsmain.services.oa.ReplyOAService;
import com.thorn.bbsmain.services.oa.UserOAService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private PostOAService postOAService;

    private UserOAService userOAService;

    private ReplyOAService replyOAService;

    public AdminService(PostOAService postOAService, UserOAService userOAService, ReplyOAService replyOAService) {
        this.postOAService = postOAService;
        this.userOAService = userOAService;
        this.replyOAService = replyOAService;
    }


    public String getPostTable(int page, String search, int limit, int type) {
        MsgBuilder builder = postOAService.addData();
        builder.addData("data", postOAService.getList(page, search, limit, type, builder));
        return builder.getMsg();
    }

    public String getUserTable(int page, String search, int limit, int type) {
        MsgBuilder builder = userOAService.addData();
        builder.addData("data", userOAService.getList(page, search, limit, type, builder));
        return builder.getMsg();
    }
}
