package com.giga.nexas.dto.bsdx.bin.parser;

import com.giga.nexas.dto.bsdx.BsdxParser;
import com.giga.nexas.dto.bsdx.bin.Bin;
import com.giga.nexas.dto.bsdx.bin.consts.BinConst;
import com.giga.nexas.dto.bsdx.bin.consts.Opcode;
import com.giga.nexas.exception.ExceptionMsgConst;
import com.giga.nexas.exception.OperationException;
import com.giga.nexas.io.BinaryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 这位同学(Karaik)
 * @Date 2025/4/24
 */
public class BinParser implements BsdxParser<Bin> {

    Logger log = LoggerFactory.getLogger(BinParser.class);

    private final static String SKIP_BIN = "__GLOBAL";

    @Override
    public String supportExtension() {
        return "bin";
    }

    @Override
    public Bin parse(byte[] data, String filename, String charset) {

        if (SKIP_BIN.equals(filename)) {
            log.info("skip === {} ", filename);
            return new Bin();
        }

        BinaryReader reader = new BinaryReader(data, charset);

        Bin bin = new Bin();
        bin.setCharset(charset);

        // 预指令块
        int preCount = reader.readInt();
        bin.setPreCount(preCount);
        bin.setPreInstructions(preCount == 0 ? new byte[0] : reader.readBytes(preCount * 8));

        // 解析指令块、Commands 与入口点
        parseInstructions(reader, bin);

        // 解析字符串表
        parseStrings(reader, bin);

        // 解析属性表
        parseProperties(reader, bin);

        // 属性表2
        parseProperties2(reader, bin);

        // 解析常量表 & 68 字节表
        parseTablesAndConstants(reader, bin);

        // 将剩余未解析的尾随字节整体保留，便于无损写回
        if (reader.hasRemaining()) {
            int remain = reader.remaining();
            bin.tailRaw = reader.readBytes(remain);
            if (remain > 0) {
                log.debug("Captured trailing raw bytes: {}", remain);
            }
        }

        return bin;
    }

    // 解析指令 + Commands + EntryPoints
    private void parseInstructions(BinaryReader reader, Bin bin) {
        // 指令数量
        int count = reader.readInt();

        if (count > 200000) {
            log.info("parseInstructions count ===  {} ", count);
            throw new OperationException(500, ExceptionMsgConst.OE_LARGE_COUNT);
        }

        // 指令列表
        List<Bin.Instruction> instructions = new ArrayList<>(count);
        List<Integer> entryPointIndices = new ArrayList<>();

        // 遍历读取
        for (int i = 0; i < count; i++) {
            // 读取 opcodeNum
            int opcodeNum = reader.readInt();
            int operandNum = reader.readInt();

            String opcode = BinConst.OPCODE_MNEMONIC_MAP.get(opcodeNum) == null
                    ? String.valueOf(opcodeNum)
                    : BinConst.OPCODE_MNEMONIC_MAP.get(opcodeNum);

            Bin.Instruction inst = new Bin.Instruction();
            inst.setIndex(i);
            inst.setOpcodeNum(opcodeNum);
            inst.setOperandNum(operandNum);
            inst.setOpcode(opcode);

            boolean isCall = (opcodeNum == Opcode.CALL.code);
            if (isCall) {
                int paramCount = operandNum >>> 16;
                int nativeId = operandNum & 0xFFFF;
                String nativeFunc = BinConst.OPERAND_MNEMONIC_MAP.get(nativeId) == null
                        ? String.valueOf(nativeId)
                        : BinConst.OPERAND_MNEMONIC_MAP.get(nativeId);

                inst.setParamCount(paramCount);
                inst.setNativeId(nativeId);
                inst.setNativeFunction(nativeFunc);
            } else {
                inst.setParamCount(0);
                inst.setNativeFunction(String.valueOf(operandNum));
            }

            instructions.add(inst);

            // 0x1B 作为入口点标记
            if (opcodeNum == 0x1B) {
                entryPointIndices.add(i);
            }
        }

        bin.setInstructions(instructions);
        bin.entryPointIndices = entryPointIndices;

        // 兼容旧方式：按助记符再次收集入口点对象
        List<Bin.Instruction> entryPoints = new ArrayList<>();
        for (Bin.Instruction ins : instructions) {
            if (BinConst.OPCODE_MNEMONIC_MAP.get(0x1B).equals(ins.getOpcode())) {
                entryPoints.add(ins);
            }
        }
        bin.setEntryPoints(entryPoints);
    }

    private void parseStrings(BinaryReader reader, Bin bin) {
        int count = reader.readInt();

        if (count > 10000) {
            log.info("parseStrings count ===  {} ", count);
            throw new OperationException(500, ExceptionMsgConst.OE_LARGE_COUNT);
        }

        List<String> table = new ArrayList<>(count);
        List<byte[]> raws = new ArrayList<>(count);
        List<Integer> lens = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int start = reader.getPosition();
            while (reader.readByte() != 0) { }
            int end = reader.getPosition();
            reader.seek(start);
            int len = end - start; // 含终止0
            byte[] raw = reader.readBytes(len);
            String txt = new String(raw, reader.getCharset()).replace("\0", "");
            table.add(txt);
            raws.add(raw);
            lens.add(len);
        }

        bin.setStringTable(table);
        bin.stringTableRaw = raws;
        bin.stringTableLenWithTerminator = lens;
    }

    private void parseProperties(BinaryReader reader, Bin bin) {
        int count = reader.readInt();

        if (count > 10000) {
            log.info("parseProperties count ===  {} ", count);
            throw new OperationException(500, ExceptionMsgConst.OE_LARGE_COUNT);
        }

        List<String> properties = new ArrayList<>(count);
        List<byte[]> raws = new ArrayList<>(count);
        List<Integer> lens = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int start = reader.getPosition();
            while (reader.readByte() != 0) { }
            int end = reader.getPosition();
            reader.seek(start);
            int len = end - start; // 含终止0
            byte[] raw = reader.readBytes(len);
            String prop = new String(raw, reader.getCharset()).replace("\0", "");
            properties.add(prop);
            raws.add(raw);
            lens.add(len);
        }

        bin.setProperties(properties);
        bin.propertiesRaw = raws;
        bin.propertiesLenWithTerminator = lens;
    }

    private void parseProperties2(BinaryReader reader, Bin bin) {
        int count = reader.readInt();

        if (count > 10000) {
            log.info("parseProperties2 count ===  {} ", count);
            throw new OperationException(500, ExceptionMsgConst.OE_LARGE_COUNT);
        }

        List<String> props2 = new ArrayList<>(count);
        List<byte[]> raws = new ArrayList<>(count);
        List<Integer> lens = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int start = reader.getPosition();
            while (reader.readByte() != 0) { }
            int end = reader.getPosition();
            reader.seek(start);
            int len = end - start; // 含终止0
            byte[] raw = reader.readBytes(len);
            String p2 = new String(raw, reader.getCharset()).replace("\0", "");
            props2.add(p2);
            raws.add(raw);
            lens.add(len);
        }

        bin.setProperties2(props2);
        bin.properties2Raw = raws;
        bin.properties2LenWithTerminator = lens;
    }

    private void parseTablesAndConstants(BinaryReader reader, Bin bin) {
        int tableCount = reader.readInt();

        if (tableCount > 10000) {
            log.info("parseTablesAndConstants count ===  {} ", tableCount);
            throw new OperationException(500, ExceptionMsgConst.OE_LARGE_COUNT);
        }

        List<byte[]> tables = new ArrayList<>(tableCount);
        for (int i = 0; i < tableCount; i++) {
            tables.add(reader.readBytes(68));
        }
        bin.setTable(tables);

        Map<Integer, Integer[]> constants = new HashMap<>();
        for (byte[] tbl : tables) {
            BinaryReader tr = new BinaryReader(tbl);
            int index = tr.readInt();
            List<Integer> nums = new ArrayList<>();
            while (true) {
                int val = tr.readInt();
                if (val == 0xFFFFFFFF) {
                    break;
                }
                nums.add(val);
            }
            constants.put(index, nums.toArray(new Integer[0]));
        }
        bin.setConstants(constants);
    }
}
