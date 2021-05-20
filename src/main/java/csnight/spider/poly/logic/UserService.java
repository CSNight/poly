package csnight.spider.poly.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import csnight.spider.poly.model.PolyUser;
import csnight.spider.poly.model.UserObserver;
import csnight.spider.poly.rest.dto.CheckCodeDto;
import csnight.spider.poly.rest.dto.UserDto;
import csnight.spider.poly.utils.HttpUtils;
import csnight.spider.poly.utils.JSONUtils;
import csnight.spider.poly.websocket.WebSocketServer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private PolyUser polyUser = new PolyUser();
    private final String LOGIN = "https://platformpcgateway.polyt.cn/api/1.0/login/fastLogin";
    private final String SEND_PHONE = "https://platformpcgateway.polyt.cn/api/1.0/login/checkCodeAndSendMsg";
    private final String CHECK_LOGIN = "https://platformpcgateway.polyt.cn/api/1.0/login/checkLogin";
    private final String GET_LOGIN_USER = "https://platformpcgateway.polyt.cn/api/1.0/login/getLoginUser";
    private final String GET_OBSERVER_LIST = "https://platformpcgateway.polyt.cn/api/1.0/member/getObserverList";
    private final String GET_TOKEN = "https://platformpcgateway.polyt.cn/api/1.0/common/getToken";

    public UserService() {
        HttpUtils.reqProcessor("https://polyt.cn", "GET", "", new JSONObject());
    }

    public String LoginSession(UserDto dto) {
        JSONObject body = JSONObject.parseObject(JSONUtils.pojo2json(dto));
        body.put("token", polyUser.getToken());
        polyUser.setPhone(dto.getPhone());
        String res = HttpUtils.reqProcessor(LOGIN, "POST", "passport", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            String result = resultObj.getString("msg").equals("OK") ? "success" : "failed";
            WebSocketServer.getInstance().broadcast("登录：" + result);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        WebSocketServer.getInstance().broadcast("登录失败");
        return "failed";
    }

    public String CheckUser() {
        String check = this.CheckLogin();
        WebSocketServer.getInstance().broadcast("登录检查：" + check);
        String check1 = this.GetLoginUserInfo();
        WebSocketServer.getInstance().broadcast("获取用户信息：" + check1);
        List<UserObserver> observers = this.GetObserverList();
        WebSocketServer.getInstance().broadcast("获取观影人列表：" + check1);
        return check.equals("success") && check1.equals("success") && observers.size() > 0 ? "success" : "failed";
    }

    public String GetToken() {
        if (polyUser.getToken() != null) {
            return polyUser.getToken();
        }
        String res = HttpUtils.reqProcessor(GET_TOKEN, "POST", "passport", new JSONObject());
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            polyUser.setToken(resultObj.getString("data"));
            if (polyUser.getToken() == null) {
                return null;
            }
            WebSocketServer.getInstance().broadcast("获取Token：" + polyUser.getToken());
            return polyUser.getToken();
        } catch (Exception ex) {
            WebSocketServer.getInstance().broadcast("获取Token失败：" + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public String SendCheckPhone(CheckCodeDto dto) {
        JSONObject body = JSONObject.parseObject(JSONUtils.pojo2json(dto));
        body.put("checkModel", JSONObject.parseObject(dto.getCheckModel()));
        body.put("token", polyUser.getToken());
        String res = HttpUtils.reqProcessor(SEND_PHONE, "POST", "passport", body);
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            return resultObj.getBooleanValue("data") ? "success" : "failed";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String CheckLogin() {
        String res = HttpUtils.reqProcessor(CHECK_LOGIN, "POST", "index", new JSONObject());
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            return resultObj.getJSONObject("data").getBooleanValue("hasLogin") ? "success" : "failed";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String GetLoginUserInfo() {
        String res = HttpUtils.reqProcessor(GET_LOGIN_USER, "POST", "index", new JSONObject());
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            JSONObject data = resultObj.getJSONObject("data");
            polyUser.setAccount(data.getString("account"));
            polyUser.setHaveNoPayOrder(data.getBooleanValue("haveNoPayOrder"));
            polyUser.setHeadImg(data.getString("headImg"));
            polyUser.setNikeName(data.getString("nikeName"));
            return "success";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public List<UserObserver> GetObserverList() {
        String res = HttpUtils.reqProcessor(GET_OBSERVER_LIST, "POST", "index", new JSONObject());
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            JSONArray data = resultObj.getJSONArray("data");
            polyUser.getWatchers().clear();
            if (data != null) {
                data.forEach(e -> {
                    polyUser.getWatchers().add(JSONUtils.json2pojo(e.toString(), UserObserver.class));
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return polyUser.getWatchers();
    }
}
