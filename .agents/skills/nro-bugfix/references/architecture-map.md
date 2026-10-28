# NRO Codebase Architecture Map (Quick Reference)

## Directory → Module GEMINI Routing

| Directory | GEMINI Module | Trách Nhiệm |
|:----------|:-------------|:-------------|
| `src/services/` | `src/services/GEMINI.md` | Vật phẩm, đập đồ, NPC, shop, ký gửi, Smart Drop |
| `src/models/player/` | `src/models/player/GEMINI.md` | Player, NPoint, HP/KI/Dame, Skill, Pet |
| `src/models/map/` | `src/models/map/GEMINI.md` | Map, Zone, Mob AI, TileMap, chuyển map |
| `src/models/boss/` | `src/models/boss/GEMINI.md` | Boss lifecycle, AI, phase, reward |
| `src/models/tournament/` | `src/models/tournament/GEMINI.md` | Whis Arena, VDST, giải đấu, Bot |
| `src/models/task/` | `src/models/task/GEMINI.md` | Nhiệm vụ, danh hiệu, tiến trình |
| `src/network/` | `src/network/GEMINI.md` | Netty socket, Message, Session |
| `src/database/` | `src/database/GEMINI.md` | MySQL, HikariCP, DAO, query |
| `Client/.../Scripts/` | `Client/.../Scripts/GEMINI.md` | Unity C# client, UI, packet decode |

## Implicit Routing (src/models/ subfolders without own GEMINI.md)

| Subdirectory | Route To |
|:-------------|:---------|
| `npc/`, `combine/`, `item/`, `shop/`, `intrinsic/`, `clan/`, `phoban/`, `radar/` | `src/services/GEMINI.md` |
| `skill/` | `src/models/player/GEMINI.md` |
| `mob/` | `src/models/map/GEMINI.md` |
| `matches/` | `src/models/tournament/GEMINI.md` |

## Critical File Pairs (Server ↔ Client 1:1)

| Server Java | Client C# | Relationship |
|:------------|:----------|:-------------|
| `src/server/Controller.java` | `Controller.cs` | Packet dispatch — opcode handlers |
| `src/services/Service.java` | `Service.cs` | Packet construction — write↔read |
| `src/network/io/Message.java` | `myReader` / `myWriter` | Binary stream I/O |
| `src/models/player/Player.java` | `Char.cs` (God/Player/) | Player entity |
| `src/models/mob/Mob.java` | `Mobs.cs` (God/Mob/) | Mob entity |
| `src/models/map/Zone.java` | Map rendering logic | Zone state |

## Build & Verification Commands

```bash
# Server Java build
build.bat

# Full guardrails check (schema + invariant violations + performance alerts)
python tools/memory/guardrails.py --check-all

# Routing lookup by keyword
python tools/memory/guardrails.py --test-routing "<keyword>"

# Memory recall (past lessons)
python tools/memory/memory.py recall "<description>" --files "<file>"

# Stream protocol diff
python .agents/skills/nro-bugfix/scripts/stream_diff.py <opcode>

# Bug diagnosis
python .agents/skills/nro-bugfix/scripts/diagnose.py --symptom "<description>"

# Impact analysis before changing a method
python .agents/skills/nro-bugfix/scripts/impact.py <File.java> <methodName>
```

