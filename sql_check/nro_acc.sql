/*
 Navicat Premium Data Transfer

 Source Server         : nro
 Source Server Type    : MySQL
 Source Server Version : 100427
 Source Host           : localhost:3306
 Source Schema         : nro_acc

 Target Server Type    : MySQL
 Target Server Version : 100427
 File Encoding         : 65001

 Date: 25/11/2025 22:49:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` timestamp NULL DEFAULT current_timestamp,
  `update_time` timestamp NULL DEFAULT current_timestamp,
  `ban` tinyint(1) NOT NULL DEFAULT 0,
  `is_admin` tinyint(1) NOT NULL DEFAULT 0,
  `last_time_login` timestamp NOT NULL DEFAULT '2002-07-31 00:00:00',
  `last_time_logout` timestamp NOT NULL DEFAULT '2002-07-31 00:00:00',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `active` int NOT NULL DEFAULT 1,
  `thoi_vang` int NOT NULL DEFAULT 0,
  `server_login` int NOT NULL DEFAULT -1,
  `bd_player` double NULL DEFAULT 1,
  `is_gift_box` tinyint(1) NULL DEFAULT 0,
  `gift_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0',
  `reward` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `cash` int NOT NULL DEFAULT 0,
  `danap` int NOT NULL DEFAULT 0,
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `xsrf_token` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `newpass` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `luotquay` int NOT NULL DEFAULT 0,
  `vang` bigint NOT NULL DEFAULT 0,
  `event_point` int NOT NULL DEFAULT 0,
  `vip` int NOT NULL DEFAULT 0,
  `vip1` int NOT NULL DEFAULT 0,
  `vip2` int NOT NULL DEFAULT 2,
  `sotien` int NOT NULL DEFAULT 0,
  `diem_da_nhan` int NOT NULL DEFAULT 0,
  `hasReceivedVIP` int NOT NULL DEFAULT 0,
  `hasReceivedVIP1` int NOT NULL DEFAULT 0,
  `hasReceivedVIP2` int NOT NULL DEFAULT 0,
  `lastTimeReceivedVIP` bigint NOT NULL DEFAULT 0,
  `lastTimeReceivedVIP1` bigint NOT NULL DEFAULT 0,
  `lastTimeReceivedVIP2` bigint NOT NULL DEFAULT 0,
  `coin` int NOT NULL DEFAULT 0,
  `gioithieu` int NOT NULL DEFAULT 0,
  `admin` int NOT NULL DEFAULT 0,
  `tichdiem` int NOT NULL DEFAULT 0,
  `mkc2` int NOT NULL DEFAULT 0,
  `gmail` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `server` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_acc_login`(`username` ASC, `password` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1025553 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of account
-- ----------------------------
INSERT INTO `account` VALUES (1, '1', '1', '2025-04-16 19:22:49', '2025-04-16 19:22:49', 0, 0, '2025-11-25 12:52:46', '2025-11-25 12:53:00', '127.0.0.1', 1, 0, -1, 1, 0, '0', '[]', 0, 5000000, '', '', '', 0, 0, 0, 0, 0, 2, 5030000, 5000, 1, 0, 1, 1751103619722, 0, 1751105119085, 1000000, 0, 0, 0, 0, '', 0);

-- ----------------------------
-- Table structure for adminpanel
-- ----------------------------
DROP TABLE IF EXISTS `adminpanel`;
CREATE TABLE `adminpanel`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `trangthai` enum('bao_tri','hoat_dong') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `android` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `iphone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `windows` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `java` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `apikey` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `taikhoanmb` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `stkmb` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tenmb` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of adminpanel
-- ----------------------------
INSERT INTO `adminpanel` VALUES (1, 'https://localhost/', '../image/logo.gif', '', 'Ngọc Rồng Mobi - Trang Chủ', '1', '2', '3', '4', '8667F6AF5193D289A214C17E36672887', '', '', '');

-- ----------------------------
-- Table structure for clan
-- ----------------------------
DROP TABLE IF EXISTS `clan`;
CREATE TABLE `clan`  (
  `id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `NAME_2` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `slogan` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `img_id` int NOT NULL DEFAULT 0,
  `power_point` bigint NOT NULL DEFAULT 0,
  `max_member` smallint NOT NULL DEFAULT 10,
  `clan_point` int NOT NULL DEFAULT 0,
  `LEVEL` int NOT NULL DEFAULT 1,
  `members` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `tops` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of clan
-- ----------------------------
INSERT INTO `clan` VALUES (1, 'Dndjd', '', '', 7, 0, 10, 0, 1, '[\"{\\\"role\\\":0,\\\"receive_donate\\\":0,\\\"member_point\\\":0,\\\"body\\\":14,\\\"join_time\\\":1746314848,\\\"leg\\\":15,\\\"head\\\":127,\\\"ask_pea_time\\\":0,\\\"name\\\":\\\"owiiwo\\\",\\\"clan_point\\\":0,\\\"id\\\":1023160,\\\"donate\\\":0,\\\"power\\\":55124}\"]', 'cc', '2025-05-04 06:27:28');

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of event
-- ----------------------------
INSERT INTO `event` VALUES (1, 'LUNNAR_NEW_YEAR', '{\"damePrecent\":0,\"hpPrecent\":0,\"mpPrecent\":0,\"papPrecent\":0}');

-- ----------------------------
-- Table structure for giftcode
-- ----------------------------
DROP TABLE IF EXISTS `giftcode`;
CREATE TABLE `giftcode`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `count_left` int NOT NULL,
  `detail` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `allGender` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `datecreate` timestamp NOT NULL DEFAULT current_timestamp ON UPDATE CURRENT_TIMESTAMP,
  `expired` timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 117 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of giftcode
-- ----------------------------
INSERT INTO `giftcode` VALUES (1, 'nromobi', 93804, '[{\"id\":343,\"quantity\":5,\"options\":[{\"param\":0,\"id\":83}]},{\"id\":380,\"quantity\":20,\"options\":[{\"param\":0,\"id\":30}]},{\"id\":1240,\"quantity\":1,\"options\":[{\"param\":10,\"id\":50},{\"param\":10,\"id\":77},{\"param\":10,\"id\":103},{\"param\":11,\"id\":101},{\"param\":0,\"id\":30}]}]', 'all', '2025-05-06 13:17:34', '2025-12-20 13:39:08');

-- ----------------------------
-- Table structure for history_transaction
-- ----------------------------
DROP TABLE IF EXISTS `history_transaction`;
CREATE TABLE `history_transaction`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `player_2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `item_player_1` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `item_player_2` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bag_1_before_tran` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bag_2_before_tran` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bag_1_after_tran` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bag_2_after_tran` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `time_tran` timestamp NOT NULL DEFAULT current_timestamp,
  `detail_gold_1` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `detail_gold_2` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43075 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of history_transaction
-- ----------------------------

-- ----------------------------
-- Table structure for mbbank_log
-- ----------------------------
DROP TABLE IF EXISTS `mbbank_log`;
CREATE TABLE `mbbank_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `benAccountName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `accountNo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bankName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mbbank_log
-- ----------------------------

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of messages
-- ----------------------------

-- ----------------------------
-- Table structure for momo_trans
-- ----------------------------
DROP TABLE IF EXISTS `momo_trans`;
CREATE TABLE `momo_trans`  (
  `ID` int NOT NULL,
  `tranId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `io` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `partnerId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `status` int NOT NULL,
  `partnerName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `amount` int NOT NULL,
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `millisecond` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of momo_trans
-- ----------------------------

-- ----------------------------
-- Table structure for napthe
-- ----------------------------
DROP TABLE IF EXISTS `napthe`;
CREATE TABLE `napthe`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_nap` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `telco` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `serial` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount` int NOT NULL,
  `status` int NOT NULL,
  `request_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of napthe
-- ----------------------------
INSERT INTO `napthe` VALUES (105, 'meo', 'VIETTEL', '10010747233563', '711684349936730', 10000, 1, '773832097', '2025-05-06 09:00:30');

-- ----------------------------
-- Table structure for naptien
-- ----------------------------
DROP TABLE IF EXISTS `naptien`;
CREATE TABLE `naptien`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `type` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `sotien` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `seri` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `loaithe` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `time` timestamp NULL DEFAULT current_timestamp,
  `noidung` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `tinhtrang` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `tranid` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `magioithieu` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 120 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of naptien
-- ----------------------------

-- ----------------------------
-- Table structure for payments
-- ----------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `refNo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `date` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount` int NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bank` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 285 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payments
-- ----------------------------
INSERT INTO `payments` VALUES (283, '1', '2503', '2025-05-05 00:00:00', 10000, '1', NULL);
INSERT INTO `payments` VALUES (284, '1', '2502', '2025-05-05 00:00:00', 10000, '1', NULL);

-- ----------------------------
-- Table structure for player
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_id` int NULL DEFAULT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `head` int NOT NULL DEFAULT 102,
  `gender` int NOT NULL,
  `have_tennis_space_ship` tinyint(1) NULL DEFAULT 0,
  `clan_id` int NOT NULL DEFAULT -1,
  `data_inventory` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_location` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_point` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_magic_tree` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `items_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `items_bag` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `items_box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `items_box_lucky_round` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `items_daban` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `friends` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `enemies` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_intrinsic` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_item_time` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `devndung_time` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_task` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_mabu_egg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_charm` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `skills` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `skills_shortcut` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pet` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_black_ball` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_side_task` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `data_danh_hieu` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp,
  `notify` text CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL,
  `baovetaikhoan` varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '[]',
  `captcha` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `data_card` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `lasttimepkcommeson` bigint NOT NULL DEFAULT 0,
  `bandokhobau` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `conduongrandoc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `doanhtrai` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `masterDoesNotAttack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nhanthoivang` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `ruonggo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `sieuthanthuy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `vodaisinhtu` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '[]',
  `rongxuong` bigint NOT NULL DEFAULT 0,
  `data_item_event` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `data_luyentap` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `data_clan_task` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `data_vip` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `rank` int NOT NULL DEFAULT 0,
  `data_achievement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `giftcode` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `danh_hieu_shop` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[0,0,0,0,0,0,0,0]',
  `data_clan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `firstTimeLogin` timestamp NOT NULL DEFAULT current_timestamp,
  `buarandom` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[1,1]',
  `dien_sukien` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[0,0,0]',
  `banhtet` bigint NOT NULL DEFAULT 0,
  `banhchung` bigint NOT NULL DEFAULT 0,
  `hoc_ky_nang` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `boughtSkills` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `checkNhanQua` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `lucky_spins` int NOT NULL DEFAULT 0,
  `arena_wins` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account_id`(`account_id` ASC) USING BTREE,
  INDEX `idx_player_name`(`name` ASC) USING BTREE,
  INDEX `idx_player_clan`(`clan_id` ASC) USING BTREE,
  CONSTRAINT `player_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1023276 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of player
-- ----------------------------
INSERT INTO `player` VALUES (1023155, 1025406, 'admin1', 'Vàng tươi: 2.000 (2 k) | Thỏi vàng: 0 (Bag: 0 - Box: 0) | Ngọc rồng 3s: 0 (Bag: 0 - Box: 0) | Ngọc rồng 4s: 0 (Bag: 0 - Box: 0)', 9, 1, 0, -1, '[20000000000,205785,428072,992469,0]', '[4,444,264]', '[0,2349907318,25248888,1000,1000,100000,200000,10271,0,0,0,104800,8150]', '[6,15,0,1746312210175,1746245510000]', '[\"[154,1,\\\"[\\\\\\\"[47,60]\\\\\\\",\\\\\\\"[131,1]\\\\\\\",\\\\\\\"[143,1]\\\\\\\",\\\\\\\"[30,1]\\\\\\\"]\\\",1746338527340]\",\"[159,1,\\\"[\\\\\\\"[6,4800]\\\\\\\",\\\\\\\"[27,1800]\\\\\\\",\\\\\\\"[132,1]\\\\\\\",\\\\\\\"[144,1]\\\\\\\",\\\\\\\"[30,1]\\\\\\\"]\\\",1746338530837]\",\"[-1,0,\\\"[]\\\",1764042956684]\",\"[28,1,\\\"[\\\\\\\"[7,15]\\\\\\\",\\\\\\\"[131,0]\\\\\\\",\\\\\\\"[143,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[107,2]\\\\\\\",\\\\\\\"[50,6]\\\\\\\",\\\\\\\"[102,2]\\\\\\\"]\\\",1746254529651]\",\"[12,1,\\\"[\\\\\\\"[14,1]\\\\\\\",\\\\\\\"[131,0]\\\\\\\",\\\\\\\"[143,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[107,4]\\\\\\\",\\\\\\\"[50,12]\\\\\\\",\\\\\\\"[102,4]\\\\\\\"]\\\",1746227729917]\",\"[-1,0,\\\"[]\\\",1764042956684]\",\"[531,2,\\\"[\\\\\\\"[50,100000]\\\\\\\",\\\\\\\"[108,100]\\\\\\\"]\\\",1746244414709]\",\"[-1,0,\\\"[]\\\",1764042956684]\",\"[-1,0,\\\"[]\\\",1764042956684]\",\"[-1,0,\\\"[]\\\",1764042956684]\",\"[-1,0,\\\"[]\\\",1764042956684]\",\"[-1,0,\\\"[]\\\",1764042956684]\"]', '[\"[521,1,\\\"[\\\\\\\"[1,545]\\\\\\\"]\\\",1746152468856]\",\"[454,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746252318283]\",\"[167,1,\\\"[\\\\\\\"[7,6000]\\\\\\\",\\\\\\\"[28,1200]\\\\\\\",\\\\\\\"[130,1]\\\\\\\",\\\\\\\"[142,1]\\\\\\\",\\\\\\\"[30,1]\\\\\\\"]\\\",1746338537991]\",\"[194,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746221262610]\",\"[220,8,\\\"[\\\\\\\"[71,0]\\\\\\\"]\\\",1746221564042]\",\"[457,97727,\\\"[\\\\\\\"[100,1]\\\\\\\",\\\\\\\"[86,0]\\\\\\\"]\\\",1746221447870]\",\"[821,16,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746324805874]\",\"[750,1,\\\"[\\\\\\\"[86,0]\\\\\\\"]\\\",1746222112253]\",\"[1032,28,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746222121614]\",\"[279,1,\\\"[\\\\\\\"[14,10]\\\\\\\",\\\\\\\"[132,1]\\\\\\\",\\\\\\\"[144,1]\\\\\\\",\\\\\\\"[30,1]\\\\\\\"]\\\",1746338542669]\",\"[1461,1,\\\"[\\\\\\\"[50,26]\\\\\\\",\\\\\\\"[77,25]\\\\\\\",\\\\\\\"[103,25]\\\\\\\",\\\\\\\"[148,45]\\\\\\\",\\\\\\\"[157,15]\\\\\\\",\\\\\\\"[229,15]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[227,0]\\\\\\\"]\\\",1746221174320]\",\"[563,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[23,57]\\\\\\\",\\\\\\\"[87,0]\\\\\\\"]\\\",1746327658254]\",\"[561,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[14,15]\\\\\\\",\\\\\\\"[87,0]\\\\\\\"]\\\",1746327658254]\",\"[1082,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746251922558]\",\"[562,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[0,3657]\\\\\\\",\\\\\\\"[87,0]\\\\\\\"]\\\",1746327658254]\",\"[16,4,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746255490529]\",\"[28,1,\\\"[\\\\\\\"[7,15]\\\\\\\",\\\\\\\"[130,0]\\\\\\\",\\\\\\\"[142,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746254908200]\",\"[567,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[23,61]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746250644743]\",\"[706,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746255248631]\",\"[1082,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746251927258]\",\"[1083,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746224219870]\",\"[464,1,\\\"[\\\\\\\"[50,24]\\\\\\\",\\\\\\\"[117,15]\\\\\\\",\\\\\\\"[148,25]\\\\\\\",\\\\\\\"[77,24]\\\\\\\"]\\\",1746245160416]\",\"[261,1,\\\"[\\\\\\\"[0,2150]\\\\\\\",\\\\\\\"[132,1]\\\\\\\",\\\\\\\"[144,1]\\\\\\\",\\\\\\\"[30,1]\\\\\\\"]\\\",1746338534039]\",\"[1083,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746224246725]\",\"[749,1,\\\"[\\\\\\\"[86,0]\\\\\\\"]\\\",1746254552698]\",\"[20,4,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746245155214]\",\"[1083,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746224289360]\",\"[1461,1,\\\"[\\\\\\\"[50,29]\\\\\\\",\\\\\\\"[77,29]\\\\\\\",\\\\\\\"[103,29]\\\\\\\",\\\\\\\"[148,45]\\\\\\\",\\\\\\\"[157,15]\\\\\\\",\\\\\\\"[5,15]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[227,0]\\\\\\\"]\\\",1746338504627]\",\"[1082,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746224302174]\",\"[222,2,\\\"[\\\\\\\"[69,0]\\\\\\\"]\\\",1746254506589]\",\"[1468,94,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746338519642]\",\"[1158,1,\\\"[\\\\\\\"[30,0]\\\\\\\"]\\\",1746224591572]\",\"[28,1,\\\"[\\\\\\\"[7,15]\\\\\\\",\\\\\\\"[132,0]\\\\\\\",\\\\\\\"[144,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746227936186]\",\"[225,14,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746227927188]\",\"[1139,18,\\\"[\\\\\\\"[30,0]\\\\\\\"]\\\",1746224655629]\",\"[1138,19,\\\"[\\\\\\\"[30,0]\\\\\\\"]\\\",1746224656489]\",\"[64,6,\\\"[\\\\\\\"[2,16]\\\\\\\"]\\\",1746300663659]\",\"[1306,40,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746227572174]\",\"[22,1,\\\"[\\\\\\\"[0,3]\\\\\\\",\\\\\\\"[132,0]\\\\\\\",\\\\\\\"[144,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746227581667]\",\"[28,1,\\\"[\\\\\\\"[7,15]\\\\\\\",\\\\\\\"[130,0]\\\\\\\",\\\\\\\"[142,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746227595686]\",\"[28,1,\\\"[\\\\\\\"[7,15]\\\\\\\",\\\\\\\"[130,0]\\\\\\\",\\\\\\\"[142,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746227617723]\",\"[442,3,\\\"[\\\\\\\"[96,5]\\\\\\\"]\\\",1746227651765]\",\"[221,4,\\\"[\\\\\\\"[70,0]\\\\\\\"]\\\",1746227659784]\",\"[223,3,\\\"[\\\\\\\"[68,0]\\\\\\\"]\\\",1746227663811]\",\"[751,2,\\\"[\\\\\\\"[86,0]\\\\\\\"]\\\",1746227723895]\",\"[1324,93,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746250453557]\",\"[441,3,\\\"[\\\\\\\"[95,5]\\\\\\\"]\\\",1746227748902]\",\"[1341,146,\\\"[\\\\\\\"[30,1]\\\\\\\"]\\\",1746249144125]\",\"[443,3,\\\"[\\\\\\\"[97,5]\\\\\\\"]\\\",1746227770633]\",\"[748,3,\\\"[\\\\\\\"[86,0]\\\\\\\"]\\\",1746227784988]\",\"[12,1,\\\"[\\\\\\\"[14,1]\\\\\\\",\\\\\\\"[132,0]\\\\\\\",\\\\\\\"[144,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746254804236]\",\"[445,6,\\\"[\\\\\\\"[99,3]\\\\\\\"]\\\",1746227843067]\",\"[556,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[22,54]\\\\\\\",\\\\\\\"[87,0]\\\\\\\"]\\\",1746327658254]\",\"[28,1,\\\"[\\\\\\\"[7,15]\\\\\\\",\\\\\\\"[130,0]\\\\\\\",\\\\\\\"[142,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746254850285]\",\"[22,1,\\\"[\\\\\\\"[0,3]\\\\\\\",\\\\\\\"[132,0]\\\\\\\",\\\\\\\"[144,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746227857072]\",\"[703,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746254818083]\",\"[446,3,\\\"[\\\\\\\"[100,5]\\\\\\\"]\\\",1746254481625]\",\"[382,2,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746253884293]\",\"[444,3,\\\"[\\\\\\\"[98,3]\\\\\\\"]\\\",1746227974249]\",\"[1228,97,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746338550031]\",\"[565,1,\\\"[\\\\\\\"[23,39]\\\\\\\",\\\\\\\"[209,1]\\\\\\\",\\\\\\\"[21,18]\\\\\\\",\\\\\\\"[87,0]\\\\\\\",\\\\\\\"[107,2]\\\\\\\"]\\\",1746253087830]\",\"[1099,98,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746325053897]\",\"[1467,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746338509187]\",\"[-1,0,\\\"[]\\\",1764042956688]\",\"[-1,0,\\\"[]\\\",1764042956688]\",\"[-1,0,\\\"[]\\\",1764042956688]\",\"[-1,0,\\\"[]\\\",1764042956688]\",\"[-1,0,\\\"[]\\\",1764042956688]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\"]', '[\"[64,90,\\\"[\\\\\\\"[2,16]\\\\\\\"]\\\",1746245521071]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956689]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\",\"[-1,0,\\\"[]\\\",1764042956690]\"]', '[\"[189,21000,\\\"[]\\\",1764042956690]\",\"[745,1,\\\"[\\\\\\\"[50,10]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,10]\\\\\\\"]\\\",1764042956690]\",\"[189,35000,\\\"[]\\\",1764042956690]\",\"[189,10000,\\\"[]\\\",1764042956690]\",\"[189,22000,\\\"[]\\\",1764042956690]\",\"[189,49000,\\\"[]\\\",1764042956690]\",\"[469,1,\\\"[\\\\\\\"[94,8]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,5]\\\\\\\"]\\\",1764042956690]\",\"[468,1,\\\"[\\\\\\\"[77,8]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,18]\\\\\\\"]\\\",1764042956690]\",\"[189,30000,\\\"[]\\\",1764042956690]\",\"[189,34000,\\\"[]\\\",1764042956690]\",\"[189,10000,\\\"[]\\\",1764042956690]\",\"[189,5000,\\\"[]\\\",1764042956690]\",\"[189,22000,\\\"[]\\\",1764042956690]\",\"[745,1,\\\"[\\\\\\\"[80,9]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,6]\\\\\\\"]\\\",1764042956690]\",\"[189,37000,\\\"[]\\\",1764042956690]\",\"[1000,1,\\\"[\\\\\\\"[94,6]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,4]\\\\\\\"]\\\",1764042956690]\",\"[189,30000,\\\"[]\\\",1764042956690]\",\"[189,46000,\\\"[]\\\",1764042956690]\",\"[189,45000,\\\"[]\\\",1764042956690]\",\"[189,16000,\\\"[]\\\",1764042956690]\",\"[189,39000,\\\"[]\\\",1764042956690]\",\"[189,10000,\\\"[]\\\",1764042956690]\",\"[585,2,\\\"[]\\\",1764042956690]\",\"[467,1,\\\"[\\\\\\\"[50,9]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,17]\\\\\\\"]\\\",1764042956690]\",\"[189,36000,\\\"[]\\\",1764042956691]\",\"[224,1,\\\"[]\\\",1764042956691]\",\"[804,1,\\\"[\\\\\\\"[5,8]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,14]\\\\\\\"]\\\",1764042956691]\",\"[189,41000,\\\"[]\\\",1764042956691]\",\"[189,36000,\\\"[]\\\",1764042956691]\",\"[800,1,\\\"[\\\\\\\"[77,10]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,7]\\\\\\\"]\\\",1764042956691]\",\"[189,32000,\\\"[]\\\",1764042956691]\",\"[223,5,\\\"[]\\\",1764042956691]\",\"[189,46000,\\\"[]\\\",1764042956691]\",\"[189,9000,\\\"[]\\\",1764042956691]\",\"[800,1,\\\"[\\\\\\\"[5,9]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,2]\\\\\\\"]\\\",1764042956691]\",\"[189,32000,\\\"[]\\\",1764042956691]\",\"[221,2,\\\"[]\\\",1764042956691]\",\"[804,1,\\\"[\\\\\\\"[103,7]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,25]\\\\\\\"]\\\",1764042956691]\",\"[585,1,\\\"[]\\\",1764042956691]\",\"[189,29000,\\\"[]\\\",1764042956691]\",\"[189,42000,\\\"[]\\\",1764042956691]\",\"[189,49000,\\\"[]\\\",1764042956692]\",\"[189,22000,\\\"[]\\\",1764042956692]\",\"[471,1,\\\"[\\\\\\\"[81,7]\\\\\\\",\\\\\\\"[30,0]\\\\\\\",\\\\\\\"[93,14]\\\\\\\"]\\\",1764042956692]\",\"[189,44000,\\\"[]\\\",1764042956692]\",\"[189,44000,\\\"[]\\\",1764042956692]\",\"[189,18000,\\\"[]\\\",1764042956692]\"]', '[\"[1081,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746252270378]\",\"[565,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[23,62]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746251648266]\",\"[561,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[14,15]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746251648266]\",\"[557,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[47,1136]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746251648266]\",\"[558,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[22,62]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746251648266]\",\"[564,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[0,3709]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746251648266]\",\"[222,1,\\\"[\\\\\\\"[69,0]\\\\\\\"]\\\",1746251764670]\",\"[1081,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746252158414]\",\"[64,3,\\\"[\\\\\\\"[2,16]\\\\\\\"]\\\",1746251551430]\",\"[224,1,\\\"[\\\\\\\"[67,0]\\\\\\\"]\\\",1746248217302]\",\"[557,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[47,1267]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746250464163]\",\"[12,1,\\\"[\\\\\\\"[14,1]\\\\\\\",\\\\\\\"[130,0]\\\\\\\",\\\\\\\"[142,0]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746254846234]\",\"[1081,1,\\\"[\\\\\\\"[73,0]\\\\\\\"]\\\",1746251944617]\",\"[561,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[14,15]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746327664603]\",\"[565,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[23,67]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746327664603]\",\"[564,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[0,4011]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746327664603]\",\"[558,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[22,55]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746327664603]\",\"[557,1,\\\"[\\\\\\\"[21,15]\\\\\\\",\\\\\\\"[47,1182]\\\\\\\",\\\\\\\"[30,0]\\\\\\\"]\\\",1746327664603]\"]', '[]', '[]', '[0,0,0,0]', '[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]', '[0,0,0,0]', '[25,0,0,0]', '[]', '[1748812909644,1748812908620,1748812907531,1748812906470,1748812905310,1748816494830,1748812903340,1748812902398,1746152468857,1746152468857]', '[\"[2,7,1764043081364,0]\",\"[3,0,1746222561313,0]\",\"[7,0,1746222561313,0]\",\"[11,0,1746222561313,0]\",\"[12,7,1764042966531,0]\",\"[17,7,1764043081046,0]\",\"[18,0,1746222561313,0]\",\"[19,7,1746324904701,0]\",\"[26,0,1746222561313,0]\"]', '[2,17,19,12,-1,-1,-1,-1,-1,-1]', '[\"[0,2,\\\"$Đệ tử\\\",0,1778615111,3]\",\"[0,267101,2781,1000,1000,2060,2020,38,19,2,2060,2020]\",\"[\\\"[-1,0,\\\\\\\"[]\\\\\\\",1764042956708]\\\",\\\"[-1,0,\\\\\\\"[]\\\\\\\",1764042956708]\\\",\\\"[-1,0,\\\\\\\"[]\\\\\\\",1764042956708]\\\",\\\"[-1,0,\\\\\\\"[]\\\\\\\",1764042956708]\\\",\\\"[-1,0,\\\\\\\"[]\\\\\\\",1764042956708]\\\",\\\"[-1,0,\\\\\\\"[]\\\\\\\",1764042956708]\\\"]\",\"[\\\"[0,7,1746327835627,0]\\\",\\\"[-1,0,0,0]\\\",\\\"[-1,0,0,0]\\\",\\\"[-1,0,0,0]\\\",\\\"[-1,0,0,0]\\\",\\\"[-1,0,0,0]\\\",\\\"[-1,0,0,0]\\\"]\"]', '[\"[0,0,0]\",\"[0,0,0]\",\"[0,0,0]\",\"[0,0,0]\",\"[0,0,0]\",\"[0,0,0]\",\"[0,0,0]\"]', '[-1,0,0,0,20,0]', '[0,0,0,0,0,0,0,0,0,0,0,0,0]', '2025-05-02 09:21:08', 'null', '[0,false,1746152468920]', '[]', '[]', 0, '[1,1746152468920]', '[false,0,false,false]', '[0]', '0', '[false,0]', '[0,50000,1,1746152468920,1746222561313]', '[false,0,false]', '[false,1,1746222560801,364]', 1746223203582, '[0,0,0,0,85,1746248404586]', '[0,false,-1,55790,1764043081947,2,3609,1746325247505,0,0]', '[-1,0,0,0,5,0]', '[0,0,false,false,false]', 2, '[\"[0,false]\",\"[0,false]\",\"[0,false]\",\"[0,false]\",\"[0,false]\",\"[5686,false]\",\"[38,false]\",\"[28,false]\",\"[52027000,false]\",\"[0,false]\",\"[0,false]\",\"[1001000000,false]\",\"[1,false]\",\"[133,false]\",\"[0,false]\",\"[0,false]\",\"[0,false]\",\"[0,false]\",\"[0,false]\",\"[0,false]\"]', '[]', '[0,0,32,0,0,259,0,0]', '[0,0]', '2025-05-02 09:21:08', '[0,1]', '[0,0,0]', 0, 0, '[0,0,0,0]', '[]', '[\"1\",1,\"1970-01-01T00:00:00\"]', 0, 0);

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `comments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of posts
-- ----------------------------

-- ----------------------------
-- Table structure for recharge
-- ----------------------------
DROP TABLE IF EXISTS `recharge`;
CREATE TABLE `recharge`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_id` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `code` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `serial` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `tranid` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `amount_real` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` int NOT NULL,
  `time` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of recharge
-- ----------------------------

-- ----------------------------
-- Table structure for sell_item
-- ----------------------------
DROP TABLE IF EXISTS `sell_item`;
CREATE TABLE `sell_item`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `price` int NOT NULL,
  `item` int NOT NULL,
  `slot` int NOT NULL,
  `options` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `users_buy` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `status` int NOT NULL,
  `time` varchar(999) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sell_item
-- ----------------------------

-- ----------------------------
-- Table structure for setting
-- ----------------------------
DROP TABLE IF EXISTS `setting`;
CREATE TABLE `setting`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `author` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `keywords` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `logo` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `background` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `size_logo` int NOT NULL,
  `banner` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `navbar` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `download` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `mtv` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `amount_mtv` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1',
  `thongbao` varchar(99) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nd_thongbao` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `favicon` varchar(999) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of setting
-- ----------------------------
INSERT INTO `setting` VALUES (1, 'Dragon Boy', 'Description', 'Author', 'Keywords', 'https://ngocrongonline.com/images/logo_halo_2024.png', 'https://dragonballwiki.net/wp-content/uploads/2017/08/maxresdefault-7.jpg', 50, 'https://ngocrongonline.com/images/banner_halo_2024.png', 'fixed', '[{\"id\":1,\"image\":\"\\/images\\/jar.png\",\"link\":\"\\/\",\"type\":\"download\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}},{\"id\":2,\"image\":\"\\/images\\/android.png\",\"link\":\"#\",\"type\":\"download\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}},{\"id\":3,\"image\":\"\\/images\\/play.png\",\"link\":\"#\",\"type\":\"download\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}},{\"id\":4,\"image\":\"\\/images\\/pc.png\",\"link\":\"#\",\"type\":\"download\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}},{\"id\":5,\"image\":\"\\/images\\/ip.png\",\"link\":\"#\",\"type\":\"download\",\"description\":{\"text\":\"AppCenter\",\"link\":\"#\"}},{\"id\":6,\"image\":\"\\/images\\/ip.png\",\"link\":\"#\",\"type\":\"download\",\"description\":{\"text\":\"TestFight\",\"link\":\"#\"}},{\"id\":7,\"image\":\"\\/images\\/napngoc.png\",\"link\":\"#\",\"type\":\"download\",\"description\":{\"text\":\"Báo Lỗi thẻ\",\"link\":\"#\"}},{\"id\":8,\"image\":\"\\/images\\/NyfbBnU.png\",\"link\":\"#\",\"type\":\"social\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}},{\"id\":9,\"image\":\"\\/images\\/iaUGdY5.png\",\"link\":\"#\",\"type\":\"social\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}},{\"id\":10,\"image\":\"\\/images\\/6QAnArF.png\",\"link\":\"#\",\"type\":\"social\",\"description\":{\"text\":\"2.2.2\",\"link\":\"#\"}}]', 'true', '10000', 'true', '<p><em><strong>Hướng dẫn anh em đ&aacute;nh t&agrave;i nặn 1 3</strong></em></p>', 'https://ngocrongonline.com/images/favicon-32x32.png');

-- ----------------------------
-- Table structure for shop_ky_gui
-- ----------------------------
DROP TABLE IF EXISTS `shop_ky_gui`;
CREATE TABLE `shop_ky_gui`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `tab` int NOT NULL,
  `item_id` int NOT NULL,
  `gold` int NOT NULL,
  `gem` int NOT NULL,
  `quantity` int NOT NULL,
  `itemOption` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `isUpTop` int NOT NULL,
  `isBuy` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kygui_timkiem`(`tab` ASC, `isBuy` ASC, `item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 762 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shop_ky_gui
-- ----------------------------
INSERT INTO `shop_ky_gui` VALUES (737, 1023154, 3, 17, 999, -1, 77, '[{\"param\":0,\"id\":73}]', 1, 0);

-- ----------------------------
-- Table structure for super_rank
-- ----------------------------
DROP TABLE IF EXISTS `super_rank`;
CREATE TABLE `super_rank`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `player_id` int NOT NULL,
  `name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `rank` int NOT NULL,
  `last_pk_time` bigint NOT NULL,
  `last_reward_time` bigint NOT NULL,
  `ticket` int NOT NULL,
  `win` int NOT NULL,
  `lose` int NOT NULL,
  `history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `received` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13328 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of super_rank
-- ----------------------------
INSERT INTO `super_rank` VALUES (13206, 1023154, 'admin', 66, 1763300136721, 1763300137255, 3, 19, 0, '[]', '{\"head\":123,\"def\":2,\"hp\":22000033,\"dame\":1650000000,\"body\":124,\"leg\":125}', 1);

-- ----------------------------
-- Table structure for task_main_template
-- ----------------------------
DROP TABLE IF EXISTS `task_main_template`;
CREATE TABLE `task_main_template`  (
  `id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_main_template
-- ----------------------------
INSERT INTO `task_main_template` VALUES (0, 'Nhiệm vụ đầu tiên', 'Chi tiết nhiệm vụ');
INSERT INTO `task_main_template` VALUES (1, 'Nhiệm vụ tập luyện', 'Mộc nhân được đặt nhiều tại %1, ngay trước nhà %2\r\nHãy đánh ngã 5 mộc nhân, \r\nsau đó quay về nhà báo cáo với ông %2\r\nĐể đánh, hãy chạm nhanh 2 lần vào đối tượng\r\nThưởng 500 sức mạnh\r\nThưởng 500 tiềm năng');
INSERT INTO `task_main_template` VALUES (2, 'Nhiệm vụ tìm thức ăn', 'Tìm đến %3, tiêu diệt bọn quái %4 và nhặt về 10 đùi gà\r\nThưởng 1 k sức mạnh\r\nThưởng 1 k tiềm năng\r\nHọc được kỹ năng bay');
INSERT INTO `task_main_template` VALUES (3, 'Nhiệm vụ sao băng', 'Đi khám phá xem vật thể lạ vừa rơi xuống hành tinh\r\nThưởng 2 k sức mạnh\r\nThưởng 2 k tiềm năng');
INSERT INTO `task_main_template` VALUES (4, 'Nhiệm vụ thử thách', 'Khủng long mẹ sống tại Trái Đất\r\nLợn lòi mẹ sống tại Namếc\r\nQuỷ đất mẹ sống tại Xayda\r\nDùng tàu vũ trụ để di chuyển sang hành tinh khác\r\nThưởng 4 k sức mạnh\r\nThưởng 4 k tiềm năng');
INSERT INTO `task_main_template` VALUES (5, 'Nhiệm vụ thử thách', 'Lợn lòi mẹ sống tại Namếc\r\nKhủng long mẹ sống tại Trái Đất\r\nQuỷ đất mẹ sống tại Xayda\r\nDùng tàu vũ trụ để di chuyển sang hành tinh khác\r\nThưởng 4 k sức mạnh\r\nThưởng 4 k tiềm năng');
INSERT INTO `task_main_template` VALUES (6, 'Nhiệm vụ thử thách', 'Quỷ đất mẹ sống tại Xayda\r\nKhủng long mẹ sống tại Trái Đất\r\nLợn lòi mẹ sống tại Namếc\r\nDùng tàu vũ trụ để di chuyển sang hành tinh khác\r\nThưởng 4 k sức mạnh\r\nThưởng 4 k tiềm năng');
INSERT INTO `task_main_template` VALUES (7, 'Nhiệm vụ giải cứu', 'Đến khu vực %13,\r\nHạ 20 con %9\r\nThưởng 8 k sức mạnh\r\nThưởng 8 k tiềm năng');
INSERT INTO `task_main_template` VALUES (8, 'Nhiệm vụ tìm ngọc', 'Ngọc rồng 7 sao đang bị bọn\r\n%14 cướp đi.\r\nĐánh bại chúng để tìm lại.\r\nThưởng 15 k sức mạnh\r\nThưởng 15 k tiềm năng');
INSERT INTO `task_main_template` VALUES (9, 'Nhiệm vụ tìm ngọc', 'Tìm đường đến Karin\r\nnói chuyện với Bò Mộng\r\nkhi đụng độ Tàu Pảy Pảy hãy mau chóng bay lên tháp karin');
INSERT INTO `task_main_template` VALUES (10, 'Nhiệm vụ tìm ngọc', 'Học võ với Thần Mèo\r\nXuống rừng Karin tiêu diệt Tàu Pảy Pảy\r\nđem ngọc về cho ông %2\r\nThưởng 15 k sức mạnh\r\nThưởng 15 k tiềm năng');
INSERT INTO `task_main_template` VALUES (11, 'Nhiệm vụ bái sư', 'Tìm đường tới %11, trò chuyện với %10 và xin làm đệ tử');
INSERT INTO `task_main_template` VALUES (12, 'Nhiệm vụ gia nhập bang hội', 'Báo cáo với %2 khi bang của bạn\r\ncó từ 5 thành viên trở lên\r\nThưởng 20 k sức mạnh\r\nThưởng 20 k tiềm năng');
INSERT INTO `task_main_template` VALUES (13, 'Nhiệm vụ bang hội đầu tiên', 'Cùng phối hợp với 1 người đồng đội lên đường làm nhiệm vụ\nGợi ý:\nHeo rừng xuất hiện tại rừng Bamboo\nHeo da xanh xuất \nhiện tại núi hoa vàng\nHeo xayda xuất hiện tại rừng cọ\nHãy tới trạm tàu vũ trụ để có thể di chuyển qua các map');
INSERT INTO `task_main_template` VALUES (14, 'Nhiệm vụ bái sư', 'Đánh bọn %12 để lấy truyện\r\nDoremon tập 2\r\nThưởng 80 k sức mạnh\r\nThưởng 80 k tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (15, 'Nhiệm vụ bang hội thứ 2', 'Cùng ít nhất 2 thành viên trong\r\nbang tiêu diệt\r\nBulon tại Đảo Bulông(Trái Đất)\r\nUkulele tại Đông Nam Guru(Namếc)\r\nQuỷ mập tại Bờ Vực Đen(Xayda)\r\nThưởng 150 k sức mạnh\r\nThưởng 150 k tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (16, 'Nhiệm vụ thách đấu', 'Thách đấu và chiến thằng 10 người\r\nbất kì\r\nThưởng 150 k sức mạnh\r\nThưởng 150 k tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (17, 'Nhiệm vụ tiêu diệt Boss Trùm', 'Đạt 1.500.000 sức mạnh để trở\r\nthành Siêu nhân\r\nTiêu diệt Akkuman tại Thành\r\nphố Vegeta, tiêu diệt Tamborine tại Đông\r\nKarin, tiêu diệt Drum tại Thung\r\nlũng Namếc\r\nThưởng 200 k sức mạnh\r\nThưởng 200 k tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (18, 'Nhiệm vụ thử thách', 'Đạt 5 triệu sức mạnh\r\nTham gia và chiến thắng vòng 2 đại hội\r\nvõ thuật tại Vách núi Kakarot\r\nThưởng 500 k sức mạnh\r\nThưởng 500 k tiềm năng');
INSERT INTO `task_main_template` VALUES (19, 'Nhiệm vụ cam go', 'Đạt 15 triệu sức mạnh\r\nVào doanh trại Độc Nhãn tìm diệt\r\nTrung Úy Trắng\r\nThưởng 5 Tr sức mạnh\r\nThưởng 5 Tr tiềm năng');
INSERT INTO `task_main_template` VALUES (20, 'Nhiệm vụ bất khả thi', 'Đạt 50 triệu sức mạnh\r\nTiêu diệt bọn tay sai của Fide tại Xayda\r\nThưởng 50 Tr sức mạnh\r\nThưởng 50 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (21, 'Nhiệm vụ tìm diệt đệ tử', 'Tiêu diệt bọn đệ tử Kuku, Mập Đầu Đinh,\r\nRambo của Fide đại ca tại Xayda\r\nCui có thể biết vị trí của chúng, nếu tìm\r\nkhông thấy hãy đến gặp Cui tại thành\r\nphố Vegeta\r\nThưởng 20 Tr sức mạnh\r\nThưởng 20 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (22, 'Tiểu đội sát thủ', 'Tiêu diệt Tiểu Đội Sát Thủ do Fide đại\r\nca gọi đến tại Xayda\r\nThưởng 20 Tr sức mạnh\r\nThưởng 20 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (23, 'Fide đại ca', 'Fide đã xuất hiện tại núi khỉ vàng\r\nThưởng 20 Tr sức mạnh\r\nThưởng 20 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (24, 'Chú bé đến từ tương lai', 'Đến trái đất, rừng bamboo, rừng dương\r\nxỉ, nam Kamê tìm người lạ\r\nĐến đảo rùa đưa thuốc cho Quy Lão\r\nTheo Ca Lích đến tương lai\r\nGiúp họ diệt bọn bọ hung con\r\nThưởng 1 Tr sức mạnh\r\nThưởng 1 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (25, 'Chạm trán Rôbốt Sát Thủ lần 1', 'Hãy đến thành phố phía nam\r\nđảo balê hoặc cao nguyên\r\nCùng 2 đồng bang diệt 900 Xên con cấp 3\r\nBáo với Bunma tương lai\r\nThưởng 1 Tr sức mạnh\r\nThưởng 1 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (26, 'Chạm trán Rôbốt Sát Thủ lần 2', 'Trở về quá khứ, đến sân sau siêu thị\r\nTiêu diệt bọn Rôbốt sát thủ\r\nBáo với Bunma tương lai\r\nThưởng 1 Tr sức mạnh\r\nThưởng 1 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (27, 'Chạm trán Rôbốt Sát Thủ lần 3', 'Đến thành phố, ngọn núi, thung lũng phía Bắc\r\nTiêu diệt bọn Rôbốt sát thủ\r\nCùng 2 đồng bang diệt 800 Xên con cấp 5\r\nBáo với Bunma tương lai\r\nThưởng 1 Tr sức mạnh\r\nThưởng 1 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (28, 'Chạm trán Xên bọ hung', 'Đến thị trấn Ginder\r\nTiêu diệt Xên Bọ Hung cấp 1\r\nTiêu diệt Xên Bọ Hung cấp 2\r\nTiêu diệt Xên Bọ Hung hoàn thiện\r\nCùng 2 đồng bang diệt 700 Xên con cấp 8\r\nBáo với Bunma tương lai\r\nThưởng 1 Tr sức mạnh\r\nThưởng 1 Tr tiềm năng');
INSERT INTO `task_main_template` VALUES (29, 'Cuộc dạo chơi của Xên', 'Nâng sức đánh gốc lên 10K, đến gặp thần\r\nmèo\r\nThu thập Capsule kì bí\r\nĐến võ đài Xên Bọ Hung\r\nTiêu diệt 7 đứa con của Xên\r\nTiêu diệt Siêu Bọ Hung\r\nBáo với Bunma tương lai\r\nThưởng 1 Tr sức mạnh\r\nThưởng 1 Tr tiềm năng\r\n');
INSERT INTO `task_main_template` VALUES (30, 'Cuộc đối đầu không cân sức', 'Cẩn thận !!!\r\nNhững vị khách không mời mà tới\r\nthường tỏ ra nguy hiểm\r\n');
INSERT INTO `task_main_template` VALUES (31, 'Chạm trán người ngoài hành tinh', 'Bảo vệ hành tinh thực vật, hạ những kẻ xâm lược.\r\nThưởng 10 Tr sức mạnh\r\nThưởng 10 Tr tiềm năng');
INSERT INTO `task_main_template` VALUES (32, 'Vui lòng chờ nhiệm vụ mới', 'Vui lòng chờ nhiệm vụ mới');

-- ----------------------------
-- Table structure for trans_log
-- ----------------------------
DROP TABLE IF EXISTS `trans_log`;
CREATE TABLE `trans_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `amount` bigint NOT NULL,
  `seri` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `pin` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `type` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `status` int NOT NULL DEFAULT 0,
  `trans_id` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp,
  `giatri` int NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of trans_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
