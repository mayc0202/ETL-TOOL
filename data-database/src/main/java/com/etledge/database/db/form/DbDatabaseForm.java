package com.etledge.database.db.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @Author: yc
 * @CreateTime: 2025-06-03
 * @Description:
 * @Version: 1.0
 */
@Data
public class DbDatabaseForm {

    @ApiModelProperty(value = "数据源id", name = "id", example = "1")
    private Integer id;

    @NotBlank(message = "数据源名称不能为空!")
    @ApiModelProperty(value = "数据源名称", name = "name", example = "xx测试库")
    private String name;

    @NotNull(message = "数据源分组不能为空!")
    @ApiModelProperty(value = "数据源分组", name = "groupId", example = "1")
    private Integer groupId;

    @NotBlank(message = "数据源分层不能为空!")
    @ApiModelProperty(value = "数据源分层", name = "label", example = "1-业务库BIZ；2-贴源库ODS；3-治理库DW；4-应用库ADS；5-共享库DS")
    private String label;

    @NotBlank(message = "数据库大类不能为空!")
    @ApiModelProperty(value = "数据库大类", name = "category", example = "数据库大类:1-关系型数据库；2-非关系型数据库；3-消息型数据库；4-FTP类型; 5-OSS")
    private String category;

    @NotBlank(message = "数据库类型不能为空!")
    @ApiModelProperty(value = "数据库类型", name = "type", example = "数据库类型:MySQL、Oracle、SQLServer、PostgreSQL等")
    private String type;

    @NotBlank(message = "主机地址不能为空!")
    @ApiModelProperty(value = "主机地址", name = "dbHost", example = "127.0.0.1")
    private String dbHost;

    @NotBlank(message = "端口不能为空!")
    @ApiModelProperty(value = "端口", name = "dbPort", example = "8080")
    private String dbPort;

    @NotBlank(message = "数据库名称/SID不能为空!")
    @ApiModelProperty(value = "数据库名称/SID", name = "dbName", example = "db_student")
    private String dbName;

    @ApiModelProperty(value = "模式schema", name = "dbSchema", example = "public")
    private String dbSchema;

    @NotBlank(message = "数据库用户名不能为空!")
    @ApiModelProperty(value = "数据库用户名", name = "username", example = "root")
    private String username;

    @NotBlank(message = "数据库密码不能为空!")
    @ApiModelProperty(value = "数据库密码", name = "password", example = "******")
    private String password;

    @ApiModelProperty(value = "备注", name = "remark", example = "...")
    private String remark;

    @ApiModelProperty(value = "最近同步时间", name = "lastSyncTime", example = "2025-06-04 10:25:36")
    private Date lastSyncTime;

    //数据同步周期
    @ApiModelProperty(value = "数据同步周期", name = "dataSyncCycle", example = "1")
    private Integer dataSyncCycle;

    @ApiModelProperty(value = "属性", name = "properties", example = "...")
    private String properties;

    @ApiModelProperty(value = "扩展配置", name = "extConfig", example = "...")
    private String extConfig;

    @ApiModelProperty(value = "be地址", name = "beAddress", example = "...")
    private String beAddress;

    @ApiModelProperty(value = "fe地址", name = "feAddress", example = "127.0.0.1:9030")
    private String feAddress;

    @ApiModelProperty(value = "连接方式", name = "connectType", example = "127.0.0.1:9030")
    private String connectType;

//    private Boolean useSSL;
//
//    private Boolean useCopy;
//
//    private String authWay;
//
//    private Integer connectionTimeout;
//
//    private Integer readTimeout;
//
//    private String controlEncoding;
//
//    private String mode;
//
//    private String secretId;
//
//    private String secretKey;
//
//    private String odpsEndpoint;
//
//    private String tunnelEndpoint;
//
//    private String project;
//
//    private String accessKeyId;
//
//    private String accessKeySecret;
//
//    private List<PropertiesForm> propertiesList;
//
//    private String shareType;
//
//    private String dbConnectType;
//
//    private String accessKey;
}