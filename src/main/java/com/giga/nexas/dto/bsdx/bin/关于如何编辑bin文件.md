| 字段                                  | 回写是否生效 | 备注                                                                                                       |
| ----------------------------------- | ------ | -------------------------------------------------------------------------------------------------------- |
| `preInstructions`                   | ✅      | 直接写回的字节块；建议长度为 8 的倍数。                                                                                    |
| `preCount`                          | ✅      | 会被校验：若与 `preInstructions.length/8` 不符，将被**纠正为长度/8**。                                                     |
| `instructions[*].opcode`            | ✅      | 助记符/数字字符串 **覆盖** `opcodeNum`（先查映射，查不到再按整数解析）。                                                            |
| `instructions[*].opcodeNum`         | ✅      | 兜底；当 `opcode` 可解析时以 `opcode` 为准。                                                                         |
| `instructions[*].paramCount`        | ✅      | **仅对 CALL 生效**，写入 operand 的高 16 位（`paramCount << 16`）。                                                   |
| `instructions[*].nativeFunction`    | ✅      | **CALL**：在 `nativeId` 为空时用于解析/映射低 16 位；**非 CALL**：作为立即数，**覆盖** `operandNum`。                             |
| `instructions[*].nativeId`          | ✅      | **CALL 优先**于 `nativeFunction`，决定 operand 的低 16 位。**非 CALL 忽略**。                                          |
| `instructions[*].operandNum`        | ✅      | **CALL** 时仅兜底（高位给 `paramCount`，低位在 `nativeId/nativeFunction` 都缺时使用）；**非 CALL** 在 `nativeFunction` 为空时使用。 |
| `instructions[*].index`             | ❌      | 仅分析/显示；写回按列表自然顺序，不依赖该值。                                                                                  |
| `stringTable`                       | ✅      | 作为**唯一来源**写回；逐项按 `charset` 编码并追加 `0x00` 终止。                                                              |
| `properties`                        | ✅      | 同上。                                                                                                      |
| `properties2`                       | ✅      | 同上。                                                                                                      |
| `table`                             | ✅      | 若非空则**原样优先**写回（每项自动裁/补为 68B）；同时**覆盖** `constants`。                                                       |
| `constants`                         | ✅      | **仅当 `table` 为空**时生效：按 key 升序重建 68B 表（`index`、若干值、`0xFFFFFFFF` 结束、0 填充）。                                 |
| `constants2`                        | ❌（当前）  | 预留，生成器未使用。                                                                                               |
| `tailRaw`                           | ✅      | 未知尾随段，**原样追加**，保证无损回写。                                                                                   |
| `charset`                           | ✅      | 仅影响 `stringTable/properties/properties2` 的编码；不影响 `table/integers`。                                       |
| `entryPoints` / `entryPointIndices` | ❌      | 仅分析/显示，不参与写回。                                                                                            |
| `extensionName`                     | ❌      | 元信息/显示用，生成器不读取。                                                                                          |
