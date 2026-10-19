package com.xq.controller.admin;

import com.xq.annotation.RequireRole;
import com.xq.constant.UserRoleConstant;
import com.xq.result.Result;
import com.xq.service.CardRtvService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 卡面 RTV 管理
 */
@RestController("AdminCardRtvController")
@RequestMapping("/admin/card-rtv")
@RequireRole(UserRoleConstant.ADMIN)
@Slf4j
public class CardRtvController {

    @Autowired
    private CardRtvService cardRtvService;

    /**
     * 删除
     *
     * @param id 主键
     * @return 统一返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "RTV ID不能为空") Long id) {
        cardRtvService.delete(id);
        return Result.success();
    }
}
