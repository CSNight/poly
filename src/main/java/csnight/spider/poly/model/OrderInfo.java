package csnight.spider.poly.model;

import com.alibaba.fastjson.JSONArray;

public class OrderInfo {
    private String projectId;
    private int showId;
    private String showTime;
    private JSONArray priceList;
    private String getTicketName;
    private String uuid;
    private String getTicketPhone;
    private String payWay;
    private String watchers;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public JSONArray getPriceList() {
        return priceList;
    }

    public void setPriceList(JSONArray priceList) {
        this.priceList = priceList;
    }

    public String getGetTicketName() {
        return getTicketName;
    }

    public void setGetTicketName(String getTicketName) {
        this.getTicketName = getTicketName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGetTicketPhone() {
        return getTicketPhone;
    }

    public void setGetTicketPhone(String getTicketPhone) {
        this.getTicketPhone = getTicketPhone;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public String getWatchers() {
        return watchers;
    }

    public void setWatchers(String watchers) {
        this.watchers = watchers;
    }
}
