package com.giga.nexas.dto.bsdx.bin.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.giga.nexas.dto.bsdx.BsdxGenerator;
import com.giga.nexas.dto.bsdx.bin.Bin;
import com.giga.nexas.dto.bsdx.bin.consts.BinConst;
import com.giga.nexas.dto.bsdx.bin.consts.Opcode;
import com.giga.nexas.io.BinaryWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * @Author 这位同学(Karaik)
 * @Date 2025/5/15
 * @Description BinGenerator
 */
@Slf4j
public class BinGenerator implements BsdxGenerator<Bin> {

    @Override
    public String supportExtension() {
        return "bin";
    }

    @Override
    public void generate(String path, Bin bin, String charsetName) throws IOException {

        // 路径准备
        FileUtil.mkdir(FileUtil.getParent(path, 1));

        String effectiveCharset = !StrUtil.isBlank(charsetName)
                ? charsetName
                : (StrUtil.isBlank(bin.getCharset()) ? "windows-31j" : bin.getCharset());

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(path));
             BinaryWriter writer = new BinaryWriter(os, effectiveCharset)) {

            // 1) 预指令块：preCount + preInstructions(preCount * 8)
            writePreInstructions(writer, bin);

            // 2) 指令块：count + for each {opcodeNum, operandNum}
            writeInstructions(writer, bin);

            // 3) 字符串表：count + N 个 0 终止串（优先 raw，无则重编码）
            writeNullTerminatedTable(writer, bin.getStringTable(), bin.stringTableRaw);

            // 4) 属性表1：count + N 个 0 终止串（优先 raw）
            writeNullTerminatedTable(writer, bin.getProperties(), bin.propertiesRaw);

            // 5) 属性表2：count + N 个 0 终止串（优先 raw）
            writeNullTerminatedTable(writer, bin.getProperties2(), bin.properties2Raw);

            // 6) 68 字节表：tableCount + tableCount * 68 字节
            //    - 优先直接写回解析时保留的原始表
            //    - 若原始表缺失但有 constants，则根据 constants 重建每张 68 字节表
            writeTables(writer, bin);

            // 原样写回未解析的尾随原始字节
            if (bin.tailRaw != null && bin.tailRaw.length > 0) {
                writer.writeBytes(bin.tailRaw);
            }

        } catch (Exception e) {
            log.info("path === {}", path);
            log.info("error === {}", e.getMessage(), e);
            throw e;
        }
    }

    private void writePreInstructions(BinaryWriter writer, Bin bin) throws IOException {
        byte[] pre = bin.getPreInstructions();
        int effectivePreCount;

        if (pre == null || pre.length == 0) {
            writer.writeInt(0);
            return;
        }

        // 如果对象里有显式 preCount，尝试校验；否则用长度/8 推断
        Integer preCount = bin.getPreCount();
        if (preCount == null) {
            if (pre.length % 8 != 0) {
                log.warn("preInstructions length({}) is not multiple of 8, raw write anyway.", pre.length);
            }
            effectivePreCount = pre.length / 8;
        } else {
            effectivePreCount = preCount;
            int expectedLen = preCount * 8;
            if (expectedLen != pre.length) {
                log.warn("preCount({}) * 8 != preInstructions.length({}), adjust to length/8.",
                        preCount, pre.length);
                effectivePreCount = pre.length / 8;
            }
        }

        writer.writeInt(effectivePreCount);
        if (effectivePreCount > 0) {
            writer.writeBytes(pre);
        }
    }

    private void writeInstructions(BinaryWriter writer, Bin bin) throws IOException {
        List<Bin.Instruction> list = bin.getInstructions();
        int count = (list == null) ? 0 : list.size();

        writer.writeInt(count);
        if (count == 0) return;

        for (int i = 0; i < count; i++) {
            Bin.Instruction inst = list.get(i);

            // 解析 opcode 编号（优先 opcodeNum，若缺失则根据助记符/数字字符串推断）
            int opcodeNum = resolveOpcodeNum(inst);

            // 解析 operand（CALL 需要根据 paramCount/nativeId 重新打包；其他直接取整数）
            int operandNum = resolveOperandNum(opcodeNum, inst);

            writer.writeInt(opcodeNum);
            writer.writeInt(operandNum);
        }
    }

    /**
     * 统一写入 0 终止的“字符串表”类结构：
     * - 先写 count
     * - 每项优先使用 parser 保存下来的 raw（含终止0），保证无损
     * - 若 raw 缺失或为 null，则用 writer 的 charset 重编码写入，并补终止0
     */
    private void writeNullTerminatedTable(BinaryWriter writer,
                                          List<String> textList,
                                          List<byte[]> rawList) throws IOException {

        int a = (textList == null) ? 0 : textList.size();
        int b = (rawList == null) ? 0 : rawList.size();
        int count = Math.max(a, b);

        writer.writeInt(count);

        for (int i = 0; i < count; i++) {
            byte[] raw = (rawList != null && i < rawList.size()) ? rawList.get(i) : null;
            if (raw != null) {
                writer.writeBytes(raw);
                continue;
            }

            String s = (textList != null && i < textList.size()) ? textList.get(i) : "";
            writer.writeNullTerminatedString(s == null ? "" : s);
        }
    }

    private void writeTables(BinaryWriter writer, Bin bin) throws IOException {
        List<byte[]> tables = bin.getTable();

        if (tables != null && !tables.isEmpty()) {
            // 直接无损写回
            writer.writeInt(tables.size());
            for (byte[] t : tables) {
                if (t == null) {
                    // 空洞也写 68 字节全 0，避免结构错位
                    writer.writeBytes(new byte[68]);
                } else if (t.length != 68) {
                    log.warn("table entry length != 68 (was {}), will pad/truncate.", t.length);
                    writer.writeBytes(fit68(t));
                } else {
                    writer.writeBytes(t);
                }
            }
            return;
        }

        // 原始 68 字节表缺失 —— 尝试根据 constants 重建
        Map<Integer, Integer[]> constants = bin.getConstants();
        if (constants == null || constants.isEmpty()) {
            // 两者都没有，就写一个空的 tableCount
            writer.writeInt(0);
            return;
        }

        // 为了稳定性，按 key 升序生成
        List<Map.Entry<Integer, Integer[]>> entries = new ArrayList<>(constants.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getKey));

        writer.writeInt(entries.size());
        for (Map.Entry<Integer, Integer[]> e : entries) {
            int index = e.getKey();
            Integer[] vals = e.getValue();

            // 68 字节格式：index(int) + vals... + 0xFFFFFFFF 结束 + 余量 0 填充
            ByteBuffer buf = ByteBuffer.allocate(68).order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(index);
            if (vals != null) {
                for (Integer v : vals) {
                    if (buf.position() + 4 > 68 - 4) { // 保证还能放结束标记
                        log.warn("constants for index {} too long, truncating to fit 68 bytes.", index);
                        break;
                    }
                    buf.putInt(v == null ? 0 : v);
                }
            }
            buf.putInt(0xFFFFFFFF);
            while (buf.position() < 68) buf.put((byte) 0);

            writer.writeBytes(buf.array());
        }
    }

    private int resolveOpcodeNum(Bin.Instruction inst) {
        // 优先使用 opcodeNum（解析阶段已保存）
        // 但为了容忍手动编辑过助记符，这里做个兜底一致化
        int fromNum = inst.getOpcodeNum();

        String mnemonic = inst.getOpcode();
        if (mnemonic == null) {
            return fromNum;
        }

        Integer mapNum = BinConst.MNEMONIC_OPCODE_MAP.get(mnemonic);
        if (mapNum != null) {
            // 若两者不一致，倾向使用用户编辑后的助记符对应值，同时记录一下
            if (mapNum != fromNum) {
                log.debug("opcode mismatch: opcodeNum={} mnemonic({})=>{}; choose mnemonic mapping.",
                        fromNum, mnemonic, mapNum);
            }
            return mapNum;
        }

        // 助记符不是已知关键字，可能是纯数字字符串
        try {
            int parsed = Integer.parseInt(mnemonic);
            if (parsed != fromNum) {
                log.debug("opcode numeric-string override: {} -> {}", fromNum, parsed);
            }
            return parsed;
        } catch (NumberFormatException ignore) {
            // 既非关键字也非数字串，就用原始 opcodeNum
            return fromNum;
        }
    }

    private int resolveOperandNum(int opcodeNum, Bin.Instruction inst) {
        // CALL：operand = (paramCount << 16) | (nativeId & 0xFFFF)
        if (opcodeNum == Opcode.CALL.code) {
            int paramCount = (inst.getParamCount() != null) ? inst.getParamCount() : (inst.getOperandNum() >>> 16);

            Integer nativeIdObj = inst.nativeId;
            int nativeId;

            if (nativeIdObj != null) {
                nativeId = nativeIdObj;
            } else if (!StrUtil.isBlank(inst.getNativeFunction())) {
                Integer mapped = BinConst.MNEMONIC_OPERAND_MAP.get(inst.getNativeFunction());
                if (mapped != null) {
                    nativeId = mapped;
                } else {
                    // 不是关键字，就尝试按数字解析
                    nativeId = parseIntSafe(inst.getNativeFunction(), inst.getOperandNum() & 0xFFFF);
                }
            } else {
                // 都没有，以原始 operandNum 低16位为准
                nativeId = inst.getOperandNum() & 0xFFFF;
            }

            return ((paramCount & 0xFFFF) << 16) | (nativeId & 0xFFFF);
        }

        // 非CALL：operand大多为立即数，优先使用对象层operandNum；
        // 若编辑了nativeFunction（多数场景是数字字符串），则以编辑后的值覆盖。
        if (!StrUtil.isBlank(inst.getNativeFunction())) {
            Integer mapped = BinConst.MNEMONIC_OPERAND_MAP.get(inst.getNativeFunction());
            if (mapped != null) {
                return mapped; // 极少数非CALL也可能复用“命名操作数”
            }
            return parseIntSafe(inst.getNativeFunction(), inst.getOperandNum());
        }

        return inst.getOperandNum();
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ignore) {
            return fallback;
        }
    }

    private byte[] fit68(byte[] in) {
        if (in.length == 68) return in;
        byte[] out = new byte[68];
        if (in.length > 68) {
            System.arraycopy(in, 0, out, 0, 68);
        } else {
            System.arraycopy(in, 0, out, 0, in.length);
            // 其余填 0
        }
        return out;
    }
}
