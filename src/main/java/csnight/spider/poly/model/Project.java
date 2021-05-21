package csnight.spider.poly.model;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String projectId;
    private String projectDesp;
    private String url;
    private String productId;
    private String productName;
    private String productSubtypeId;
    private String productSubtypeName;
    private String productTypeId;
    private String productTypeName;
    private String theaterId;
    private String theaterName;
    private String img;
    private int prodTicketMaxPrice;
    private int prodTicketMinPrice;
    private String showStartToEndTime;
    private List<ShowDetail> shows = new ArrayList<>();

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectDesp() {
        return projectDesp;
    }

    public void setProjectDesp(String projectDesp) {
        this.projectDesp = projectDesp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSubtypeId() {
        return productSubtypeId;
    }

    public void setProductSubtypeId(String productSubtypeId) {
        this.productSubtypeId = productSubtypeId;
    }

    public String getProductSubtypeName() {
        return productSubtypeName;
    }

    public void setProductSubtypeName(String productSubtypeName) {
        this.productSubtypeName = productSubtypeName;
    }

    public String getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(String productTypeId) {
        this.productTypeId = productTypeId;
    }

    public String getProductTypeName() {
        return productTypeName;
    }

    public void setProductTypeName(String productTypeName) {
        this.productTypeName = productTypeName;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(String theaterId) {
        this.theaterId = theaterId;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getProdTicketMaxPrice() {
        return prodTicketMaxPrice;
    }

    public void setProdTicketMaxPrice(int prodTicketMaxPrice) {
        this.prodTicketMaxPrice = prodTicketMaxPrice;
    }

    public int getProdTicketMinPrice() {
        return prodTicketMinPrice;
    }

    public void setProdTicketMinPrice(int prodTicketMinPrice) {
        this.prodTicketMinPrice = prodTicketMinPrice;
    }

    public String getShowStartToEndTime() {
        return showStartToEndTime;
    }

    public void setShowStartToEndTime(String showStartToEndTime) {
        this.showStartToEndTime = showStartToEndTime;
    }

    public List<ShowDetail> getShows() {
        return shows;
    }

    public void setShows(List<ShowDetail> shows) {
        this.shows = shows;
    }
}
