package csnight.spider.poly.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class OrderDto {
    @NotNull
    @NotEmpty
    private String projectInfo;
    @NotNull
    @NotEmpty
    private String getTickName;
    @NotNull
    @NotEmpty
    private String showWatcher;
    private int level;
    @Min(1)
    private int showId;
    private String payWay;
    private boolean autoDownGrade;
    private int clazz = 0;

    public String getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(String projectInfo) {
        this.projectInfo = projectInfo;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getGetTickName() {
        return getTickName;
    }

    public void setGetTickName(String getTickName) {
        this.getTickName = getTickName;
    }

    public String getShowWatcher() {
        return showWatcher;
    }

    public void setShowWatcher(String showWatcher) {
        this.showWatcher = showWatcher;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public boolean isAutoDownGrade() {
        return autoDownGrade;
    }

    public void setAutoDownGrade(boolean autoDownGrade) {
        this.autoDownGrade = autoDownGrade;
    }

    public int getClazz() {
        return clazz;
    }

    public void setClazz(int clazz) {
        this.clazz = clazz;
    }
}
