package csnight.spider.poly.model;

import java.util.ArrayList;
import java.util.List;

public class ShowDetail {
    private int sectionNum;
    private int showId;
    private int venueId;
    private String venueName;
    private int freeTicketCount;
    private String saleBeginTimeStr;
    private String theaterId;
    private String sectionId;
    private String categoryId;
    //section name
    private String cname;
    private String sectionCode;
    private long count;
    private String showTime;
    private long saleBeginTime;
    private long saleEngTime;
    private String checkMode;
    private String status;
    private List<TickPrice> ticketPriceList = new ArrayList<>();

    public int getSectionNum() {
        return sectionNum;
    }

    public void setSectionNum(int sectionNum) {
        this.sectionNum = sectionNum;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public int getFreeTicketCount() {
        return freeTicketCount;
    }

    public void setFreeTicketCount(int freeTicketCount) {
        this.freeTicketCount = freeTicketCount;
    }

    public String getSaleBeginTimeStr() {
        return saleBeginTimeStr;
    }

    public void setSaleBeginTimeStr(String saleBeginTimeStr) {
        this.saleBeginTimeStr = saleBeginTimeStr;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(String theaterId) {
        this.theaterId = theaterId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public long getSaleBeginTime() {
        return saleBeginTime;
    }

    public void setSaleBeginTime(long saleBeginTime) {
        this.saleBeginTime = saleBeginTime;
    }

    public long getSaleEngTime() {
        return saleEngTime;
    }

    public void setSaleEngTime(long saleEngTime) {
        this.saleEngTime = saleEngTime;
    }

    public String getCheckMode() {
        return checkMode;
    }

    public void setCheckMode(String checkMode) {
        this.checkMode = checkMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TickPrice> getTicketPriceList() {
        return ticketPriceList;
    }

    public void setTicketPriceList(List<TickPrice> ticketPriceList) {
        this.ticketPriceList = ticketPriceList;
    }
}
