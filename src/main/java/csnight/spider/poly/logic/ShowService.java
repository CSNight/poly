package csnight.spider.poly.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import csnight.spider.poly.model.Project;
import csnight.spider.poly.model.ShowDetail;
import csnight.spider.poly.utils.HttpUtils;
import csnight.spider.poly.utils.JSONUtils;
import csnight.spider.poly.websocket.WebSocketServer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowService {
    private final String SHOW_INFO = "https://platformpcgateway.polyt.cn/api/1.0/show/getShowInfoDetail";
    private final String PROJECT_DETAIL = "https://platformpcgateway.polyt.cn/api/1.0/ticket/getProjectDetail";
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
                    project.getShows().add(detail);
                }
            }
            WebSocketServer.getInstance().broadcast("获取项目信息：" + result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return project.getShows();
    }
}
