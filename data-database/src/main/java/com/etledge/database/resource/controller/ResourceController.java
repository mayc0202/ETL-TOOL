package com.etledge.database.resource.controller;

import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "资源管理")
@Validated
@RestController
@RequestMapping(value = "/resource", produces = {"application/json;charset=utf-8"})
public class ResourceController {


}
