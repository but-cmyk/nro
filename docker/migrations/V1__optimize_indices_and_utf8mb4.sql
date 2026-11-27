-- V1: Tối ưu hóa Index & Chuẩn hóa UTF8MB4 cho MySQL 8.0
-- An toàn để chạy lặp lại trên DB hiện có

-- 1. Index cho bảng player (tăng tốc lookup theo account_id khi login & queryTopSM)
CREATE INDEX idx_player_account_id ON player (account_id);

-- 2. Index cho bảng shop_ky_gui (tăng tốc truy vấn theo người bán & trạng thái)
CREATE INDEX idx_ky_gui_player_id ON shop_ky_gui (player_id);

-- 3. Index cho bảng history_transaction (tăng tốc tra cứu Admin Live Ops)
CREATE INDEX idx_history_tx_created ON history_transaction (created_at);
