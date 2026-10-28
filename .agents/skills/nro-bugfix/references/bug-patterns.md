# NRO Bug Pattern Quick Reference

## Common Bug Taxonomy

| ID | Loại Bug | Triệu Chứng Đặc Trưng | Nguyên Nhân Gốc | Module GEMINI |
|:---|:---------|:----------------------|:-----------------|:--------------|
| STREAM_DESYNC | Protocol | Dữ liệu rác, opcode sai, crash random | write/read không khớp 1:1 | `src/network/GEMINI.md` |
| DUPE_BUG | Security | Item nhân bản, số lượng sai | Thiếu lock trong Trade/Combine | `src/services/GEMINI.md` |
| OVERFLOW_INT | Math | Sức mạnh = 0, HP âm, hiển thị sai | int thay vì long | `src/models/player/GEMINI.md` |
| NPE_ZONE | Crash | Server crash, NullPointerException | player/mob rời zone đang iterate | `src/models/map/GEMINI.md` |
| UI_FREEZE | Client | Xoay tròn, đơ, đen màn hình | InfoDlg không release, changeMap sai | `Client/.../GEMINI.md` |
| GC_PAINT_ALLOC | Perf | FPS giật, micro-stutter | new/Substring trong paint() 60FPS | `Client/.../GEMINI.md` |
| DB_LEAK | Infra | Connection timeout 30s | try-with-resources thiếu | `src/database/GEMINI.md` |
| CS0117_UNITY | Build | Error CS0117 compile | Package version không tương thích | `Client/.../GEMINI.md` |
| BOSS_STUCK | Logic | Boss bất tử, không biến mất | leaveMap() thiếu, phase HP reset | `src/models/boss/GEMINI.md` |
| OPTION_DUPE | Data | Option item nhân đôi | parseItemSafe không clear trước | `src/services/GEMINI.md` |

## Decision Tree: Chọn Đúng Công Cụ

```
Bug Report đến
├── Có stacktrace? → diagnose.py --stacktrace "..."
├── Liên quan packet/opcode? → stream_diff.py <opcode>
├── Lỗi compile Unity CS0117? → diagnose.py --cs0117 "..."
├── Cần biết blast radius trước khi sửa? → impact.py <File> <Method>
└── Mô tả chung chung? → diagnose.py --symptom "..." + memory.py recall "..."
```

## Opcode Hay Gặp Bug

| Opcode | Chức năng | Bug thường gặp |
|:-------|:----------|:---------------|
| -27 | Version/Login handshake | Client treo WaitDlg nếu thiếu byte |
| -74 | Download game data | Nghẽn Netty thread nếu không cache RAM |
| -44 | Consign shop list | Thiếu writeUTF(sellerName) khi version >= 237 |
| -96 | BXH/Top ranking | Stream desync giữa typeTop và data fields |
| -100 | Shop ký gửi | Lệch byte stream seller name |
| 62 | Nâng cấp skill | Client readSByte() trả 0 khi EOF thay vì throw |

## Checklist Trước Khi Commit Fix

- [ ] `build.bat` chạy thành công (0 compile errors)
- [ ] `python tools/memory/guardrails.py --check-all` — PASS tất cả
- [ ] Nếu sửa packet: `stream_diff.py <opcode>` — đồng bộ 1:1
- [ ] Nếu sửa Player/NPoint: kiểm tra long thay vì int
- [ ] Nếu sửa Trade/Combine: kiểm tra synchronized/lock
- [ ] Nếu sửa DB query: kiểm tra try-with-resources
- [ ] Nếu sửa paint()/update(): kiểm tra zero-GC (không new trong loop)
- [ ] Nếu sửa Boss lifecycle: kiểm tra leaveMap() + removePlayer()
- [ ] `python tools/memory/memory.py recall "<mô tả bug>"` — có bài học cũ liên quan?

