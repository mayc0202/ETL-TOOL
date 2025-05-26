/*
 Navicat Premium Data Transfer

 Source Server         : LocalDB
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306
 Source Schema         : etl-database

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 23/05/2025 00:05:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for db_basic
-- ----------------------------
DROP TABLE IF EXISTS `db_basic`;
CREATE TABLE `db_basic`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据库名称',
  `img` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT 'logo编码',
  `category_id` int(0) NOT NULL COMMENT '数据库类型id',
  `order_by` int(0) NOT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for db_category
-- ----------------------------
DROP TABLE IF EXISTS `db_category`;
CREATE TABLE `db_category`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据库类型名称',
  `img` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT 'logo编码',
  `order_by` int(0) NOT NULL,
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for db_database
-- ----------------------------
DROP TABLE IF EXISTS `db_database`;
CREATE TABLE `db_database`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `group_id` int(0) NOT NULL COMMENT '数据库分类',
  `category` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据库大类：1-关系型数据库；2-非关系型数据库；3-消息型数据库；4-FTP类型; 5-OSS',
  `type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据库类型：MySQL、Oracle、SQLServer、PostgreSQL等',
  `db_host` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '主机地址',
  `db_port` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '端口',
  `db_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '数据库名称/SID',
  `db_schema` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '模式schema，目前只支持Oracle',
  `username` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据库用户名',
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据库密码',
  `remark` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '备注',
  `is_stats_by_day` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否按天做增量统计。默认值：不需要增量统计',
  `last_sync_time` datetime(0) NULL DEFAULT NULL COMMENT '最近同步时间',
  `data_sync_cycle` int(0) NULL DEFAULT NULL COMMENT '数据同步周期',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否解除：0-未解除；1-已解除，默认未删除',
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人账号',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新人账号',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `properties` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '属性',
  `ext_config` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '扩展配置',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for db_field
-- ----------------------------
DROP TABLE IF EXISTS `db_field`;
CREATE TABLE `db_field`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `table_id` int(0) NOT NULL COMMENT '表ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `business_name` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '业务名称',
  `data_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '数据类型',
  `data_length` int(0) NULL DEFAULT NULL COMMENT '数据长度',
  `description` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '字段描述',
  `nullable` tinyint(1) NOT NULL COMMENT '是否可以为空 0-否，1-是',
  `order_by` int(0) NULL DEFAULT NULL COMMENT '排序字段',
  `precision` int(0) NULL DEFAULT NULL COMMENT '小数点精度',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除，默认0',
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for db_group
-- ----------------------------
DROP TABLE IF EXISTS `db_group`;
CREATE TABLE `db_group`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `order_by` int(0) NOT NULL,
  `deleted` tinyint(1) NOT NULL COMMENT '是否删除，0未删除，1已删除，默认0',
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for db_table
-- ----------------------------
DROP TABLE IF EXISTS `db_table`;
CREATE TABLE `db_table`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `db_id` int(0) NOT NULL COMMENT '数据库ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `business_name` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '业务名称',
  `description` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '描述',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除，0未删除，1已删除，默认0',
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人账号',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新人账号',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rule
-- ----------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule`  (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规则ID(UUID)',
  `group_id` int(0) NOT NULL COMMENT '规则分类ID',
  `rule_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规则代码',
  `rule_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规则名称',
  `rule_description` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '规则描述',
  `rule_processor_id` int(0) NOT NULL COMMENT '规则类型',
  `status` int(0) NOT NULL COMMENT '状态 0-草稿 1-发布',
  `draft_config` mediumtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '草稿配置',
  `released_config` mediumtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '发布后配置',
  `deleted` int(0) NOT NULL COMMENT '删除状态 0-未删除 1-已删除',
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人账号',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '修改人账号',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `rule_type` int(0) NOT NULL COMMENT '参数样式类型 1通用 2公共 3自定义',
  `first_group_Id` int(0) NOT NULL COMMENT '分类最高级的id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rule_group
-- ----------------------------
DROP TABLE IF EXISTS `rule_group`;
CREATE TABLE `rule_group`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '规则分类主键',
  `group_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '分组名称',
  `parent_id` int(0) NULL DEFAULT NULL COMMENT '上级分组ID',
  `order_by` int(0) NOT NULL COMMENT '排序字段',
  `created_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人账号',
  `created_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '修改人账号',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `group_type` int(0) NULL DEFAULT NULL COMMENT '规则类型(0-公共 1-自定义)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for rule_processor
-- ----------------------------
DROP TABLE IF EXISTS `rule_processor`;
CREATE TABLE `rule_processor`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '处理器ID',
  `processor_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规则类型',
  `params_config` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '规则的参数定义',
  `processor_class` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规则处理器Java类的全路径',
  `style_type` int(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
