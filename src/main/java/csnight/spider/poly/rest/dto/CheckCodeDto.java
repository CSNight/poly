package csnight.spider.poly.rest.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CheckCodeDto {
    @NotNull
    @NotEmpty
    private String checkModel;
    private String code;
    private boolean loginFlag;
    @NotNull
    @NotEmpty
    private String phone;
    @NotNull
    @NotEmpty
    private String phoneArea = "86";
    private String phoneCode;

    public String getCheckModel() {
        return checkModel;
    }

    public void setCheckModel(String checkModel) {
        this.checkModel = checkModel;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isLoginFlag() {
        return loginFlag;
    }

    public void setLoginFlag(boolean loginFlag) {
        this.loginFlag = loginFlag;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneArea() {
        return phoneArea;
    }

    public void setPhoneArea(String phoneArea) {
        this.phoneArea = phoneArea;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
}
