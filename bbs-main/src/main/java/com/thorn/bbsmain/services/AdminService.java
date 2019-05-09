package com.thorn.bbsmain.services;

import com.thorn.bbsmain.services.oa.MessageOAService;
import com.thorn.bbsmain.services.oa.PostOAService;
import com.thorn.bbsmain.services.oa.UserOAService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private PostOAService postOAService;

    private UserOAService userOAService;

    private MessageOAService messageOAService;

    public AdminService(PostOAService postOAService, UserOAService userOAService, MessageOAService messageOAService) {
        this.postOAService = postOAService;
        this.userOAService = userOAService;
        this.messageOAService = messageOAService;
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

    public String getMessageTable(int page, int step) {
        MsgBuilder builder = messageOAService.addData();
        builder.addData("data", messageOAService.getList(page, step, builder));
        return builder.getMsg();
    }

    public String broadcast(String msg) {
        messageOAService.broadcast(msg);
        return "";
    }
}
