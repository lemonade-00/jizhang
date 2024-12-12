package ntou.cs.project.Common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "recurring_accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RecurringAccount extends Account {

    public RecurringAccount(Account acc) {
        super(acc.getID(), acc.getRemark(), acc.getCategory(), acc.getAttach(), acc.getAttachURL(), acc.getAccType(),
                acc.getPrice(),
                acc.getUserID(),
                acc.getTime());
    }

    @NotNull
    private String recurrenceType; // 重複週期（日/週/月/年）
    private LocalDate recurrenceEndDate; // 重複結束日期
    private LocalDate lastGenerate;
    private boolean isActive = false;
}