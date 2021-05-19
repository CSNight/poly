package csnight.spider.poly.rest;

import csnight.spider.poly.logic.UserService;
import csnight.spider.poly.rest.dto.UserDto;
import csnight.spider.poly.utils.RespTemplate;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "user")
public class UserController {
    @Resource
    private UserService userService;

    @ApiOperation(value = "设置登录信息")
    @RequestMapping(value = "/addUserSession", method = RequestMethod.POST)
    public RespTemplate AddLogin(@RequestBody UserDto dto) {
        return new RespTemplate(HttpStatus.OK, userService.AddLoginSession(dto));
    }

    @ApiOperation(value = "检查登录")
    @RequestMapping(value = "/checkLogin", method = RequestMethod.GET)
    public RespTemplate CheckLogin() {
        return new RespTemplate(HttpStatus.OK, userService.CheckLogin());
    }

    @ApiOperation(value = "获取用户")
    @RequestMapping(value = "/getUserLogin", method = RequestMethod.GET)
    public RespTemplate GetLoginUser() {
        return new RespTemplate(HttpStatus.OK, userService.GetLoginUserInfo());
    }

    @ApiOperation(value = "获取观影人列表")
    @RequestMapping(value = "/getObservers", method = RequestMethod.GET)
    public RespTemplate GetObservers() {
        return new RespTemplate(HttpStatus.OK, userService.GetObserverList());
    }
}
