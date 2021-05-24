package csnight.spider.poly.model;

public class OrderPayInfo {
    //订单价格
    private double actuallyPaidAmt;
    //订单号
    private String id;
    //订单过期时间
    private long orderExpireTime;
    //订单号
    private String orderId;
    private String orderStatus;
    private String payId;
    private String payWayCode;
    private String salesOrderCode;
    private int supportPurse;
    private String theaterId;

    public double getActuallyPaidAmt() {
        return actuallyPaidAmt;
    }

    public void setActuallyPaidAmt(double actuallyPaidAmt) {
        this.actuallyPaidAmt = actuallyPaidAmt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getOrderExpireTime() {
        return orderExpireTime;
    }

    public void setOrderExpireTime(long orderExpireTime) {
        this.orderExpireTime = orderExpireTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public String getPayWayCode() {
        return payWayCode;
    }

    public void setPayWayCode(String payWayCode) {
        this.payWayCode = payWayCode;
    }

    public String getSalesOrderCode() {
        return salesOrderCode;
    }

    public void setSalesOrderCode(String salesOrderCode) {
        this.salesOrderCode = salesOrderCode;
    }

    public int getSupportPurse() {
        return supportPurse;
    }

    public void setSupportPurse(int supportPurse) {
        this.supportPurse = supportPurse;
    }

    public String getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(String theaterId) {
        this.theaterId = theaterId;
    }
}
