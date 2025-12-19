/*
 Navicat Premium Data Transfer

 Source Server         : nro
 Source Server Type    : MySQL
 Source Server Version : 100427
 Source Host           : localhost:3306
 Source Schema         : nro_data

 Target Server Type    : MySQL
 Target Server Version : 100427
 File Encoding         : 65001

 Date: 25/11/2025 22:51:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for achievement_template
-- ----------------------------
DROP TABLE IF EXISTS `achievement_template`;
CREATE TABLE `achievement_template`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `info1` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `info2` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `money` int NOT NULL,
  `max_count` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of achievement_template
-- ----------------------------
INSERT INTO `achievement_template` VALUES (1, 'Gia nhập Vệ Binh', 'Đạt cấp Vệ Binh', 50, 340000);
INSERT INTO `achievement_template` VALUES (2, 'Sức mạnh siêu cấp', 'Đạt cấp %1', 50, 1500000);
INSERT INTO `achievement_template` VALUES (3, 'Nông dân chăm chỉ', 'Cây đậu thần đạt cấp 5', 50, 5);
INSERT INTO `achievement_template` VALUES (4, 'Trăm trận trăm thắng', 'Thắng 100 người khác nhau', 100, 100);
INSERT INTO `achievement_template` VALUES (5, 'Nội công cao cường', 'Chưởng 2.000 phát', 50, 2000);
INSERT INTO `achievement_template` VALUES (6, 'Khinh công thành thạo', 'Bay 20.000 mét', 50, 20000);
INSERT INTO `achievement_template` VALUES (7, 'Thợ săn thiện xạ', 'Hạ 1.000 quái trên không', 50, 1000);
INSERT INTO `achievement_template` VALUES (8, 'Tập luyện bài bản', 'Hạ 1.000 người rơm', 100, 1000);
INSERT INTO `achievement_template` VALUES (9, 'Hoạt động chăm chỉ', 'Chơi hơn 120 giờ', 50, 120);
INSERT INTO `achievement_template` VALUES (10, 'Hỗ trợ đồng đội', 'Cho 10.000 đậu thần', 50, 10000);
INSERT INTO `achievement_template` VALUES (11, 'Trùm nhặt ve chai', 'Bán cho %2 200 món đồ', 50, 200);
INSERT INTO `achievement_template` VALUES (12, 'Lần đầu nạp ngọc', 'Nạp ít nhất 100k', 500, 1500);
INSERT INTO `achievement_template` VALUES (13, 'Đánh bại siêu quái', 'Hạ 100 siêu quái', 100, 100);
INSERT INTO `achievement_template` VALUES (14, 'Thánh hồi sinh', 'Hồi sinh tại chỗ 200 lần', 50, 200);
INSERT INTO `achievement_template` VALUES (15, 'Kỹ năng thành thạo', 'Dùng chiêu đặc biệt 1000 lần', 50, 1000);
INSERT INTO `achievement_template` VALUES (16, 'Trùm nhặt ngọc', 'Nhặt 1000 ngọc', 100, 1000);
INSERT INTO `achievement_template` VALUES (17, 'Đạt 150 triệu sức mạnh', 'Dành cho tân thủ', 50, 150000000);
INSERT INTO `achievement_template` VALUES (18, 'Tuyệt kỹ thành thạo', 'Dùng tuyệt kỹ (skill thứ 9) 7749 lần', 50, 49);
INSERT INTO `achievement_template` VALUES (19, 'Chăm sóc đặc biệt', 'Được Namếc hồi sinh 2K lần', 50, 2000);
INSERT INTO `achievement_template` VALUES (20, 'Trùm kết liễu Boss', 'Đánh đòn cuối hạ Boss 2K lần', 20, 2000);

-- ----------------------------
-- Table structure for array_head_2_frames
-- ----------------------------
DROP TABLE IF EXISTS `array_head_2_frames`;
CREATE TABLE `array_head_2_frames`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of array_head_2_frames
-- ----------------------------
INSERT INTO `array_head_2_frames` VALUES (1, '[979,980]');

-- ----------------------------
-- Table structure for bg_item_template
-- ----------------------------
DROP TABLE IF EXISTS `bg_item_template`;
CREATE TABLE `bg_item_template`  (
  `id` int NOT NULL,
  `image_id` int NOT NULL,
  `layer` int NOT NULL,
  `dx` int NOT NULL,
  `dy` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bg_item_template
-- ----------------------------
INSERT INTO `bg_item_template` VALUES (0, 0, 1, -17, -3);
I-------------------------------------------
-- Table structure for clan_task_template
-- ----------------------------
DROP TABLE IF EXISTS `clan_task_template`;
CREATE TABLE `clan_task_template`  (
  `id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of clan_task_template
-- ----------------------------
INSERT INTO `clan_task_template` VALUES (0, 'Hạ %1 khủng long', '1-20', '20-100', '100-500', '500-2000', '2000-5000');

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
INSERT INTO `event` VALUES (1, 'international_womens_day', '{\"damePrecent\":0,\"hpPrecent\":0,\"mpPrecent\":0,\"papPrecent\":1709918371414}');

-- ----------------------------
-- Table structure for flag_bag
-- ----------------------------
DROP TABLE IF EXISTS `flag_bag`;
CREATE TABLE `flag_bag`  (
  `id` int NOT NULL,
  `icon_data` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'flag_bag',
  `gold` int NOT NULL DEFAULT -1,
  `gem` int NOT NULL DEFAULT -1,
  `icon_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flag_bag
-- ----------------------------
INSERT INTO `flag_bag` VALUES (0, '1017, 1018', 'Cờ xám', 10000, -1, 1027);

-- ----------------------------
-- Table structure for head_avatar
-- ----------------------------
DROP TABLE IF EXISTS `head_avatar`;
CREATE TABLE `head_avatar`  (
  `head_id` int NOT NULL,
  `avatar_id` int NOT NULL,
  PRIMARY KEY (`head_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of head_avatar
-- ----------------------------
INSERT INTO `head_avatar` VALUES (0, 516);

-- ----------------------------
-- Table structure for img_by_name
-- ----------------------------
DROP TABLE IF EXISTS `img_by_name`;
CREATE TABLE `img_by_name`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `NAME` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `n_frame` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `NAME`(`NAME` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of img_by_name
-- ----------------------------
INSERT INTO `img_by_name` VALUES (1, 'mount_1_0', 3);

-- ----------------------------
-- Table structure for intrinsic
-- ----------------------------
DROP TABLE IF EXISTS `intrinsic`;
CREATE TABLE `intrinsic`  (
  `id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `param_from_1` int NOT NULL DEFAULT 0,
  `param_to_1` int NOT NULL DEFAULT 0,
  `param_from_2` int NOT NULL DEFAULT 0,
  `param_to_2` int NOT NULL DEFAULT 0,
  `icon` int NOT NULL DEFAULT 0,
  `gender` smallint NOT NULL DEFAULT 3
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of intrinsic
-- ----------------------------
INSERT INTO `intrinsic` VALUES (0, 'Chưa kích hoạt nội tại\nBấm vào để xem chi tiết', 0, 0, 0, 0, 5223, 3);


-- ----------------------------
-- Table structure for item_option_template
-- ----------------------------
DROP TABLE IF EXISTS `item_option_template`;
CREATE TABLE `item_option_template`  (
  `id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of item_option_template
-- ----------------------------
INSERT INTO `item_option_template` VALUES (0, 'Tấn công+#');

-- ----------------------------
-- Table structure for item_template
-- ----------------------------
DROP TABLE IF EXISTS `item_template`;
CREATE TABLE `item_template`  (
  `id` int NOT NULL,
  `TYPE` int NOT NULL,
  `gender` smallint NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `level` int NOT NULL DEFAULT 0,
  `icon_id` int NOT NULL,
  `part` int NOT NULL,
  `is_up_to_up` tinyint(1) NOT NULL,
  `power_require` int NOT NULL,
  `gold` int NOT NULL DEFAULT 0,
  `gem` int NOT NULL DEFAULT 0,
  `head` int NOT NULL DEFAULT -1,
  `body` int NOT NULL DEFAULT -1,
  `leg` int NOT NULL DEFAULT -1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of item_template
-- ----------------------------
INSERT INTO `item_template` VALUES (0, 0, 0, 'Áo vải 3 lỗ', 'Giúp giảm sát thương', 1, 390, 14, 0, 1200, 500, 0, -1, -1, -1);
INSERT INTO `item_template` VALUES (1, 0, 1, 'Áo sợi len', 'Giúp giảm sát thương', 1, 393, 10, 0, 1200, 500, 0, -1, -1, -1);


-- ----------------------------
-- Table structure for map_template
-- ----------------------------
DROP TABLE IF EXISTS `map_template`;
CREATE TABLE `map_template`  (
  `id` int NOT NULL,
  `NAME` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `zones` int NOT NULL DEFAULT 1,
  `max_player` int NOT NULL DEFAULT 15,
  `data` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]',
  `type` int NOT NULL DEFAULT 1,
  `planet_id` int NOT NULL DEFAULT 1,
  `bg_type` int NOT NULL DEFAULT 1,
  `tile_id` int NOT NULL DEFAULT 1,
  `bg_id` int NOT NULL DEFAULT 1,
  `waypoints` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `mobs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `npcs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `is_map_double` int NOT NULL DEFAULT 0,
  `effect` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of map_template
-- ----------------------------
INSERT INTO `map_template` VALUES (0, 'Làng Aru', 21, 12, '[0,0,0,1,0]', 0, 0, 0, 1, 0, '[\"[\"Đồi hoa cúc\",1224,408,1248,432,0,0,1,60,384]\",\"[\"Nhà Gôhan\",288,408,360,432,1,1,21,489,336]\",\"[\"Vách núi Aru\",0,408,24,432,0,0,42,1380,432]\"]', '[\"[0,1,10,780,432]\",\"[0,1,10,900,432]\",\"[0,1,10,1020,432]\",\"[0,1,10,660,432]\"]', '[\"[7,228,432,562]\",\"[6,492,432,0]\",\"[67,618,432,2132]\",\"[64,704,432,6578]\"]', 0, '[]');

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
-- Table structure for mob_template
-- ----------------------------
DROP TABLE IF EXISTS `mob_template`;
CREATE TABLE `mob_template`  (
  `id` int NOT NULL,
  `TYPE` int NOT NULL,
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `hp` int NOT NULL,
  `range_move` smallint NOT NULL,
  `speed` smallint NOT NULL,
  `dart_type` smallint NOT NULL,
  `percent_dame` smallint NOT NULL DEFAULT 5,
  `percent_tiem_nang` smallint NOT NULL DEFAULT 50,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mob_template
-- ----------------------------
INSERT INTO `mob_template` VALUES (0, 0, 'Mộc nhân', 20, 0, 1, 25, 5, 50);

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of napthe
-- ----------------------------

-- ----------------------------
-- Table structure for notify
-- ----------------------------
DROP TABLE IF EXISTS `notify`;
CREATE TABLE `notify`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `text` text CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notify
-- ----------------------------
INSERT INTO `notify` VALUES (1, 'Hỗ trợ nhiệm vụ', 'Hỗ trợ nhiệm vụ :\r\n- TDST ở Nappa\r\n- Fide 1 2 3\r\n- Pic , Poc , KK\r\n- Xên bọ hung 1 2 3\r\nThời gian : 4h - 5h sáng và 17h - 18h tối mỗi ngày\r\nXong nhiệm vụ sẽ không thể vào khu vực');

-- ----------------------------
-- Table structure for npc_template
-- ----------------------------
DROP TABLE IF EXISTS `npc_template`;
CREATE TABLE `npc_template`  (
  `id` int NOT NULL,
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `head` int NOT NULL,
  `body` int NOT NULL,
  `leg` int NOT NULL,
  `avatar` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of npc_template
-- ----------------------------
INSERT INTO `npc_template` VALUES (0, 'Ông Gôhan', 18, 19, 20, 349);


-- ----------------------------
-- Table structure for part
-- ----------------------------
DROP TABLE IF EXISTS `part`;
CREATE TABLE `part`  (
  `id` int NOT NULL,
  `TYPE` int NOT NULL,
  `DATA` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of part
-- ----------------------------
INSERT INTO `part` VALUES (0, 0, '[[17,0,0],[18,0,0],[20,0,0]]');

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
  `bank` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payments
-- ----------------------------

-- ----------------------------
-- Table structure for radar
-- ----------------------------
DROP TABLE IF EXISTS `radar`;
CREATE TABLE `radar`  (
  `id` int NOT NULL,
  `iconId` int NULL DEFAULT 0,
  `rank` tinyint NULL DEFAULT 0,
  `max` int NULL DEFAULT 60,
  `type` int NULL DEFAULT 0,
  `template` int NULL DEFAULT 1,
  `body` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '[]',
  `name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '',
  `info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '',
  `options` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '[]',
  `require` int NULL DEFAULT -1,
  `require_level` int NULL DEFAULT 0,
  `aura_id` smallint NULL DEFAULT -1
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of radar
-- ----------------------------
INSERT INTO `radar` VALUES (828, 7467, 0, 120, 0, 1, '[{\"head\":1, \"body\":1, \"leg\":1, \"bag\":-1}]', 'Thẻ Khủng long', 'Hai chi trước của Khủng long rất ngắn nên chúng không thể cầm thức ăn được', '[{\"id\": 6, \"param\": 10, \"activeCard\": 0},\n{\"id\": 6, \"param\": 20, \"activeCard\": 1},\n{\"id\": 6, \"param\": 30, \"activeCard\": 2}]', -1, 0, 1);

-- ----------------------------
-- Table structure for shop
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `npc_id` int NOT NULL,
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `type_shop` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `npc_id`(`npc_id` ASC) USING BTREE,
  CONSTRAINT `shop_ibfk_1` FOREIGN KEY (`npc_id`) REFERENCES `npc_template` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shop
-- ----------------------------
INSERT INTO `shop` VALUES (1, 7, 'BUNMA', 0);


-- ----------------------------
-- Table structure for shop_ky_gui
-- ----------------------------
DROP TABLE IF EXISTS `shop_ky_gui`;
CREATE TABLE `shop_ky_gui`  (
  `id` int NOT NULL,
  `player_id` int NOT NULL,
  `tab` int NOT NULL,
  `item_id` int NOT NULL,
  `gold` int NOT NULL,
  `gem` int NOT NULL,
  `quantity` int NOT NULL,
  `itemOption` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `lasttime` bigint NOT NULL,
  `isBuy` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shop_ky_gui
-- ----------------------------

-- ----------------------------
-- Table structure for side_task_template
-- ----------------------------
DROP TABLE IF EXISTS `side_task_template`;
CREATE TABLE `side_task_template`  (
  `id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv4` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count_lv5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of side_task_template
-- ----------------------------
INSERT INTO `side_task_template` VALUES (0, 'Tiêu diệt %1 khủng long', '1-20', '20-100', '100-500', '500-2000', '2000-5000');

-- ----------------------------
-- Table structure for skill_template
-- ----------------------------
DROP TABLE IF EXISTS `skill_template`;
CREATE TABLE `skill_template`  (
  `nclass_id` int NOT NULL,
  `id` int NOT NULL,
  `NAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_point` smallint NOT NULL DEFAULT 7,
  `mana_use_type` smallint NOT NULL,
  `TYPE` smallint NOT NULL,
  `icon_id` int NOT NULL,
  `dam_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `slot` int NOT NULL,
  `skills` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`nclass_id`, `id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of skill_template
-- ----------------------------
INSERT INTO `skill_template` VALUES (0, 0, 'Chiêu đấm Dragon', 7, 0, 1, 539, 'Tăng sức đánh: #%', 0, '[\"{\"power_require\":1000,\"damage\":100,\"dx\":32,\"dy\":18,\"price\":0,\"max_fight\":1,\"mana_use\":1,\"cool_down\":500,\"id\":0,\"point\":1,\"info\":\"tại ông nội ngay lúc đầu\"}\",\"{\"power_require\":10000,\"damage\":110,\"dx\":34,\"dy\":18,\"price\":10,\"max_fight\":1,\"mana_use\":2,\"cool_down\":500,\"id\":1,\"point\":2,\"info\":\"tại ông nội\"}\",\"{\"power_require\":22000,\"damage\":120,\"dx\":36,\"dy\":18,\"price\":50,\"max_fight\":1,\"mana_use\":4,\"cool_down\":500,\"id\":2,\"point\":3,\"info\":\"tại Quy Lão Kame\"}\",\"{\"power_require\":66000,\"damage\":130,\"dx\":38,\"dy\":18,\"price\":100,\"max_fight\":1,\"mana_use\":8,\"cool_down\":500,\"id\":3,\"point\":4,\"info\":\"tại Quy Lão Kame\"}\",\"{\"power_require\":200000,\"damage\":140,\"dx\":40,\"dy\":18,\"price\":500,\"max_fight\":1,\"mana_use\":16,\"cool_down\":500,\"id\":4,\"point\":5,\"info\":\"tại Quy Lão Kame\"}\",\"{\"power_require\":600000,\"damage\":150,\"dx\":42,\"dy\":18,\"price\":1000,\"max_fight\":1,\"mana_use\":32,\"cool_down\":500,\"id\":5,\"point\":6,\"info\":\"tại Quy Lão Kame\"}\",\"{\"power_require\":1800000,\"damage\":160,\"dx\":44,\"dy\":18,\"price\":2000,\"max_fight\":1,\"mana_use\":70,\"cool_down\":500,\"id\":6,\"point\":7,\"info\":\"tại Quy Lão Kame\"}\"]');

-- ----------------------------
-- Table structure for tab_shop
-- ----------------------------
DROP TABLE IF EXISTS `tab_shop`;
CREATE TABLE `tab_shop`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int NOT NULL,
  `tab_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `tab_index` int NOT NULL DEFAULT 0,
  `items` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `shop_id`(`shop_id` ASC) USING BTREE,
  CONSTRAINT `tab_shop_ibfk_1` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tab_shop
-- ----------------------------
INSERT INTO `tab_shop` VALUES (1, 1, 'Áo<>Quần', 1, '[{\"cost\":500,\"type_sell\":0,\"is_new\":false,\"temp_id\":0,\"item_spec\":0,\"options\":[{\"param\":2,\"id\":47}],\"is_sell\":true},{\"cost\":5000,\"type_sell\":0,\"is_new\":false,\"temp_id\":33,\"item_spec\":0,\"options\":[{\"param\":4,\"id\":47}],\"is_sell\":true},{\"cost\":10000,\"type_sell\":0,\"is_new\":false,\"temp_id\":3,\"item_spec\":0,\"options\":[{\"param\":8,\"id\":47}],\"is_sell\":true},{\"cost\":20000,\"type_sell\":0,\"is_new\":false,\"temp_id\":34,\"item_spec\":0,\"options\":[{\"param\":16,\"id\":47}],\"is_sell\":true},{\"cost\":50000,\"type_sell\":0,\"is_new\":false,\"temp_id\":136,\"item_spec\":0,\"options\":[{\"param\":24,\"id\":47}],\"is_sell\":true},{\"cost\":100000,\"type_sell\":0,\"is_new\":false,\"temp_id\":137,\"item_spec\":0,\"options\":[{\"param\":40,\"id\":47}],\"is_sell\":true},{\"cost\":200000,\"type_sell\":0,\"is_new\":false,\"temp_id\":138,\"item_spec\":0,\"options\":[{\"param\":60,\"id\":47}],\"is_sell\":true},{\"cost\":500000,\"type_sell\":0,\"is_new\":false,\"temp_id\":139,\"item_spec\":0,\"options\":[{\"param\":90,\"id\":47}],\"is_sell\":true},{\"cost\":2000000,\"type_sell\":0,\"is_new\":false,\"temp_id\":230,\"item_spec\":0,\"options\":[{\"param\":200,\"id\":47}],\"is_sell\":true},{\"cost\":5800000,\"type_sell\":0,\"is_new\":false,\"temp_id\":231,\"item_spec\":0,\"options\":[{\"param\":250,\"id\":47}],\"is_sell\":true},{\"cost\":17000000,\"type_sell\":0,\"is_new\":false,\"temp_id\":232,\"item_spec\":0,\"options\":[{\"param\":300,\"id\":47}],\"is_sell\":true},{\"cost\":52000000,\"type_sell\":0,\"is_new\":false,\"temp_id\":233,\"item_spec\":0,\"options\":[{\"param\":400,\"id\":47}],\"is_sell\":true},{\"cost\":400,\"type_sell\":0,\"is_new\":false,\"temp_id\":6,\"item_spec\":0,\"options\":[{\"param\":30,\"id\":6}],\"is_sell\":true},{\"cost\":4000,\"type_sell\":0,\"is_new\":false,\"temp_id\":35,\"item_spec\":0,\"options\":[{\"param\":150,\"id\":6},{\"param\":12,\"id\":27}],\"is_sell\":true},{\"cost\":8000,\"type_sell\":0,\"is_new\":false,\"temp_id\":9,\"item_spec\":0,\"options\":[{\"param\":300,\"id\":6},{\"param\":40,\"id\":27}],\"is_sell\":true},{\"cost\":18000,\"type_sell\":0,\"is_new\":false,\"temp_id\":36,\"item_spec\":0,\"options\":[{\"param\":600,\"id\":6},{\"param\":120,\"id\":27}],\"is_sell\":true},{\"cost\":45000,\"type_sell\":0,\"is_new\":false,\"temp_id\":140,\"item_spec\":0,\"options\":[{\"param\":1400,\"id\":6},{\"param\":280,\"id\":27}],\"is_sell\":true},{\"cost\":90000,\"type_sell\":0,\"is_new\":false,\"temp_id\":141,\"item_spec\":0,\"options\":[{\"param\":3000,\"id\":6},{\"param\":600,\"id\":27}],\"is_sell\":true},{\"cost\":180000,\"type_sell\":0,\"is_new\":false,\"temp_id\":142,\"item_spec\":0,\"options\":[{\"param\":6000,\"id\":6},{\"param\":1200,\"id\":27}],\"is_sell\":true},{\"cost\":400000,\"type_sell\":0,\"is_new\":false,\"temp_id\":143,\"item_spec\":0,\"options\":[{\"param\":10000,\"id\":6},{\"param\":2000,\"id\":27}],\"is_sell\":true},{\"cost\":2000000,\"type_sell\":0,\"is_new\":false,\"temp_id\":242,\"item_spec\":0,\"options\":[{\"param\":14000,\"id\":6},{\"param\":2500,\"id\":27}],\"is_sell\":true},{\"cost\":5800000,\"type_sell\":0,\"is_new\":false,\"temp_id\":243,\"item_spec\":0,\"options\":[{\"param\":18000,\"id\":6},{\"param\":3000,\"id\":27}],\"is_sell\":true},{\"cost\":17000000,\"type_sell\":0,\"is_new\":false,\"temp_id\":244,\"item_spec\":0,\"options\":[{\"param\":22000,\"id\":6},{\"param\":3500,\"id\":27}],\"is_sell\":true},{\"cost\":52000000,\"type_sell\":0,\"is_new\":false,\"temp_id\":245,\"item_spec\":0,\"options\":[{\"param\":26000,\"id\":6},{\"param\":4000,\"id\":27}],\"is_sell\":true}]');

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

-- ----------------------------
-- Table structure for task_sub_template
-- ----------------------------
DROP TABLE IF EXISTS `task_sub_template`;
CREATE TABLE `task_sub_template`  (
  `task_main_id` int NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `max_count` int NOT NULL DEFAULT -1,
  `notify` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `npc_id` int NOT NULL DEFAULT -1,
  `map` int NOT NULL,
  `ducvupro` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`ducvupro`) USING BTREE,
  INDEX `task_main_id`(`task_main_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 277 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_sub_template
-- ----------------------------
INSERT INTO `task_sub_template` VALUES (0, 'Di chuyển tới mũi tên chỉ dẫn', 1, '', -1, -1, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of trans_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
