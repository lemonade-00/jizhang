package ntou.cs.project.Deal;

import javax.validation.constraints.NotNull;

public class BudgetRequest {
    private String budget;
    private String startDate;
    private String endDate;

    @NotNull
    public String getBudget() {
        return this.budget;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public String getEndDate() {
        return this.endDate;
    }
}
