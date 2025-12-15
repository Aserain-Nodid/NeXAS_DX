# bhe2bsdx 移植流程记录

## 我做了什么
- 拆包整理了 BHE 与 BSDX 的静态资源，按类型放在 `src/main/resources/game/*`，并把反编译得到的结构化 JSON 保存在 `grpBheJson`、`wazBheJson`、`spmBheJson` 等目录，方便对照调试。
- 为五类文件实现了解析/回写服务（`BheBinService`、`BsdxBinService`），并补全了对应的 DTO，使 grp/waz/spm/mek/dat 都能以 Java 对象形式操作。
- 起草了 `TransferTest#testPipeline` 与 `TransMeka`，搭建了从 BHE 取出机体资源、尝试映射到 BSDX 结构的试验流程，并整理了 83→72 槽位映射以及大量 `CEvent*` 的适配方法。
- 针对 SPM 新增的 hitbox、WAZ 的事件差异等关键点，写了转换辅助逻辑（例如 `transHitbox()`、`transBheCEventEffectToBsdx()`），为后续真实移植做准备。
- 补充了 `PacUtil` 等工具，预留了打包/解包测试环境，便于后续直接把结果写回 PAC 资源。

## 我之后必须要做的
- 让 `TransMeka.process` 真正把转换后的对象写回 BSDX 资源：更新 grp 中的机体/技能/语音注册表，并将新的 mek/waz/spm 追加到输出目录再用 `PacUtil.pack` 封包。
- 深拷贝 `BatVoice` 条目并校准语音索引：避免直接引用 BHE 的列表对象，同时确认 `MekVoiceInfo.version`、`voiceSlots` 与新增语音表的对应关系。
- 校正 `MekBasicInfo` 内的 `wazFileSequence`、`spmFileSequence` 等索引，重建 `MekMaterialBlock` 的三段资源引用，确保它与 BSDX 的资源编号一致。
- 完整搬运 BHE 的 `Tama01`~`Tama05`（含子弹、爆炸等依赖）并与 BSDX 的同号文件合并或替换，保证技能里的子弹引用能落到正确的 WAZ。
- 在 WAZ 转换中同步 `skillInfoUnknownList`，并填补当前映射为 `-1` 的槽位逻辑（例如 slot 52 CPU 特殊行动）；必要时实现兼容的降级或自定义事件。
- 重新计算每个技能的阶段数、槽位描述等元数据，确认 `phaseQuantity` 与实际写出的 phase 数一致，避免加载时出现越界。
- 给转换后的 SPM 重新挂到 BSDX 的 `spritegroup.grp`，并校验命名大小写、分页/贴图索引是否与菜单等界面资源一致。
- 针对 dat/csv，确认是否需要增量合并（如 pilot 列表、语音脚本），并准备自动化校验脚本来验证导入后的数据能在游戏内正常加载。
- 选取若干机体在真实二进制上回放测试，记录所有崩溃/缺失的事件，作为后续修复点。

## 当前流程存在的问题
- `transBatVoice` 仅做浅拷贝，`voiceList` 内的元素仍是 `com.giga.nexas.dto.bhe.grp.groupmap.BatVoiceGrp$BatVoice`，后续用 BSDX 的 `GrpGenerator` 会因类型不符报错；同时 BHE 结构里的 `unk0` 直接丢掉。
- `transMeka` 完全照搬 BHE 索引：例如 `tsukuyomi.mek` 的 `wazFileSequence` 仍为 11，但 BSDX 的 `wazagroup` 第 11 项是 `Chinatsu1`；`spmFileSequence=13` 对应 BSDX 的 `Aki`。`MekMaterialBlock` 里的 `spriteGroups` 依旧引用 BHE 的顺序（如 `tsukuyomi` 第一条是 `[0,0,0,1,0,2,0,3]`），而 BSDX 第 6 个开始就已经变成 `Link/Fire/...`，会挂错资源；`MekVoiceInfo.version` 被强行改成 30，也和现有 BSDX 机体的 0/5 不一致。
- `processWazaSkillUnitCollection` 只保留 `skillInfoObjectList`，任何只有 Unknown 段的数据都会被抛弃：`tsukuyomi.waz` 中 `立ち` Phase0 的 slot9/37 以及 `ブーストダッシュ（持続）` Phase0 的 slot80 都只剩 Unknown，因此转换后直接消失。
- 同一段逻辑完全没把 BHE 的 `skillInfoUnknownList` 拷回去，即便某个槽位同时存在事件和 Unknown，写出的 BSDX `SkillUnit` 也会缺少 Unknown 部分（诸如 `CEventEffect` 的附加参数）。
- 槽位映射里把 52 映射为 `-1`，实测 `tsukuyomi.waz` 有 16 个 slot52（`抜刀`/`納刀` 等动作）——这些 CPU 行动定义会直接被砍掉，需要补兼容实现而不是硬过滤。
- `TransMeka.process` 里传进来的 `mekaPilotSpm`、`selectMekaMenuMekaSpm` 没有任何处理，新增机体不会出现在选人界面的 SPM 表里；`bsdxBinService` 也没被用来生成/写盘。
