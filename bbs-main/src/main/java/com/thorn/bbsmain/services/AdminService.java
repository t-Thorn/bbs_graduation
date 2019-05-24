package com.thorn.bbsmain.services;

import com.thorn.bbsmain.services.oa.MessageOAService;
import com.thorn.bbsmain.services.oa.PostOAService;
import com.thorn.bbsmain.services.oa.UserOAService;
import com.thorn.bbsmain.services.oa.ViolationService;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private PostOAService postOAService;

    private UserOAService userOAService;

    private MessageOAService messageOAService;

    private ViolationService violationService;

    public AdminService(PostOAService postOAService, UserOAService userOAService,
                        MessageOAService messageOAService, ViolationService violationService) {
        this.postOAService = postOAService;
        this.userOAService = userOAService;
        this.messageOAService = messageOAService;
        this.violationService = violationService;
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

    public String getMessageDetail(int id) {
        return (String) messageOAService.getDetail(id);
    }

    public String getSensitivities(int page, int step, String search) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("code", 0);
        builder.addData("msg", "");
        builder.addData("data", violationService.getSensitivities(page, step, search, builder));
        return builder.getMsg();
    }

    public String getViolation(int page, int step, String search) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("code", 0);
        builder.addData("msg", "");
        builder.addData("data", violationService.getViolations(page, step, search, builder));
        return builder.getMsg();
    }

    public String getViolationRecord(int page, int step, String search) {
        MsgBuilder builder = new MsgBuilder();
        builder.addData("code", 0);
        builder.addData("msg", "");
        builder.addData("data", violationService.getViolationRecords(page, step, search, builder));
        return builder.getMsg();
    }
}
