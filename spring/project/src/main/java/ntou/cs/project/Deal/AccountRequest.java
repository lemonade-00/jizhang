package ntou.cs.project.Deal;

public class AccountRequest {
    private String category;
    private String accType;
    private String remark;
    private String attach;
    private String time;
    private int price;
    private boolean isRecurring = false;
    private String recurrenceType;
    private String recurrenceEndDate;

    public String getCategory() {
        return this.category;
    }

    public int getPrice() {
        return this.price;
    }

    public String getTime() {
        return this.time;
    }

    public String getAttach() {
        return this.attach;
    }

    public String getRemark() {
        return this.remark;
    }

    public String getAccType() {
        return this.accType;
    }

    public String getrecurrenceType() {
        return this.recurrenceType;
    }

    public String getRecurrenceEndDate() {
        return this.recurrenceEndDate;
    }

    public boolean getisRecurring() {
        return this.isRecurring;
    }

}
