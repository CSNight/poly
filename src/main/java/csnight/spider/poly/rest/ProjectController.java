package csnight.spider.poly.rest;

import csnight.spider.poly.aop.LogAsync;
import csnight.spider.poly.logic.ShowService;
import csnight.spider.poly.model.OrderPayInfo;
import csnight.spider.poly.rest.dto.OrderDto;
import csnight.spider.poly.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "演出管理API")
@RestController
@RequestMapping(value = "project")
public class ProjectController {
    @Resource
    private ShowService showService;

    @LogAsync
    @ApiOperation(value = "获取演出信息")
    @RequestMapping(value = "/projectInfo", method = RequestMethod.GET)
    public RespTemplate GetProjectInfo(@RequestParam("url") String url) {
        return new RespTemplate(HttpStatus.OK, showService.GetProjectDetail(url));
    }

    @LogAsync
    @ApiOperation(value = "获取场次信息")
    @RequestMapping(value = "/showInfo/{jid}/{pid}", method = RequestMethod.GET)
    public RespTemplate GetShowInfo(@PathVariable String jid, @PathVariable String pid) {
        return new RespTemplate(HttpStatus.OK, showService.GetShowDetail(jid, pid));
    }

    @LogAsync
    @ApiOperation(value = "获取座位列表")
    @RequestMapping(value = "/sectionInfo/{jid}/{showId}/{sectionId}", method = RequestMethod.GET)
    public RespTemplate GetShowInfo(@PathVariable String jid, @PathVariable int showId, @PathVariable String sectionId) {
        return new RespTemplate(HttpStatus.OK, showService.GetSeatList(jid, showId, sectionId));
    }

    @LogAsync
    @ApiOperation(value = "开始抢票")
    @RequestMapping(value = "/startClaw", method = RequestMethod.POST)
    public RespTemplate StartClaw(@Valid @RequestBody OrderDto dto) {
        return new RespTemplate(HttpStatus.OK, showService.StartClaw(dto));
    }

    @LogAsync
    @ApiOperation(value = "停止抢票")
    @RequestMapping(value = "/stopClaw", method = RequestMethod.PUT)
    public RespTemplate StopClaw() {
        return new RespTemplate(HttpStatus.OK, showService.StopClaw());
    }

    @LogAsync
    @ApiOperation(value = "获取付款信息")
    @RequestMapping(value = "/getPay", method = RequestMethod.POST)
    public RespTemplate GetPay(@RequestBody OrderPayInfo payInfo) {
        return new RespTemplate(HttpStatus.OK, showService.GetPayCode(payInfo));
    }

    @LogAsync
    @ApiOperation(value = "检查付款状态")
    @RequestMapping(value = "/checkPay/{orderId}", method = RequestMethod.POST)
    public RespTemplate CheckPayStatus(@PathVariable String orderId) {
        return new RespTemplate(HttpStatus.OK, showService.CheckPay(orderId));
    }

    @LogAsync
    @ApiOperation(value = "取消订单")
    @RequestMapping(value = "/cancelOrder/{orderId}", method = RequestMethod.DELETE)
    public RespTemplate CancelOrder(@PathVariable String orderId) {
        return new RespTemplate(HttpStatus.OK, showService.CancelOrder(orderId));
    }

    @LogAsync
    @ApiOperation(value = "订单详情")
    @RequestMapping(value = "/getPayComplete/{orderId}", method = RequestMethod.POST)
    public RespTemplate PayComplete(@PathVariable String orderId) {
        return new RespTemplate(HttpStatus.OK, showService.GetPayComplete(orderId));
    }
}
