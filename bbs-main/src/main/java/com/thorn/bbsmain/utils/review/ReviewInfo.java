package com.thorn.bbsmain.utils.review;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;

@Data
@Builder
public class ReviewInfo implements Serializable {
    int uid;
    int pid;
    int floor;
    String content;

    @Tolerate
    public ReviewInfo() {
    }
}
