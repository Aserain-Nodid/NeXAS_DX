# BHE → BSDX SPM HitBox 映射依据

为了在转换过程中尽量贴近 BSDX 原始命中体的分类，我们对 `src/main/resources/spmJson` 中 34 组与 BHE 同名的 SPM 进行统计（共 226k+ 条 `hitRect`），验证以下经验映射：

    shapeType=0 (旧格式默认矩形) → BSDX unk0=1
    shapeType=1 (CRotatableRect) → BSDX unk0=2
    shapeType=10 (CRotatableBox) → BSDX unk0=7

套用上述规则可正确匹配 BSDX 中 96.84% 的命中体类型（按实际条目加权）。剩余约 3% 出现在 `unk0=0/3/4/5/6` 等类别，推测与技能特效或特殊判定有关；在所有已解析的 BHE 资源中未发现对应标记，因此暂视为未被使用或需要额外信息才能匹配。

## 样本对照

- `kou.spm.json`
  - BHE：`shapeType=1` 20,990 条；`shapeType=10` 1,666 条。
  - BSDX：`unk0=2` 19,662 条；`unk0=7` 1,648 条；其余小量为 0/1/3/4/5/6。
- `gregory.spm.json`
  - BHE：`shapeType=1` 2,751 条；`shapeType=10` 91 条。
  - BSDX：`unk0=2` 2,614 条；`unk0=7` 88 条；其余为 0/1/3/4/6 等。
- `zako_201a.spm.json`
  - BHE：`shapeType=1` 377 条；`shapeType=10` 1 条。
  - BSDX：`unk0=2` 338 条；另有 `unk0=0/1` 共 40 条。

虽然仍有少量判定归类无法完整复原，但上述映射已覆盖绝大部分场景，因而在代码中作为默认映射使用。后续如发现新格式再行扩充即可。

最后更新：2025-09-17（基于当前全部样本）。
