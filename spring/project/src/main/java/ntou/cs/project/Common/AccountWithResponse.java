package ntou.cs.project.Common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountWithResponse {
    private Account account;
    private RecurringAccount recurringAccount = null;
}
