package csnight.spider.poly.rest;

import csnight.spider.poly.aop.LogAsync;
import csnight.spider.poly.logic.UserService;
import csnight.spider.poly.rest.dto.CheckCodeDto;
import csnight.spider.poly.rest.dto.UserDto;
import csnight.spider.poly.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "用户管理API")
@RestController
@RequestMapping(value = "user")
public class UserController {
    @Resource
    private UserService userService;

    @LogAsync
    @ApiOperation(value = "登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public RespTemplate AddLogin(@Valid @RequestBody UserDto dto) {
        return new RespTemplate(HttpStatus.OK, userService.LoginSession(dto));
    }

    @LogAsync
    @ApiOperation(value = "用户检查")
    @RequestMapping(value = "/checkUser", method = RequestMethod.GET)
    public RespTemplate CheckLoginUser() {
        return new RespTemplate(HttpStatus.OK, userService.CheckUser());
    }

    @LogAsync
    @ApiOperation(value = "获取Token")
    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public RespTemplate GetToken() {
        return new RespTemplate(HttpStatus.OK, userService.GetToken());
    }

    @LogAsync
    @ApiOperation(value = "发送验证码")
    @RequestMapping(value = "/sendPhone", method = RequestMethod.POST)
    public RespTemplate SendPhoneCheck(@Valid @RequestBody CheckCodeDto dto) {
        return new RespTemplate(HttpStatus.OK, userService.SendCheckPhone(dto));
    }

    @LogAsync
    @ApiOperation(value = "检查登录")
    @RequestMapping(value = "/checkLogin", method = RequestMethod.GET)
    public RespTemplate CheckLogin() {
        return new RespTemplate(HttpStatus.OK, userService.CheckLogin());
    }

    @LogAsync
    @ApiOperation(value = "获取用户")
    @RequestMapping(value = "/getUserLogin", method = RequestMethod.GET)
    public RespTemplate GetLoginUser() {
        return new RespTemplate(HttpStatus.OK, userService.GetLoginUserInfo());
    }

    @LogAsync
    @ApiOperation(value = "获取观影人列表")
    @RequestMapping(value = "/getObservers", method = RequestMethod.GET)
    public RespTemplate GetObservers() {
        return new RespTemplate(HttpStatus.OK, userService.GetObserverList());
    }
}
