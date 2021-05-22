package csnight.spider.poly.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import csnight.spider.poly.model.Project;
import csnight.spider.poly.model.ShowDetail;
import csnight.spider.poly.model.TickPrice;
import csnight.spider.poly.utils.HttpUtils;
import csnight.spider.poly.utils.JSONUtils;
import csnight.spider.poly.websocket.WebSocketServer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowService {
    private final String SHOW_INFO = "https://platformpcgateway.polyt.cn/api/1.0/show/getShowInfoDetail";
    private final String PROJECT_DETAIL = "https://platformpcgateway.polyt.cn/api/1.0/ticket/getProjectDetail";
    private final String SHOW_SECTION_INFO = "https://platformpcgateway.polyt.cn/api/1.0/seat/getShowSectionInfo";
    private final String SEAT_INFO = "https://platformpcgateway.polyt.cn/api/1.0/seat/getSeatInfo";
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

    public String GetSeatList(String jid, int showId, String sectionId) {
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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
