# NRO Project Static Index Summary (Stage 1)
- Generated: `2026-11-26 09:30:17`
- **Server Java (`src/`)**: `596` files | `120,788` lines
- **Other Java (`test/`, `build/`, `tools/`)**: `10` files | `1,164` lines
- **Client Unity C#**: `247` files | `97,173` lines
- **Total Code Indexed**: `853` files | `219,125` lines
- **Worker Reading Batches (<= 6000 lines/batch)**: `48` batches
- **100% Coverage Verification**: `PASS (0 uncovered)`

## God Files Auto-Sliced (> 2,800 lines)
| File | Lines | Methods | Consts | Domain |
| :--- | ---: | ---: | ---: | :--- |
| `Client/Client/Assets/Scripts/Game1/Panel.cs` | 11,857 | 250 | 44 | `client_ui_god_mods` |
| `Client/Client/Assets/Scripts/Game1/Char.cs` | 8,754 | 186 | 28 | `client_gameplay_entities` |
| `Client/Client/Assets/Scripts/Game1/GameScr.cs` | 8,333 | 184 | 2 | `client_gameplay_entities` |
| `Client/Client/Assets/Scripts/Game1/Controller.cs` | 6,894 | 32 | 6 | `client_network_protocol` |
| `src/consts/ConstTask.java` | 6,359 | 0 | 6336 | `server_task_clan_consts` |
| `Client/Client/Assets/Scripts/Game1/GameCanvas.cs` | 3,599 | 92 | 1 | `client_core_render_audio` |
| `Client/Client/Assets/Scripts/Game1/Service.cs` | 3,285 | 154 | 0 | `client_network_protocol` |

## Worker Batches Plan (Stage 2 Fan-out)
| Batch ID | Domain | Lines | Chunks | Files / Ranges |
| :--- | :--- | ---: | ---: | :--- |
| `client_core_render_audio__p01` | `client_core_render_audio` | 5,676 | 5 | GameCanvas.cs:1-2851, GameCanvas.cs:2852-3599, Image.cs, Main.cs, mFont.cs |
| `client_core_render_audio__p02` | `client_core_render_audio` | 3,683 | 7 | mGraphics.cs, MotherCanvas.cs, mSystem.cs, MyAudioClip.cs, SmallImage.cs ... (+2 more) |
| `client_gameplay_entities__p01` | `client_gameplay_entities` | 5,460 | 24 | ActionChat.cs, ActionPaint.cs, ActionUpdate.cs, Archivement.cs, ArrayCast.cs ... (+19 more) |
| `client_gameplay_entities__p02` | `client_gameplay_entities` | 5,853 | 2 | Char.cs:1-3029, Char.cs:3030-5853 |
| `client_gameplay_entities__p03` | `client_gameplay_entities` | 5,783 | 18 | Char.cs:5854-8754, CharChild.cs, ChatBox.cs, ChatTextField.cs, ChooseCharScr.cs ... (+13 more) |
| `client_gameplay_entities__p04` | `client_gameplay_entities` | 4,598 | 24 | DefaultItemOptions.cs, Dialog.cs, EffecMn.cs, Effect.cs, Effect2.cs ... (+19 more) |
| `client_gameplay_entities__p05` | `client_gameplay_entities` | 2,835 | 1 | GameScr.cs:1-2835 |
| `client_gameplay_entities__p06` | `client_gameplay_entities` | 5,498 | 2 | GameScr.cs:2836-6013, GameScr.cs:6014-8333 |
| `client_gameplay_entities__p07` | `client_gameplay_entities` | 5,961 | 42 | Hint.cs, HTTPHandler.cs, IActionListener.cs, IChatable.cs, IKbAction.cs ... (+37 more) |
| `client_gameplay_entities__p08` | `client_gameplay_entities` | 5,910 | 21 | Mob.cs, MobCapcha.cs, MobTemplate.cs, MoneyCharge.cs, MonsterDart.cs ... (+16 more) |
| `client_gameplay_entities__p09` | `client_gameplay_entities` | 5,345 | 19 | Paint.cs, Part.cs, PartImage.cs, PKFlag.cs, PlayerDart.cs ... (+14 more) |
| `client_gameplay_entities__p10` | `client_gameplay_entities` | 5,504 | 26 | ServerScr.cs, Skill.cs, SkillInfoPaint.cs, SkillOption.cs, SkillOptionTemplate.cs ... (+21 more) |
| `client_gameplay_entities__p11` | `client_gameplay_entities` | 2,242 | 16 | TileMap.cs, Timer.cs, TopInfo.cs, TouchScreenKeyboard.cs, TouchScreenKeyboardType.cs ... (+11 more) |
| `client_network_protocol__p01` | `client_network_protocol` | 2,101 | 4 | Controller2.cs, ClanMessage.cs, Cmd.cs, NetworkOpcodes.cs |
| `client_network_protocol__p02` | `client_network_protocol` | 4,641 | 1 | Controller.cs:1-4641 |
| `client_network_protocol__p03` | `client_network_protocol` | 4,855 | 17 | Controller.cs:4642-6894, MapController.cs, MobController.cs, PetService.cs, Message.cs ... (+12 more) |
| `client_network_protocol__p04` | `client_network_protocol` | 4,497 | 4 | Service.cs:1-2805, Service.cs:2806-3285, Session_ME.cs, Session_ME2.cs |
| `client_ui_god_mods__p01` | `client_ui_god_mods` | 4,538 | 19 | ChatPopup.cs, Command.cs, Boss.cs, BossData.cs, AutoSettingManager.cs ... (+14 more) |
| `client_ui_god_mods__p02` | `client_ui_god_mods` | 5,643 | 2 | Panel.cs:1-2816, Panel.cs:2817-5643 |
| `client_ui_god_mods__p03` | `client_ui_god_mods` | 5,639 | 2 | Panel.cs:5644-8452, Panel.cs:8453-11282 |
| `client_ui_god_mods__p04` | `client_ui_god_mods` | 911 | 2 | Panel.cs:11283-11857, PopUp.cs |
| `server_boss_dungeon_pvp__p01` | `server_boss_dungeon_pvp` | 6,000 | 49 | BossManager.java, BrolyManager.java, ChristmasEventManager.java, FinalBossManager.java, GasDestroyManager.java ... (+44 more) |
| `server_boss_dungeon_pvp__p02` | `server_boss_dungeon_pvp` | 5,782 | 46 | SuperXaydaGod.java, TuanLoc.java, NhanBan.java, Chiller.java, Cooler.java ... (+41 more) |
| `server_boss_dungeon_pvp__p03` | `server_boss_dungeon_pvp` | 5,713 | 33 | BuiBui2.java, Cadic.java, Drabura.java, Drabura2.java, Drabura3.java ... (+28 more) |
| `server_boss_dungeon_pvp__p04` | `server_boss_dungeon_pvp` | 3,947 | 51 | SuperRank.java, TaoPaiPai.java, ChanXu.java, ChaPa.java, JackyChun.java ... (+46 more) |
| `server_boss_dungeon_pvp__p05` | `server_boss_dungeon_pvp` | 5,870 | 21 | BossesData.java, DHVT.java, LuyenTap.java, PKCommeson.java, ThachDau.java ... (+16 more) |
| `server_boss_dungeon_pvp__p06` | `server_boss_dungeon_pvp` | 5,820 | 23 | WhisArenaBossMatch.java, WhisArenaBot.java, WhisArenaBotManager.java, WhisArenaBotVsBotMatch.java, WhisArenaLobbyBot.java ... (+18 more) |
| `server_boss_dungeon_pvp__p07` | `server_boss_dungeon_pvp` | 552 | 3 | SuperRankService.java, The23rdMartialArtCongressService.java, WorldMartialArtsTournamentService.java |
| `server_database_persistence__p01` | `server_database_persistence` | 5,839 | 10 | AlyraManager.java, AlyraResultSet.java, EventDAO.java, HistoryTransactionDAO.java, NDVSqlFetcher.java ... (+5 more) |
| `server_database_persistence__p02` | `server_database_persistence` | 591 | 1 | ResultSetImpl.java |
| `server_economy_items__p01` | `server_economy_items` | 5,819 | 33 | ConsignShopManager.java, CheTaoDoThienSu.java, ChuyenHoaBangNgoc.java, ChuyenHoaBangVang.java, Combine.java ... (+28 more) |
| `server_economy_items__p02` | `server_economy_items` | 4,909 | 18 | Input.java, LuckyRound.java, CSMM.java, MuaCtService.java, SummonDragon.java ... (+13 more) |
| `server_economy_items__p03` | `server_economy_items` | 3,703 | 3 | ItemService.java, ShopService.java, TestConsignShopPerformance.java |
| `server_network_core__p01` | `server_network_core` | 5,062 | 36 | DataGame.java, ItemData.java, IBoss.java, IKeySessionHandler.java, IMessage.java ... (+31 more) |
| `server_network_core__p02` | `server_network_core` | 3,762 | 13 | Controller.java, GameLoopManager.java, ImageResizeManager.java, Maintenance.java, Manager.java ... (+8 more) |
| `server_player_combat__p01` | `server_player_combat` | 5,227 | 24 | Intrinsic.java, IntrinsicPlayer.java, Achievement.java, Character.java, Charms.java ... (+19 more) |
| `server_player_combat__p02` | `server_player_combat` | 5,882 | 19 | Player.java, PlayerEffect.java, RewardBlackBall.java, Satellite.java, SetClothes.java ... (+14 more) |
| `server_player_combat__p03` | `server_player_combat` | 1,816 | 2 | PlayerService.java, SkillService.java |
| `server_services_bot_telemetry__p01` | `server_services_bot_telemetry` | 5,955 | 24 | Bot.java, BotCold1.java, BotManager.java, BotNappa.java, BotState.java ... (+19 more) |
| `server_services_bot_telemetry__p02` | `server_services_bot_telemetry` | 5,988 | 16 | GiftCodeService.java, ItemTimeService.java, OpenPowerService.java, RadarService.java, RewardService.java ... (+11 more) |
| `server_services_bot_telemetry__p03` | `server_services_bot_telemetry` | 1,988 | 15 | BotTestLogger.java, CombineUtil.java, ErrorRes.java, FileIO.java, FileRunner.java ... (+10 more) |
| `server_task_clan_consts__p01` | `server_task_clan_consts` | 5,686 | 18 | AppearType.java, BossID.java, BossStatus.java, BossType.java, Cmd_message.java ... (+13 more) |
| `server_task_clan_consts__p02` | `server_task_clan_consts` | 5,041 | 27 | ConstTask.java:2801-5600, ConstTask.java:5601-6359, ConstTournament.java, SocketType.java, Event.java ... (+22 more) |
| `server_task_clan_consts__p03` | `server_task_clan_consts` | 2,425 | 1 | TaskService.java |
| `server_world_map_mob__p01` | `server_world_map_mob` | 5,963 | 33 | MapDataManager.java, TileMapDataLoader.java, Boss10.java, EffectMap.java, ItemMap.java ... (+28 more) |
| `server_world_map_mob__p02` | `server_world_map_mob` | 5,993 | 51 | NonInteractiveNPC.java, Npc.java, Appule.java, Babiday.java, BaHatMit.java ... (+46 more) |
| `server_world_map_mob__p03` | `server_world_map_mob` | 5,148 | 26 | Rong6Sao.java, Rong7Sao.java, RongOmega.java, RuongDo.java, Santa.java ... (+21 more) |
| `tooling_and_tests__p01` | `tooling_and_tests` | 1,471 | 12 | AntiLogin.java, GiftCode.java, Template.java, TestWhisArenaLogic.java, TestTlsContext.java ... (+7 more) |