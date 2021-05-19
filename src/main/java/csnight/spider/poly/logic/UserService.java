package csnight.spider.poly.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import csnight.spider.poly.model.PolyUser;
import csnight.spider.poly.model.UserObserver;
import csnight.spider.poly.rest.dto.UserDto;
import csnight.spider.poly.utils.HttpUtils;
import csnight.spider.poly.utils.JSONUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private String userSession = "";
    private PolyUser polyUser = new PolyUser();
    private final String CHECK_LOGIN = "https://platformpcgateway.polyt.cn/api/1.0/login/checkLogin";
    private final String GET_LOGIN_USER = "https://platformpcgateway.polyt.cn/api/1.0/login/getLoginUser";
    private final String GET_OBSERVER_LIST = "https://platformpcgateway.polyt.cn/api/1.0/member/getObserverList";

    public String getUserSession() {
        return userSession;
    }

    public String AddLoginSession(UserDto dto) {
        this.userSession = dto.getCookie();
        polyUser.setPhone(dto.getPhone());
        return this.CheckLogin();
    }

    public String CheckLogin() {
        String res = HttpUtils.reqProcessor(CHECK_LOGIN, "POST", new JSONObject());
        try {
            JSONObject resultObj = JSONObject.parseObject(res);
            return resultObj.getJSONObject("data").getBooleanValue("hasLogin") ? "success" : "failed";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "failed";
    }

    public String GetLoginUserInfo() {
        String res = HttpUtils.reqProcessor(GET_LOGIN_USER, "POST", new JSONObject());
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
        String res = HttpUtils.reqProcessor(GET_OBSERVER_LIST, "POST", new JSONObject());
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
