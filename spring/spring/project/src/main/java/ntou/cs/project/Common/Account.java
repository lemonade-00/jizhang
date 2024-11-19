package ntou.cs.project.Common;

import lombok.Data;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "accounts")
@Data
public class Account {
    @Id
    private String ID;
    @NotNull
    private LocalDateTime time; // 日期
    @NotNull
    private String category; // 類別
    private String remark; // 備註
    private String accType; // 收入或支出
    private String attach; // 附件
    @NotNull
    private String userID; // 使用者
    @NotNull
    private int price;
}