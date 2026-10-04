# Chỉ đường tri thức cho `src/models/*`

File này tự nạp khi sửa bất kỳ file nào dưới `src/models/`. Nếu thư mục đang sửa **không có GEMINI.md riêng**, đọc module tương ứng bên dưới trước khi sửa:

| Thư mục | Module tri thức |
| :--- | :--- |
| `npc/`, `combine/`, `item/`, `shop/`, `intrinsic/`, `clan/`, `phoban/`, `radar/` | `src/services/GEMINI.md` (NPC Bà Hạt Mít → `CombineService`) |
| `skill/` | `src/models/player/GEMINI.md` |
| `mob/` | `src/models/map/GEMINI.md` |
| `matches/` | `src/models/tournament/GEMINI.md` |

Trước khi bắt đầu, nhớ lại bài học cũ: `python tools/memory/memory.py recall "<mô tả task>" --files <file đang sửa>`
