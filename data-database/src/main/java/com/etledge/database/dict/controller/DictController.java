package com.etledge.database.dict.controller;

import com.etledge.common.result.RtnData;
import com.etledge.database.dict.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "字典管理")
@Validated
@RestController
@RequestMapping(value = "/dict", produces = {"application/json;charset=utf-8"})
public class DictController {

    @Autowired
    private DictService dictService;

    /**
     * 获取数据源分层字典
     *
     * @return
     */
    @ApiOperation(value = "获取字典")
    @GetMapping("/getDict.do")
    public RtnData getDict(@RequestParam String dictCode) {
        return RtnData.ok(dictService.getDict(dictCode));
    }
}
