package com.thorn.bbsmain.services.oa;

import com.thorn.bbsmain.exceptions.SqlException;
import com.thorn.bbsmain.mapper.ViolationMapper;
import com.thorn.bbsmain.mapper.entity.Sensitivity;
import com.thorn.bbsmain.mapper.entity.Violation;
import com.thorn.bbsmain.services.ReplyService;
import com.thorn.bbsmain.utils.MsgBuilder;
import com.thorn.bbsmain.utils.MyUtil;
import com.thorn.bbsmain.utils.review.ReviewProcess;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViolationService {

    private ViolationMapper violationMapper;

    private ReplyService replyService;

    private ReviewProcess reviewProcess;

    public ViolationService(ViolationMapper violationMapper, ReplyService replyService, ReviewProcess reviewProcess) {
        this.violationMapper = violationMapper;
        this.replyService = replyService;
        this.reviewProcess = reviewProcess;
    }

    public List<Sensitivity> getSensitivities(int page, int step, String search, MsgBuilder builder) {
        search = search == null ? "" : search;
        builder.addData("count", violationMapper.getSensitivitiesNum(search));
        return violationMapper.getSensitivities((page - 1) * step, step, search);
    }

    public String setSensitivityStatus(int id, boolean available) throws SqlException {
        try {
            violationMapper.setSensitivityStatus(id, available);
        } catch (Exception e) {
            throw new SqlException("数据库执行失败");
        }
        return new MsgBuilder().getMsg();
    }

    public List<Violation> getViolations(int page, int step, String search, MsgBuilder builder) {
        if ("".equals(search)) {
            search = null;
        }
        builder.addData("count", violationMapper.getViolationNum(search));
        return violationMapper.getViolations((page - 1) * step, step, search);
    }

    public List<Violation> getViolationRecords(int page, int step, String search, MsgBuilder builder) {
        if ("".equals(search)) {
            search = null;
        }
        builder.addData("count", violationMapper.getViolationRecordNum(search));
        List<Violation> violationRecords = violationMapper.getViolationRecords((page - 1) * step, step, search);
        violationRecords.forEach(v -> {
            v.setPage(MyUtil.getPage(replyService.getReplyOffsetByFloor(v.getPid(), v.getFloor())
                    , replyService.getONE_PAGE_REPLY_NUM()));
            v.setReplyID(replyService.getReplyIDByFloor(v.getPid(), v.getFloor()));
        });
        return violationRecords;
    }

    public String addSensitivityStatus(String word) {
        violationMapper.addSensitivity(word);
        reviewProcess.setNeedToRefresh(true);
        return "";
    }
}
