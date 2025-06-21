package com.etledge.database.db.controller;


import com.etledge.common.result.RtnData;
import com.etledge.database.db.form.DbDatabaseForm;
import com.etledge.database.db.service.DbDatabaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * (DbDatabase)表控制层
 *
 * @author mayc
 * @since 2025-06-03 21:22:24
 */
@Api(tags = "数据源管理")
@Validated
@RestController
@RequestMapping(value = "/dbDatabase", produces = {"application/json;charset=utf-8"})
public class DbDatabaseController {

    @Autowired
    private DbDatabaseService dbDatabaseService;

    /**
     * Get database list
     *
     * @param groupId
     * @param dbId
     * @param name
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "获取数据源列表")
    @GetMapping("/list.do")
    public RtnData list(@RequestParam(required = false) Integer groupId,
                        @RequestParam(required = false) Integer dbId,
                        @RequestParam(required = false) String name,
                        @RequestParam(defaultValue = "1") Integer pageNo,
                        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return RtnData.ok(dbDatabaseService.list(groupId, dbId, name, pageNo, pageSize));
    }

    /**
     * 添加数据源
     *
     * @return
     */
    @ApiOperation(value = "添加数据源")
    @PostMapping("/add.do")
    public RtnData add(@RequestBody @Valid DbDatabaseForm form) {
        dbDatabaseService.add(form);
        return RtnData.ok("数据源接入成功!");
    }

    /**
     * 编辑数据源
     *
     * @return
     */
    @ApiOperation(value = "编辑数据源")
    @PutMapping("/update.do")
    public RtnData update(@RequestBody @Valid DbDatabaseForm form) {
        dbDatabaseService.update(form);
        return RtnData.ok("数据源编辑成功!");
    }

    /**
     * 删除数据源
     *
     * @return
     */
    @ApiOperation(value = "删除数据源")
    @PutMapping("/delete.do")
    public RtnData delete(@RequestParam Integer id) {
        dbDatabaseService.delete(id);
        return RtnData.ok("数据源删除成功!");
    }


}

