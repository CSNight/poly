package csnight.spider.poly.rest;

import csnight.spider.poly.aop.LogAsync;
import csnight.spider.poly.logic.ShowService;
import csnight.spider.poly.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
}
