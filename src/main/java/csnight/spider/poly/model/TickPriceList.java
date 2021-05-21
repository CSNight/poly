package csnight.spider.poly.model;

public class TickPriceList {
    private double totalPrices;
    //折扣
    private double discount;
    //
    private int reservedCount;
    private int saleclassId;
    private String showStatus;
    private double basisPrice;
    private int showId;
    private double price;
    private long endTime;
    private int priceId;
    private long applyTime;

    public double getTotalPrices() {
        return totalPrices;
    }

    public void setTotalPrices(double totalPrices) {
        this.totalPrices = totalPrices;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getReservedCount() {
        return reservedCount;
    }

    public void setReservedCount(int reservedCount) {
        this.reservedCount = reservedCount;
    }

    public int getSaleclassId() {
        return saleclassId;
    }

    public void setSaleclassId(int saleclassId) {
        this.saleclassId = saleclassId;
    }

    public String getShowStatus() {
        return showStatus;
    }

    public void setShowStatus(String showStatus) {
        this.showStatus = showStatus;
    }

    public double getBasisPrice() {
        return basisPrice;
    }

    public void setBasisPrice(double basisPrice) {
        this.basisPrice = basisPrice;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }

    public long getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(long applyTime) {
        this.applyTime = applyTime;
    }
}
