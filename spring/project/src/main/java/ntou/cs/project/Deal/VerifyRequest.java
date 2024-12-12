package ntou.cs.project.Deal;

import javax.validation.constraints.NotNull;

public class VerifyRequest {
    @NotNull
    private String code;
    @NotNull
    private String email;
    @NotNull
    private String password;

    public String getCode() {
        return this.code;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
}
