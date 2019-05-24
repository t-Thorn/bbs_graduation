package com.thorn.bbsmain.utils.review;

import com.thorn.bbsmain.mapper.ViolationMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewProcess {
    //敏感词列表
    private static List<String> sensitivities = null;
    private ViolationMapper violationMapper;
    /**
     * 提供一个接口给外部提交更新请求
     */
    private boolean needToRefresh = true;

    public ReviewProcess(ViolationMapper violationMapper) {
        this.violationMapper = violationMapper;
    }

    public void setNeedToRefresh(boolean needToRefresh) {
        this.needToRefresh = needToRefresh;
    }

    @RabbitListener(queues = "review")
    @RabbitHandler
    public void process(ReviewInfo msg) {
        if (needToRefresh || sensitivities == null) {
            RefreshSensitivities();
            needToRefresh = false;
        }
        String content = msg.getContent();
        StringBuffer words = new StringBuffer();
        List<String> wordList =
                sensitivities.parallelStream().filter(content::contains).collect(Collectors.toList());
        wordList.forEach(word -> {
            if (words.length() > 0) {
                words.append(";");
            }
            words.append(word);
        });
        if (words.length() > 0) {
            //包含敏感词
            violationMapper.addRecordOfViolation(msg.getUid(), msg.getPid(), msg.getFloor(), words.toString());
        }
    }

    private void RefreshSensitivities() {
        sensitivities = violationMapper.getSensitiveWord();
    }
}
