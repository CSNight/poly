package csnight.spider.poly.rest;

import csnight.spider.poly.aop.LogAsync;
import csnight.spider.poly.logic.ShowService;
import csnight.spider.poly.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
