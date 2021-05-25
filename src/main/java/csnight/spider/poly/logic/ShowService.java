package csnight.spider.poly.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import csnight.spider.poly.model.*;
import csnight.spider.poly.rest.dto.OrderDto;
import csnight.spider.poly.utils.HttpUtils;
import csnight.spider.poly.utils.JSONUtils;
import csnight.spider.poly.websocket.WebSocketServer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {
    private final String SHOW_INFO = "https://platformpcgateway.polyt.cn/api/1.0/show/getShowInfoDetail";
    private final String PROJECT_DETAIL = "https://platformpcgateway.polyt.cn/api/1.0/ticket/getProjectDetail";
    private final String SHOW_SECTION_INFO = "https://platformpcgateway.polyt.cn/api/1.0/seat/getShowSectionInfo";
    private final String SEAT_INFO = "https://platformpcgateway.polyt.cn/api/1.0/seat/getSeatInfo";
    private final String COMMIT_ON_SEAT = "https://platformpcgateway.polyt.cn/api/1.0/platformOrder/commitOrderOnSeat";
    private final String CREATE_JUMP = "https://platformpcgateway.polyt.cn/api/1.0/platformOrder/createQuickOrderJump";
    private final String CREATE_ORDER = "https://platformpcgateway.polyt.cn/api/1.0/platformOrder/createOrder";
    private final String GET_PAY_CODE = "https://platformpcgateway.polyt.cn/api/1.0/unionpay/getUnionPayQrCode";
    private final String CHECK_PAY = "https://platformpcgateway.polyt.cn/api/1.0/unionpay/checkOrderTicketStatus";
    private final String CANCEL_ORDER = "https://platformpcgateway.polyt.cn/api/1.0/myOrder/cancelOrder";
    private final String GET_PAY_COMPLETE = "https://platformpcgateway.polyt.cn/api/1.0/unionpay/orderInfoForPayComplete";
    private Project project = null;

    public Project GetProjectDetail(String url) {
        String[] part = url.split("/");
        if (part.length < 4) {
            return null;
        }
        JSONObject body = new JSONObject();
        String jid = part[part.length - 3];
        String pid = part[part.length - 1];
        body.put("productId", pid);
        body.put("projectId", jid);
        String res = HttpUtils.reqProcessor(PROJECT_DETAIL, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            if (result.equals("success")) {
                project = JSONObject.parseObject(resultObj.getString("data"), Project.class);
                project.setUrl(url);
                project.setProjectId(jid);
                project.setProductId(pid);
                this.GetShowDetail(jid, pid);
            }
            WebSocketServer.getInstance().broadcast("获取项目信息：" + result);
            return project;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        WebSocketServer.getInstance().broadcast("获取项目信息失败");
        return null;
    }

    public List<ShowDetail> GetShowDetail(String jid, String pid) {
        JSONObject body = new JSONObject();
        body.put("productId", pid);
        body.put("projectId", jid);
        String res = HttpUtils.reqProcessor(SHOW_INFO, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            JSONObject data = resultObj.getJSONObject("data");
            if (result.equals("success") && data != null) {
                JSONArray shows = data.getJSONArray("platShowInfoDetailVOList");
                for (Object show : shows) {
                    ShowDetail detail = JSONUtils.json2pojo(show.toString(), ShowDetail.class);
                    detail.setProjectId(project.getProjectId());
                    detail.getTicketPriceList().sort((o1, o2) -> {
                        if (o1.getTotalPrices() == o2.getTotalPrices()) {
                            return 0;
                        }
                        return o1.getTotalPrices() > o2.getTotalPrices() ? -1 : 1;
                    });
                    GetShowSections(detail);
                    project.getShows().add(detail);
                }
            }
            WebSocketServer.getInstance().broadcast("获取项目信息：" + result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return project.getShows();
    }

    public void GetShowSections(ShowDetail show) {
        JSONObject body = new JSONObject();
        body.put("showId", show.getShowId());
        String res = HttpUtils.reqProcessor(SHOW_SECTION_INFO, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            JSONObject data = resultObj.getJSONObject("data");
            if (result.equals("success") && data != null) {
                JSONArray sectionList = data.getJSONArray("sectionList");
                if (sectionList.size() > 0) {
                    show.setCname(sectionList.getJSONObject(0).getString("cname"));
                    show.setSectionCode(sectionList.getJSONObject(0).getString("sectionCode"));
                }
                show.setCategoryId(data.getString("categoryId"));
                JSONArray priceGrades = data.getJSONArray("priceGradeList");
                for (TickPrice tickPrice : show.getTicketPriceList()) {
                    for (int i = 0; i < priceGrades.size(); i++) {
                        JSONObject priceGrade = priceGrades.getJSONObject(i);
                        if (tickPrice.getPriceId() == priceGrade.getIntValue("ticketPriceId")) {
                            tickPrice.setTicketPriceColor(priceGrade.getString("ticketPriceColor"));
                            tickPrice.setPriceGrade(priceGrade.getString("priceGrade"));
                            tickPrice.setPriceGradeShow(priceGrade.getString("priceGradeShow"));
                        }
                    }
                }
            }
            WebSocketServer.getInstance().broadcast("获取项目信息：" + result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<SeatInfo> GetSeatList(String jid, int showId, String sectionId) {
        List<SeatInfo> seatInfos = new ArrayList<>();
        JSONObject body = new JSONObject();
        body.put("projectId", jid);
        body.put("showId", showId);
        body.put("sectionId", sectionId);
        String res = HttpUtils.reqProcessor(SEAT_INFO, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            JSONObject data = resultObj.getJSONObject("data");
            if (result.equals("success") && data != null) {
                JSONArray shows = data.getJSONArray("seatList");
                for (Object show : shows) {
                    SeatInfo seat = JSONUtils.json2pojo(show.toString(), SeatInfo.class);
                    if (seat.getSeatStatus() == 0) {
                        seatInfos.add(seat);
                    }
                }
                return seatInfos;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String CommitOnSeat(OrderInfo order) {
        JSONObject body = new JSONObject();
        body.put("channelId", "");
        body.put("priceList", order.getPriceList());
        body.put("projectId", order.getProjectId());
        body.put("seriesId", "");
        body.put("showId", order.getShowId());
        body.put("showTime", order.getShowTime());
        String res = HttpUtils.reqProcessor(COMMIT_ON_SEAT, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            String data = resultObj.getString("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data != null) {
                order.setUuid(data);
                return "success";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String CreateJump(OrderInfo orderInfo) {
        JSONObject body = new JSONObject();
        body.put("uuid", orderInfo.getUuid());
        String res = HttpUtils.reqProcessor(CREATE_JUMP, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            JSONObject data = resultObj.getJSONObject("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data != null) {
                orderInfo.setGetTicketPhone(data.getString("defaultPhone"));
                return "success";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String CreateOrder(OrderInfo info) {
        JSONObject body = new JSONObject();
        body.put("channelId", null);
        body.put("consignee", info.getGetTicketName());
        body.put("consigneePhonr", info.getGetTicketPhone());
        body.put("deliveryWay", "01");
        body.put("movieIds", info.getWatchers());
        body.put("orderFreightAmt", 0);
        body.put("payWayCode", info.getPayWay());
        body.put("seriesId", "");
        body.put("uuid", info.getUuid());
        String res = HttpUtils.reqProcessor(CREATE_ORDER, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            String data = resultObj.getString("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data != null) {
                return data;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String GetPayCode(OrderPayInfo payInfo) {
        JSONObject body = new JSONObject();
        body.put("isRecharge", "0");
        body.put("orderId", payInfo.getOrderId());
        body.put("returnUrl", "https://www.polyt.cn/paySuccess/" + payInfo.getOrderId() + "/0");
        String res = HttpUtils.reqProcessor(GET_PAY_CODE, "POST", "pay", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            JSONObject data = resultObj.getJSONObject("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data != null) {
                WebSocketServer.getInstance().broadcast(data.toString());
                return data.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String CheckPay(String orderId) {
        JSONObject body = new JSONObject();
        body.put("orderId", orderId);
        body.put("isRecharge", "0");
        String res = HttpUtils.reqProcessor(CHECK_PAY, "POST", "pay", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            boolean data = resultObj.getBooleanValue("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data) {
                WebSocketServer.getInstance().broadcast("支付成功");
                return "success";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String CancelOrder(String orderId) {
        JSONObject body = new JSONObject();
        body.put("orderId", orderId);
        String res = HttpUtils.reqProcessor(CANCEL_ORDER, "POST", "member", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            boolean data = resultObj.getBooleanValue("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data) {
                WebSocketServer.getInstance().broadcast("订单取消");
                return "success";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public JSONObject GetPayComplete(String orderId) {
        JSONObject body = new JSONObject();
        body.put("orderId", orderId);
        body.put("isRecharge", "0");
        String res = HttpUtils.reqProcessor(GET_PAY_COMPLETE, "POST", "detail", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getBooleanValue("success") ? "success" : "failed";
            JSONObject data = resultObj.getJSONObject("data");
            if (result.equals("success") && resultObj.getIntValue("code") == 200 && data != null) {
                return data;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String StartClaw(OrderDto dto) {
        if (project == null) {
            project = JSONObject.parseObject(dto.getProjectInfo(), Project.class);
        }
        if (project != null) {
            for (ShowDetail show : project.getShows()) {
                if (show.getShowId() == dto.getShowId()) {
                    ClawBus.getIns().setShowDetail(show);
                    ClawBus.getIns().StartClaw(dto);
                    return "success";
                }
            }
        }
        WebSocketServer.getInstance().broadcast("抢票启动失败");
        return "failed";
    }

    public String StopClaw() {
        ClawBus.getIns().StopClaw();
        return "success";
    }
}
