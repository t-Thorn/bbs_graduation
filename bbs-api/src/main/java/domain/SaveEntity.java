package domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SaveEntity {
    int pid;

    /**
     * 是否检查阀值
     */
    boolean check;
}
