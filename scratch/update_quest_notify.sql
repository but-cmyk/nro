-- UPDATE QUEST NOTIFICATIONS (GUIDE TEXT) FROM TASK 9 ONWARDS
USE `nro_data`;

-- Task 9: Tìm ngọc (Karin Tower & Tao Pay Pay)
UPDATE `task_sub_template` SET `notify` = 'Hãy đến rừng Karin nói chuyện với Bò Mộng.' WHERE `ducvupro` = 161;
UPDATE `task_sub_template` SET `notify` = 'Mau chóng chạy trốn lên tháp Karin để tránh Tàu Pảy Pảy!' WHERE `ducvupro` = 162;
UPDATE `task_sub_template` SET `notify` = 'Hãy leo lên đỉnh tháp Karin.' WHERE `ducvupro` = 163;
UPDATE `task_sub_template` SET `notify` = 'Nói chuyện với Thần Mèo Karin trên đỉnh tháp.' WHERE `ducvupro` = 164;

-- Task 10: Tìm ngọc (defeat Tao Pay Pay)
UPDATE `task_sub_template` SET `notify` = 'Hãy thách đấu và đánh thắng Thần Mèo Karin.' WHERE `ducvupro` = 165;
UPDATE `task_sub_template` SET `notify` = 'Đi xuống chân tháp Karin tiêu diệt Tàu Pảy Pảy.' WHERE `ducvupro` = 166;
UPDATE `task_sub_template` SET `notify` = 'Báo cáo chiến thắng với Bò Mộng.' WHERE `ducvupro` = 167;
UPDATE `task_sub_template` SET `notify` = 'Trở về nhà báo cáo với ông.' WHERE `ducvupro` = 168;

-- Task 11: Bái sư
UPDATE `task_sub_template` SET `notify` = 'Đi tìm Sư phụ tại đảo hoặc vách núi của hành tinh bạn.' WHERE `ducvupro` = 169;
UPDATE `task_sub_template` SET `notify` = 'Trở về nhà báo cáo với ông.' WHERE `ducvupro` = 170;

-- Task 12: Gia nhập bang hội
UPDATE `task_sub_template` SET `notify` = 'Gia nhập hoặc thành lập một bang hội có ít nhất 5 thành viên.' WHERE `ducvupro` = 171;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ của bạn để báo cáo.' WHERE `ducvupro` = 172;

-- Task 13: Bang hội đầu tiên
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 30 Heo rừng tại rừng Bamboo.' WHERE `ducvupro` = 173;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 30 Heo da xanh tại núi hoa vàng.' WHERE `ducvupro` = 174;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 30 Heo xayda tại rừng cọ.' WHERE `ducvupro` = 175;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ của bạn để báo cáo.' WHERE `ducvupro` = 176;

-- Task 14: Tìm tập bản đồ (sửa tên ở doneTask)
UPDATE `task_sub_template` SET `notify` = 'Tích lũy đạt 200.000 sức mạnh.' WHERE `ducvupro` = 177;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt lính lác để thu thập tập bản đồ.' WHERE `ducvupro` = 178;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 179;

-- Task 15: Bang hội thứ 2
UPDATE `task_sub_template` SET `notify` = 'Tích lũy đạt 500.000 sức mạnh.' WHERE `ducvupro` = 180;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 30 Bulon tại Đảo Bulông (Trái Đất).' WHERE `ducvupro` = 181;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 30 Ukulele tại Đông Nam Guru (Namếc).' WHERE `ducvupro` = 182;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 30 Quỷ mập tại Bờ Vực Đen (Xayda).' WHERE `ducvupro` = 183;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 184;

-- Task 16: Thách đấu
UPDATE `task_sub_template` SET `notify` = 'Thách đấu và chiến thắng 10 người chơi khác.' WHERE `ducvupro` = 185;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 186;

-- Task 17: Boss Trùm (Akkuman, Tambourine, Drum)
UPDATE `task_sub_template` SET `notify` = 'Tích lũy đạt 1.500.000 sức mạnh.' WHERE `ducvupro` = 187;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Akkuman tại thành phố Vegeta.' WHERE `ducvupro` = 188;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Tamborine tại đông Karin.' WHERE `ducvupro` = 189;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Drum tại thung lũng Namếc.' WHERE `ducvupro` = 190;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 191;

-- Task 18: Đại hội võ thuật
UPDATE `task_sub_template` SET `notify` = 'Tích lũy đạt 5.000.000 sức mạnh.' WHERE `ducvupro` = 192;
UPDATE `task_sub_template` SET `notify` = 'Thắng vòng 2 Đại hội võ thuật tại vách núi Kakarot.' WHERE `ducvupro` = 193;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 194;

-- Task 19: Doanh trại độc nhãn
UPDATE `task_sub_template` SET `notify` = 'Tích lũy đạt 15.000.000 sức mạnh.' WHERE `ducvupro` = 195;
UPDATE `task_sub_template` SET `notify` = 'Vào doanh trại Độc Nhãn tiêu diệt Trung Úy Trắng.' WHERE `ducvupro` = 196;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 197;

-- Task 20: Lính Fide
UPDATE `task_sub_template` SET `notify` = 'Tích lũy đạt 50.000.000 sức mạnh.' WHERE `ducvupro` = 198;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 500 Nappa tại hành tinh Xayda.' WHERE `ducvupro` = 199;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 400 Soldier.' WHERE `ducvupro` = 200;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 300 Appule.' WHERE `ducvupro` = 201;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 200 Raspberry.' WHERE `ducvupro` = 202;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 100 Thằn lằn xanh.' WHERE `ducvupro` = 203;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 204;

-- Task 21: Đệ tử Fide
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Boss Kuku.' WHERE `ducvupro` = 205;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Boss Mập Đầu Đinh.' WHERE `ducvupro` = 206;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Boss Rambo.' WHERE `ducvupro` = 207;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 208;

-- Task 22: Tiểu đội sát thủ
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Số 4.' WHERE `ducvupro` = 209;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Số 3.' WHERE `ducvupro` = 210;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Số 1.' WHERE `ducvupro` = 211;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Tiểu Đội Trưởng.' WHERE `ducvupro` = 212;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 213;

-- Task 23: Fide đại ca
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Fide cấp 1.' WHERE `ducvupro` = 214;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Fide cấp 2.' WHERE `ducvupro` = 215;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Fide cấp 3.' WHERE `ducvupro` = 216;
UPDATE `task_sub_template` SET `notify` = 'Đến gặp Sư phụ để báo cáo.' WHERE `ducvupro` = 217;

-- Task 24: Trunks tương lai
UPDATE `task_sub_template` SET `notify` = 'Báo cáo với ông.' WHERE `ducvupro` = 218;
UPDATE `task_sub_template` SET `notify` = 'Đi tìm người lạ đến từ tương lai.' WHERE `ducvupro` = 219;
UPDATE `task_sub_template` SET `notify` = 'Đưa thuốc trợ tim cho Quy Lão Kame.' WHERE `ducvupro` = 220;
UPDATE `task_sub_template` SET `notify` = 'Đến tương lai gặp Bunma tương lai.' WHERE `ducvupro` = 221;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 1000 Xên con cấp 1.' WHERE `ducvupro` = 222;
UPDATE `task_sub_template` SET `notify` = 'Báo với Bunma tương lai.' WHERE `ducvupro` = 223;

-- Task 25: Android 19, 20
UPDATE `task_sub_template` SET `notify` = 'Đến điểm hẹn tìm Robot sát thủ.' WHERE `ducvupro` = 224;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt số 2 (Android 19).' WHERE `ducvupro` = 225;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt số 1 (Android 20).' WHERE `ducvupro` = 226;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 900 Xên con cấp 3.' WHERE `ducvupro` = 227;
UPDATE `task_sub_template` SET `notify` = 'Báo với Bunma tương lai.' WHERE `ducvupro` = 228;

-- Task 26: Android 13, 14, 15
UPDATE `task_sub_template` SET `notify` = 'Đến sân sau siêu thị.' WHERE `ducvupro` = 229;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Android 15.' WHERE `ducvupro` = 230;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Android 14.' WHERE `ducvupro` = 231;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Android 13.' WHERE `ducvupro` = 232;
UPDATE `task_sub_template` SET `notify` = 'Báo với Bunma tương lai.' WHERE `ducvupro` = 233;

-- Task 27: Android 16, 17, 18
UPDATE `task_sub_template` SET `notify` = 'Đi tìm Android 16, 17, 18.' WHERE `ducvupro` = 234;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Android 18 (Poc).' WHERE `ducvupro` = 235;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Android 17 (Pic).' WHERE `ducvupro` = 236;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Android 16 (King Kong).' WHERE `ducvupro` = 237;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 800 Xên con cấp 5.' WHERE `ducvupro` = 238;
UPDATE `task_sub_template` SET `notify` = 'Báo với Bunma tương lai.' WHERE `ducvupro` = 239;

-- Task 28: Cell Forms
UPDATE `task_sub_template` SET `notify` = 'Đến thị trấn Ginger.' WHERE `ducvupro` = 240;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Xên Bọ Hung cấp 1.' WHERE `ducvupro` = 241;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Xên Bọ Hung cấp 2.' WHERE `ducvupro` = 242;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Xên Bọ Hung hoàn thiện.' WHERE `ducvupro` = 243;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 700 Xên con cấp 8.' WHERE `ducvupro` = 244;
UPDATE `task_sub_template` SET `notify` = 'Báo với Bunma tương lai.' WHERE `ducvupro` = 245;

-- Task 29: Cell Games
UPDATE `task_sub_template` SET `notify` = 'Nâng sức đánh gốc lên 10K và gặp Thần Mèo Karin.' WHERE `ducvupro` = 246;
UPDATE `task_sub_template` SET `notify` = 'Thu thập 50 Capsule kỳ bí.' WHERE `ducvupro` = 247;
UPDATE `task_sub_template` SET `notify` = 'Đến võ đài Xên Bọ Hung.' WHERE `ducvupro` = 248;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt 7 Xên con (Cell Jr).' WHERE `ducvupro` = 249;
UPDATE `task_sub_template` SET `notify` = 'Tiêu diệt Siêu Bọ Hung.' WHERE `ducvupro` = 250;
UPDATE `task_sub_template` SET `notify` = 'Báo với Bunma tương lai.' WHERE `ducvupro` = 251;

-- Task 30: Majin Buu Arc
UPDATE `task_sub_template` SET `notify` = 'Đi theo Osin.' WHERE `ducvupro` = 252;
UPDATE `task_sub_template` SET `notify` = 'Hạ vua địa ngục Dabura.' WHERE `ducvupro` = 253;
UPDATE `task_sub_template` SET `notify` = 'Hạ Pui Pui.' WHERE `ducvupro` = 254;
UPDATE `task_sub_template` SET `notify` = 'Hạ Pui Pui lần 2.' WHERE `ducvupro` = 255;
UPDATE `task_sub_template` SET `notify` = 'Hạ Yacôn.' WHERE `ducvupro` = 256;
UPDATE `task_sub_template` SET `notify` = 'Hạ Dabura lần 2.' WHERE `ducvupro` = 257;
UPDATE `task_sub_template` SET `notify` = 'Hạ Mabư.' WHERE `ducvupro` = 258;
UPDATE `task_sub_template` SET `notify` = 'Báo cáo với Osin.' WHERE `ducvupro` = 259;

-- Task 31: Planet Plant
UPDATE `task_sub_template` SET `notify` = 'Tìm nhẫn thời không từ Goku Black.' WHERE `ducvupro` = 260;
UPDATE `task_sub_template` SET `notify` = 'Sử dụng nhẫn thời không.' WHERE `ducvupro` = 261;
UPDATE `task_sub_template` SET `notify` = 'Tìm người Saiyan đang bị thương (Bardock).' WHERE `ducvupro` = 262;
UPDATE `task_sub_template` SET `notify` = 'Hạ 5000 Tobi và Cabira.' WHERE `ducvupro` = 263;
UPDATE `task_sub_template` SET `notify` = 'Nói chuyện với Bardock.' WHERE `ducvupro` = 264;
UPDATE `task_sub_template` SET `notify` = 'Tìm kiếm cậu bé Berry đi lạc.' WHERE `ducvupro` = 265;
UPDATE `task_sub_template` SET `notify` = 'Tìm 99 thức ăn cho Bardock.' WHERE `ducvupro` = 267;
UPDATE `task_sub_template` SET `notify` = 'Hạ 10000 Tobi và Cabira.' WHERE `ducvupro` = 268;
UPDATE `task_sub_template` SET `notify` = 'Nói chuyện với Bardock.' WHERE `ducvupro` = 269;

-- Task 32: Chiller
UPDATE `task_sub_template` SET `notify` = 'Chậm trán Chiller.' WHERE `ducvupro` = 270;
UPDATE `task_sub_template` SET `notify` = 'Quay về gặp Berry.' WHERE `ducvupro` = 271;
UPDATE `task_sub_template` SET `notify` = 'Hạ 20000 Tobi và Cabira.' WHERE `ducvupro` = 272;
UPDATE `task_sub_template` SET `notify` = 'Hạ Chiller.' WHERE `ducvupro` = 273;
UPDATE `task_sub_template` SET `notify` = 'Hạ Chiller 2.' WHERE `ducvupro` = 274;
UPDATE `task_sub_template` SET `notify` = 'Hạ Chiller bất kỳ 100 lần.' WHERE `ducvupro` = 275;
