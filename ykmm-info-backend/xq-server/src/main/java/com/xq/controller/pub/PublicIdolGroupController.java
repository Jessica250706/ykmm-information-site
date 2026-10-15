package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.IdolGroupService;
import com.xq.vo.IdolGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 偶像团体管理
 */
@RestController("PublicIdolGroupController")
@RequestMapping("/pub/idol-group")
@Slf4j
public class PublicIdolGroupController {

    @Autowired
    private IdolGroupService idolGroupService;

    /**
     * 查询全部偶像团体
     *
     * @return 偶像团体列表
     */
    @GetMapping
    public Result<List<IdolGroupVO>> listAll() {
        return Result.success(idolGroupService.listAll());
    }
}
