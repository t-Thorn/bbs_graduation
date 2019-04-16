package interfaces;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class HotPoint {
    int view;
    int reply;
    long total;

    public HotPoint(long total) {
        this.total = total;
    }
}
