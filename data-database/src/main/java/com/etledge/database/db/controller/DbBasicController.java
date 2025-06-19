package com.etledge.database.db.controller;


import com.etledge.common.result.RtnData;
import com.etledge.database.db.service.DbBasicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * (DbBasic) Controller
 *
 * @author mayc
 * @since 2025-05-20 23:34:42
 */
@Api(tags = "数据源信息管理")
@RestController
@RequestMapping(value = "/dbBasic", produces = {"application/json;charset=utf-8"})
public class DbBasicController {

    @Autowired
    private DbBasicService dbBasicService;

    /**
     * query database category list
     *
     * @return
     */
    @ApiOperation(value = "获取数据库类型集合")
    @GetMapping("/getDbCategoryList.do")
    public RtnData getDbCategoryList() {
        return RtnData.ok(dbBasicService.getDbCategoryList());
    }

    /**
     * query database basic list
     *
     * @return
     */
    @ApiOperation(value = "获取数据库基础信息集合")
    @GetMapping("/getDbBasicList.do")
    public RtnData getDbBasicList() {
        return RtnData.ok(dbBasicService.getDbBasicList());
    }

    /**
     * query database basic list by category id
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "根据数据库类别id获取数据库基础信息集合")
    @GetMapping("/getDbBasicListByCategoryId.do")
    public RtnData getDbBasicListByCategoryId(@RequestParam Integer id) {
        return RtnData.ok(dbBasicService.getDbBasicListByCategoryId(id));
    }

}

