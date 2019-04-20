package interfaces;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class HotPoint implements Serializable {
    int view;
    int reply;
    long total;

    //浏览增量
    int viewIncrement;

    //回复增量
    int replyIncrement;

    //变化量，阀值更新的重要条件
    int changNum;

    public HotPoint(long total) {
        this.total = total;
    }

}
