/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50636
Source Host           : localhost:3306
Source Database       : shop-uaa

Target Server Type    : MYSQL
Target Server Version : 50636
File Encoding         : 65001

Date: 2020-12-03 10:33:40
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_login_log
-- ----------------------------
DROP TABLE IF EXISTS `t_login_log`;
CREATE TABLE `t_login_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `phone` bigint(20) DEFAULT NULL,
  `login_ip` varchar(255) DEFAULT NULL,
  `login_time` datetime DEFAULT NULL,
  `state` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_user_info`;
CREATE TABLE `t_user_info` (
  `phone` bigint(20) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `head` varchar(255) DEFAULT NULL,
  `register_ip` varchar(255) DEFAULT NULL,
  `register_time` datetime DEFAULT NULL,
  `birthDay` varchar(255) DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_user_info
-- ----------------------------
INSERT INTO `t_user_info` VALUES ('13088889999', '叩丁狼', '1.jpg', '192.168.20.150', '2020-11-03 15:41:29', '2017-11-1', '这个人很懒，什么都没留下');

-- ----------------------------
-- Table structure for t_user_login
-- ----------------------------
DROP TABLE IF EXISTS `t_user_login`;
CREATE TABLE `t_user_login` (
  `phone` bigint(20) NOT NULL COMMENT '自增id',
  `password` varchar(32) DEFAULT NULL COMMENT '密码',
  `salt` varchar(10) DEFAULT NULL COMMENT '盐',
  PRIMARY KEY (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_user_login
-- ----------------------------
INSERT INTO `t_user_login` VALUES ('13088889999', 'f2266f92e31d032983464ea05d6fb4e4', '1a2b3c4d5e');
