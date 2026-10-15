package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.AgencyService;
import com.xq.vo.AgencyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 经纪公司管理
 */
@RestController("PublicAgencyController")
@RequestMapping("/pub/agency")
@Slf4j
public class PublicAgencyController {

    @Autowired
    private AgencyService agencyService;

    /**
     * 查询全部经纪公司
     *
     * @return 经纪公司列表
     */
    @GetMapping
    public Result<List<AgencyVO>> listAll() {
        return Result.success(agencyService.listAll());
    }
}
