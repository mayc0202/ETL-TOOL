package com.ds.etl.database.db.controller;

import com.ds.etl.common.result.RtnData;
import com.ds.etl.database.db.form.DbGroupForm;
import com.ds.etl.database.db.service.DbGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * (DbGroup) Controller
 *
 * @author mayc
 * @since 2025-05-22 00:00:40
 */
@Api(tags = "数据源分组管理")
@Validated
@RestController
@RequestMapping(value = "dbGroup", produces = {"application/json;charset=utf-8"})
public class DbGroupController {

    @Autowired
    private DbGroupService dbGroupService;

    /**
     * Add group
     *
     * @return
     */
    @ApiOperation(value = "添加数据源分组")
    @PostMapping("add.do")
    public RtnData add(@RequestBody @Valid DbGroupForm form) {
        dbGroupService.add(form);
        return RtnData.ok("Group added successfully!");
    }

    /**
     * Edit group
     *
     * @return
     */
    @ApiOperation(value = "编辑数据源分组")
    @PutMapping("update.do")
    public RtnData update(@RequestBody @Valid DbGroupForm form) {
        dbGroupService.update(form);
        return RtnData.ok("Group updated successfully!");
    }

    /**
     * Delete group
     *
     * @return
     */
    @ApiOperation(value = "删除数据源分组")
    @PutMapping("delete.do")
    public RtnData delete(@RequestParam Integer id) {
        dbGroupService.delete(id);
        return RtnData.ok("Group deleted successfully!");
    }
}

