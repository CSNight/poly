package csnight.spider.poly.model;

import java.util.ArrayList;
import java.util.List;

public class PolyUser {
    private String phone;
    private String token;
    private String account;
    private boolean haveNoPayOrder;
    private String headImg;
    private String nikeName;
    private String cookie;
    private List<UserObserver> watchers = new ArrayList<>();

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isHaveNoPayOrder() {
        return haveNoPayOrder;
    }

    public void setHaveNoPayOrder(boolean haveNoPayOrder) {
        this.haveNoPayOrder = haveNoPayOrder;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public List<UserObserver> getWatchers() {
        return watchers;
    }

    public void setWatchers(List<UserObserver> watchers) {
        this.watchers = watchers;
    }
}
