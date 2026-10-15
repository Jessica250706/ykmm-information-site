package com.xq.controller.pub;

import com.xq.result.Result;
import com.xq.service.PersonService;
import com.xq.vo.PersonOptionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人物管理
 */
@RestController("PublicPersonController")
@RequestMapping("/pub/person")
@Slf4j
public class PublicPersonController {

    @Autowired
    private PersonService personService;

    /**
     * 查询所有人物
     */
    @GetMapping("/options")
    public Result<List<PersonOptionVO>> options() {
        return Result.success(personService.listOptions());
    }
}
