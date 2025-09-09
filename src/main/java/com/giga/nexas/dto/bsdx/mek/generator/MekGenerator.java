package com.giga.nexas.dto.bsdx.mek.generator;

import com.giga.nexas.dto.bsdx.BsdxGenerator;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.dto.bsdx.mek.mekcpu.CCpuEvent;
import com.giga.nexas.exception.OperationException;
import com.giga.nexas.io.BinaryWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author 这位同学(Karaik)
 * @Date 2025/1/19
 * @Description MekGenerator
 */
public class MekGenerator implements BsdxGenerator<Mek> {

    @Override
    public String supportExtension() {
        return "mek";
    }

    @Override
    public void generate(String path, Mek mek, String charset) throws IOException {

        File originalFile   = new File(path);
        String parentDir    = originalFile.getParent();
        String fileNameBase = originalFile.getName().replaceFirst("\\.mek$", "");
        File newFile        = new File(parentDir, fileNameBase + ".mek");

        // 区块序列化为byte[]
        byte[] bodyInfoBlock     = serializeMekBasicInfo(mek, charset);
        byte[] pairBlock         = serializeMekPairBlock(mek, charset);
        byte[] weaponInfoBlock   = serializeMekWeaponInfoMap(mek, charset);
        byte[] aiInfoBlock       = serializeMekAiInfoMap(mek, charset);
        byte[] voiceInfoBlock    = serializeMekVoiceInfo(mek, charset);
        byte[] mekMaterialBlock  = serializeMekMaterialBlock(mek, charset);

        // 计算6个序列偏移
        int sequence1 = 24;
        int sequence2 = sequence1 + bodyInfoBlock.length;
        int sequence3 = sequence2 + pairBlock.length;
        int sequence4 = sequence3 + weaponInfoBlock.length;
        int sequence5 = sequence4 + aiInfoBlock.length;
        int sequence6 = sequence5 + voiceInfoBlock.length;

        try (FileOutputStream fos = new FileOutputStream(newFile);
             BinaryWriter writer  = new BinaryWriter(fos, charset)) {

            // head
            writer.writeInt(sequence1);
            writer.writeInt(sequence2);
            writer.writeInt(sequence3);
            writer.writeInt(sequence4);
            writer.writeInt(sequence5);
            writer.writeInt(sequence6);

            writer.writeBytes(bodyInfoBlock);
            writer.writeBytes(pairBlock);
            writer.writeBytes(weaponInfoBlock);
            writer.writeBytes(aiInfoBlock);
            writer.writeBytes(voiceInfoBlock);
            writer.writeBytes(mekMaterialBlock);
        }
    }

    // 2 meka基本信息
    private static byte[] serializeMekBasicInfo(Mek mek, String charset) throws IOException {
        Mek.MekBasicInfo mekBasicInfo = mek.getMekBasicInfo();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BinaryWriter writer = new BinaryWriter(baos, charset)) {

            writer.writeNullTerminatedString(mekBasicInfo.getMekName());
            writer.writeNullTerminatedString(mekBasicInfo.getMekNameEnglish());
            writer.writeNullTerminatedString(mekBasicInfo.getPilotNameKanji());
            writer.writeNullTerminatedString(mekBasicInfo.getPilotNameRoma());
            writer.writeNullTerminatedString(mekBasicInfo.getMekDescription());

            writer.writeInt(mekBasicInfo.getWazFileSequence());
            writer.writeInt(mekBasicInfo.getSpmFileSequence());
            writer.writeInt(mekBasicInfo.getMekType());
            writer.writeInt(mekBasicInfo.getHealthRecovery());
            writer.writeInt(mekBasicInfo.getForceOnKill());
            writer.writeInt(mekBasicInfo.getBaseHealth());
            writer.writeInt(mekBasicInfo.getEnergyIncreaseLevel1());
            writer.writeInt(mekBasicInfo.getEnergyIncreaseLevel2());
            writer.writeInt(mekBasicInfo.getBoosterLevel());
            writer.writeInt(mekBasicInfo.getBoosterIncreaseLevel());
            writer.writeInt(mekBasicInfo.getPermanentArmor());
            writer.writeInt(mekBasicInfo.getComboImpactFactor());
            writer.writeInt(mekBasicInfo.getFightingAbility());
            writer.writeInt(mekBasicInfo.getShootingAbility());
            writer.writeInt(mekBasicInfo.getDurability());
            writer.writeInt(mekBasicInfo.getMobility());
            writer.writeInt(mekBasicInfo.getPhysicsWeight());
            writer.writeInt(mekBasicInfo.getWalkingSpeed());
            writer.writeInt(mekBasicInfo.getNormalDashSpeed());
            writer.writeInt(mekBasicInfo.getSearchDashSpeed());
            writer.writeInt(mekBasicInfo.getBoostDashSpeed());
            writer.writeInt(mekBasicInfo.getAutoHoverHeight());

            writer.close();
            return baos.toByteArray();
        }
    }

    // 3 spm相关信息？
    private static byte[] serializeMekPairBlock(Mek mek, String charset) throws IOException {
        Mek.MekPairBlock mekPairBlock = mek.getMekPairBlock();
        List<Mek.MekPairBlock.Pair> unkPair = mekPairBlock.getUnkPair();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(baos, charset)) {
            for (int i = 0; i < 14; i++) {
                writer.writeInt(unkPair.get(i).getInt1());
                writer.writeInt(unkPair.get(i).getInt2());
            }
            writer.close();
            return baos.toByteArray();
        }
    }

    // 4 武装基本信息
    private static byte[] serializeMekWeaponInfoMap(Mek mek, String charset) throws IOException {
        Map<Integer, Mek.MekWeaponInfo> weaponInfoMap = mek.getMekWeaponInfoMap();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BinaryWriter writer = new BinaryWriter(baos, charset)) {

            writer.writeInt(weaponInfoMap.size());
            for (Mek.MekWeaponInfo weaponInfo : weaponInfoMap.values()) {
                writer.writeInt(0x01);
                writer.writeNullTerminatedString(weaponInfo.getWeaponName());
                writer.writeNullTerminatedString(weaponInfo.getWeaponSequence());
                writer.writeNullTerminatedString(weaponInfo.getWeaponDescription());

                writer.writeInt(weaponInfo.getSwitchToMekNo());
                writer.writeInt(weaponInfo.getWazSequence());
                writer.writeInt(weaponInfo.getForceCrashAmount());
                writer.writeInt(weaponInfo.getHeatMaxConsumption());
                writer.writeInt(weaponInfo.getHeatMinConsumption());
                writer.writeInt(weaponInfo.getUpgradeExp());
                writer.writeInt(weaponInfo.getStartPointWhenDemonstrate());
                writer.writeInt(weaponInfo.getWeaponCategory());
                writer.writeInt(weaponInfo.getWeaponType());
                writer.writeInt(weaponInfo.getMeleeSkillFlag());
                writer.writeInt(weaponInfo.getColdWeaponSkillFlag());
                writer.writeInt(weaponInfo.getMissileSkillFlag());
                writer.writeInt(weaponInfo.getBulletCategorySkillFlag());
                writer.writeInt(weaponInfo.getOpticalWeaponSkillFlag());
                writer.writeInt(weaponInfo.getDroneSkillFlag());
                writer.writeInt(weaponInfo.getExplosiveSkillFlag());
                writer.writeInt(weaponInfo.getDefensiveWeaponSkillFlag());
                writer.writeInt(weaponInfo.getWeaponIdentifier());
                writer.writeInt(weaponInfo.getWeaponUnknownProperty19());
            }

            writer.close();
            return baos.toByteArray();
        }
    }

    // 5 ai相关基本信息
    private static byte[] serializeMekAiInfoMap(Mek mek, String charset) throws IOException {
        List<Mek.MekAiInfo> aiInfos = mek.getMekAiInfoList();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BinaryWriter writer = new BinaryWriter(baos, charset)) {

            writer.writeInt(aiInfos.size());
            for (Mek.MekAiInfo ai : aiInfos) {
                writer.writeNullTerminatedString(ai.getAiTypeJapanese());
                writer.setCharset("UTF-8");
                writer.writeNullTerminatedString(ai.getAiTypeEnglish());
                writer.setCharset(charset);

                List<CCpuEvent> events = ai.getCpuEventList();

                writer.writeInt(events.size());
                for (CCpuEvent cpuEvent : events) {
                    short type = cpuEvent.getType();
                    writer.writeShort(type);
                    if (type == 1 || type == 2) {
                        cpuEvent.writeInfo(writer);
                    } else {
                        throw new OperationException(500, "unexpected AI type: " + type);
                    }
                }
            }
            writer.close();
            return baos.toByteArray();
        }
    }

    // 6 跟声音绑定的各种信息
    private static byte[] serializeMekVoiceInfo(Mek mek, String charset) throws IOException {
        Mek.MekVoiceInfo mekVoiceInfo = mek.getMekVoiceInfo();

        List<Mek.MekVoiceInfo.Emotion> emotions = mekVoiceInfo.getEmotions();
        List<Mek.MekVoiceInfo.VoiceSlot> voiceSlots = mekVoiceInfo.getVoiceSlots();
        List<List<List<Mek.MekVoiceInfo.Entry>>> table = mekVoiceInfo.getTable();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BinaryWriter writer = new BinaryWriter(baos, charset)) {

            // header
            writer.writeInt(mekVoiceInfo.getVersion());

            // emotions
            writer.writeInt(emotions.size());
            for (Mek.MekVoiceInfo.Emotion emotion : emotions) {
                writer.writeNullTerminatedString(emotion.getName());
                writer.writeNullTerminatedString(emotion.getToken());
            }

            // voiceSlots
            writer.writeInt(voiceSlots.size());
            for (Mek.MekVoiceInfo.VoiceSlot voiceSlot : voiceSlots) {
                writer.writeNullTerminatedString(voiceSlot.getName());
                writer.writeNullTerminatedString(voiceSlot.getToken());
            }

            // table：按行写出；每行固定写 voiceSlots.size() 个单元
            final int slotCount = voiceSlots.size();
            for (List<List<Mek.MekVoiceInfo.Entry>> row : table) {
                // 若该行列数不足，按 0 个三元组补齐；多出的列忽略
                for (int c = 0; c < slotCount; c++) {
                    List<Mek.MekVoiceInfo.Entry> cell = row.get(c);

                    writer.writeInt(cell.size());
                    for (Mek.MekVoiceInfo.Entry entry : cell) {
                        writer.writeInt(entry.getVoiceType() == null ? 0 : entry.getVoiceType());
                        writer.writeInt(entry.getGroupId() == null ? 0 : entry.getGroupId());
                        writer.writeInt(entry.getWeight() == null ? 0 : entry.getWeight());
                    }
                }
            }

            writer.close();
            return baos.toByteArray();
        }
    }

    // 7 武装选择界面的演示/素材表（MaterialBlock）
    private static byte[] serializeMekMaterialBlock(Mek mek, String charset) throws IOException {
        Mek.MekMaterialBlock block = mek.getMekMaterialBlock();
        if (block == null) {
            // 没这块就写空
            return new byte[0];
        }

        // 优先用 regularEntries 推导；否则用 regularCount；再否则用 extraRegularCount。
        final List<Mek.MekMaterialBlock.PluginEntry> regularEntries = block.getRegularEntries();
        final List<Mek.MekMaterialBlock.PluginEntry> trailingEntries = block.getTrailingEntries();
        final List<Mek.MekMaterialBlock.PluginEntry> allEntries = block.getEntries();

        final int regularCount;
        final int extra;

        if (regularEntries != null && !regularEntries.isEmpty()) {
            regularCount = regularEntries.size();
            if (regularCount < 7) {
                throw new OperationException(500, "materialBlock.regularEntries size < 7");
            }
            extra = regularCount - 7;
        } else if (block.getRegularCount() != null) {
            regularCount = block.getRegularCount();
            if (regularCount < 7) {
                throw new OperationException(500, "materialBlock.regularCount < 7");
            }
            extra = regularCount - 7;
        } else if (block.getExtraRegularCount() != null) {
            extra = block.getExtraRegularCount();
            if (extra < 0) {
                throw new OperationException(500, "materialBlock.extraRegularCount negative");
            }
            regularCount = 7 + extra;
        } else {
            // 不盲猜，直接报错，避免做错格式
            throw new OperationException(500, "materialBlock requires regularEntries/regularCount/extraRegularCount");
        }

        // 组装“写出顺序”：regular（7+extra）在前，trailing 在后
        final List<Mek.MekMaterialBlock.PluginEntry> stream = new java.util.ArrayList<>();

        if (regularEntries != null && !regularEntries.isEmpty()) {
            stream.addAll(regularEntries);
        } else {
            if (allEntries == null || allEntries.size() < regularCount) {
                throw new OperationException(500, "materialBlock.entries not enough for regularCount");
            }
            stream.addAll(allEntries.subList(0, regularCount));
        }

        if (trailingEntries != null && !trailingEntries.isEmpty()) {
            stream.addAll(trailingEntries);
        } else if (allEntries != null && allEntries.size() > regularCount) {
            stream.addAll(allEntries.subList(regularCount, allEntries.size()));
        }

        if (stream.size() < regularCount) {
            throw new OperationException(500, "materialBlock stream size < regularCount");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BinaryWriter writer = new BinaryWriter(baos, charset)) {

            // 1) 固定 7 条目
            for (int i = 0; i < 7; i++) {
                writePluginEntry(writer, stream.get(i));
            }

            // 2) extra（等价 au_re_File::read_2）
            writer.writeInt(extra);

            // 3) 再写 extra 条
            for (int i = 0; i < extra; i++) {
                writePluginEntry(writer, stream.get(7 + i));
            }

            // 4) 其余到 EOF
            for (int i = 7 + extra; i < stream.size(); i++) {
                writePluginEntry(writer, stream.get(i));
            }

            writer.close();
            return baos.toByteArray();
        }
    }

    private static void writePluginEntry(BinaryWriter writer, Mek.MekMaterialBlock.PluginEntry e) throws IOException {
        // A：spriteGroups
        writeGroupList(writer, e.getSpriteGroups());
        // B：seGroups
        writeGroupList(writer, e.getSeGroups());
        // C：voiceGroups
        writeGroupList(writer, e.getVoiceGroups());
    }

    private static void writeGroupList(BinaryWriter writer, List<int[]> groups) throws IOException {
        int groupCount = (groups == null) ? 0 : groups.size();
        writer.writeInt(groupCount);
        if (groupCount > 0) {
            for (int[] arr : groups) {
                int len = (arr == null) ? 0 : arr.length;
                writer.writeInt(len);
                for (int i = 0; i < len; i++) {
                    writer.writeInt(arr[i]);
                }
            }
        }
    }

}