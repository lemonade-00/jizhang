package ntou.cs.project.Common;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "accounts")
@Data
public class Account {
    @Id
    public String ID;
    public LocalDateTime time; // 日期
    public String category; // 類別
    public String remark; // 備註
    public String attach; // 附件
    public int price;
}