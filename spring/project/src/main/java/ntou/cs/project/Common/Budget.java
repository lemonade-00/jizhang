package ntou.cs.project.Common;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

import lombok.Data;

@Document(collection = "budget")
@Data
public class Budget {
    @Id
    private String ID;
    @NotNull
    private String userID;
    private String budget;
    private LocalDate startDate;
    private LocalDate endDate;
}
