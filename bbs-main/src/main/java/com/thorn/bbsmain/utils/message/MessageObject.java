package com.thorn.bbsmain.utils.message;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;

@Data
@Builder
public class MessageObject implements Serializable {
    private int uid;
    @Builder.Default
    private String content = "";
    @Builder.Default
    private int num = 0;
    /**
     * 消息类型 0=未读消息 >0为新消息 1=普通消息 2=公告
     */
    @Builder.Default
    private int type = 0;

    @Tolerate
    public MessageObject() {
    }
}
