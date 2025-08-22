package com.giga.nexas.dto.bsdx.mek.parser;

import com.giga.nexas.dto.bsdx.BsdxParser;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.dto.bsdx.mek.checker.MekChecker;
import com.giga.nexas.dto.bsdx.mek.mekcpu.CCpuEvent;
import com.giga.nexas.dto.bsdx.mek.mekcpu.CCpuEventAttack;
import com.giga.nexas.dto.bsdx.mek.mekcpu.CCpuEventMove;
import com.giga.nexas.exception.OperationException;
import com.giga.nexas.io.BinaryReader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author 这位同学(Karaik)
 *
 * 另外感谢（排名不分先后）
 * @；）
 * @蓝色幻想
 * @柚木式子
 * 对逆向做出的贡献
 */
@Slf4j
public class MekParser implements BsdxParser<Mek> {

    @Override
    public String supportExtension() {
        return "mek";
    }

    @Override
    public Mek parse(byte[] bytes, String filename, String charset) {
        Mek mek = new Mek();
        try {
            // 1.解析头
            parseMekHead(mek, bytes);
            if (MekChecker.checkMek(mek, bytes)) {
                throw new OperationException(500, "invalid file header!");
            }
            mek.setFileName(filename);

            // 按 MekHead 分割数组
            Mek.MekHead mekHead = mek.getMekHead();
            byte[] bodyInfoBlock     = Arrays.copyOfRange(bytes, mekHead.getSequence1(), mekHead.getSequence2());
            byte[] unknownInfoBlock1 = Arrays.copyOfRange(bytes, mekHead.getSequence2(), mekHead.getSequence3());
            byte[] weaponInfoBlock   = Arrays.copyOfRange(bytes, mekHead.getSequence3(), mekHead.getSequence4());
            byte[] aiInfoBlock1      = Arrays.copyOfRange(bytes, mekHead.getSequence4(), mekHead.getSequence5());
            byte[] aiInfoBlock2      = Arrays.copyOfRange(bytes, mekHead.getSequence5(), mekHead.getSequence6());
            byte[] mekPluginBlock    = Arrays.copyOfRange(bytes, mekHead.getSequence6(), bytes.length);

            // 2.解析机体
            parseMekInfo(mek, bodyInfoBlock, charset);
            // 3.解析未知块1
            parseMekUnknownBlock1(mek, unknownInfoBlock1);
            // 4.解析武装块
            parseMekWeaponInfo(mek, weaponInfoBlock, charset);
            // 5.解析 ai 块1
            parseMekAiInfoBlock(mek, aiInfoBlock1, charset);
            // 6.解析 ai 块2
            parseMekAiInfoBlock2(mek, aiInfoBlock2, charset);
            // 7.解析武装插槽块
            parseMekMaterialBlock(mek, mekPluginBlock);

        } catch (Exception e) {
            log.info("error === {}", e.getMessage());
            throw e;
        }
        return mek;
    }

    /**
     * 解析 Mek 头
     */
    private static void parseMekHead(Mek mek, byte[] bytes) {
        Mek.MekHead mekHead = mek.getMekHead();
        BinaryReader reader = new BinaryReader(bytes);
        mekHead.setSequence1(reader.readInt());
        mekHead.setSequence2(reader.readInt());
        mekHead.setSequence3(reader.readInt());
        mekHead.setSequence4(reader.readInt());
        mekHead.setSequence5(reader.readInt());
        mekHead.setSequence6(reader.readInt());

        // 计算各块大小
        mek.getMekBlocks().calculateBlockSizes(mekHead);
    }

    /**
     * 解析机体信息块
     */
    private static void parseMekInfo(Mek mek, byte[] bytes, String charset) {
        Mek.MekBasicInfo mekInfo = mek.getMekBasicInfo();
        BinaryReader reader = new BinaryReader(bytes, charset);

        mekInfo.setMekName(reader.readNullTerminatedString());

        reader.setCharset("UTF-8");
        mekInfo.setMekNameEnglish(reader.readNullTerminatedString());

        reader.setCharset(charset);
        mekInfo.setPilotNameKanji(reader.readNullTerminatedString());

        reader.setCharset("UTF-8");
        mekInfo.setPilotNameRoma(reader.readNullTerminatedString());

        // 机体描述
        reader.setCharset(charset);
        mekInfo.setMekDescription(reader.readNullTerminatedString());

        mekInfo.setWazFileSequence(reader.readInt());
        mekInfo.setSpmFileSequence(reader.readInt());
        mekInfo.setMekType(reader.readInt());
        mekInfo.setHealthRecovery(reader.readInt());
        mekInfo.setForceOnKill(reader.readInt());
        mekInfo.setBaseHealth(reader.readInt());
        mekInfo.setEnergyIncreaseLevel1(reader.readInt());
        mekInfo.setEnergyIncreaseLevel2(reader.readInt());
        mekInfo.setBoosterLevel(reader.readInt());
        mekInfo.setBoosterIncreaseLevel(reader.readInt());
        mekInfo.setPermanentArmor(reader.readInt());
        mekInfo.setComboImpactFactor(reader.readInt());
        mekInfo.setFightingAbility(reader.readInt());
        mekInfo.setShootingAbility(reader.readInt());
        mekInfo.setDurability(reader.readInt());
        mekInfo.setMobility(reader.readInt());
        mekInfo.setPhysicsWeight(reader.readInt());
        mekInfo.setWalkingSpeed(reader.readInt());
        mekInfo.setNormalDashSpeed(reader.readInt());
        mekInfo.setSearchDashSpeed(reader.readInt());
        mekInfo.setBoostDashSpeed(reader.readInt());
        mekInfo.setAutoHoverHeight(reader.readInt());
    }

    /**
     * 解析未知块 1（暂存原始字节）
     */
    private static void parseMekUnknownBlock1(Mek mek, byte[] bytes) {
        // 前面的蛆，以后再探索吧
        mek.getMekUnknownBlock1().setInfo(bytes);
    }

    /**
     * 解析武装信息块
     */
    private static void parseMekWeaponInfo(Mek mek, byte[] bytes, String charset) {
        Map<Integer, Mek.MekWeaponInfo> mekWeaponInfoMap = mek.getMekWeaponInfoMap();
        BinaryReader reader = new BinaryReader(bytes, charset);

        int weaponCount = reader.readInt();
        for (int i = 0; i < weaponCount; i++) {

            // 起始符
            int flag = reader.readInt();
            if (flag != 1) {
                return;
            }

            Mek.MekWeaponInfo mekWeaponInfo = new Mek.MekWeaponInfo();
            // 记录绝对偏移
            mekWeaponInfo.setOffset(mek.getMekHead().getSequence3() + reader.getPosition());

            reader.setCharset(charset);
            mekWeaponInfo.setWeaponName(reader.readNullTerminatedString());
            mekWeaponInfo.setWeaponSequence(reader.readNullTerminatedString());
            mekWeaponInfo.setWeaponDescription(reader.readNullTerminatedString());

            mekWeaponInfo.setSwitchToMekNo(reader.readInt());
            mekWeaponInfo.setWazSequence(reader.readInt());
            mekWeaponInfo.setForceCrashAmount(reader.readInt());
            mekWeaponInfo.setHeatMaxConsumption(reader.readInt());
            mekWeaponInfo.setHeatMinConsumption(reader.readInt());
            mekWeaponInfo.setUpgradeExp(reader.readInt());
            mekWeaponInfo.setStartPointWhenDemonstrate(reader.readInt());
            mekWeaponInfo.setWeaponCategory(reader.readInt());
            mekWeaponInfo.setWeaponType(reader.readInt());
            mekWeaponInfo.setMeleeSkillFlag(reader.readInt());
            mekWeaponInfo.setColdWeaponSkillFlag(reader.readInt());
            mekWeaponInfo.setMissileSkillFlag(reader.readInt());
            mekWeaponInfo.setBulletCategorySkillFlag(reader.readInt());
            mekWeaponInfo.setOpticalWeaponSkillFlag(reader.readInt());
            mekWeaponInfo.setDroneSkillFlag(reader.readInt());
            mekWeaponInfo.setExplosiveSkillFlag(reader.readInt());
            mekWeaponInfo.setDefensiveWeaponSkillFlag(reader.readInt());
            mekWeaponInfo.setWeaponIdentifier(reader.readInt());
            mekWeaponInfo.setWeaponUnknownProperty19(reader.readInt());

            mekWeaponInfoMap.put(i, mekWeaponInfo);
        }
    }

    /**
     * ai信息块
     */
    private static void parseMekAiInfoBlock(Mek mek, byte[] bytes, String charset) {
        List<Mek.MekAiInfo> aiInfoList = mek.getMekAiInfoList();
        BinaryReader reader = new BinaryReader(bytes, charset);

        int aiCount = reader.readInt();
        for (int i = 0; i < aiCount; i++) {
            Mek.MekAiInfo mekAiInfo = new Mek.MekAiInfo();
            mekAiInfo.setAiTypeJapanese(reader.readNullTerminatedString());
            mekAiInfo.setAiTypeEnglish(reader.readNullTerminatedString());
            int actionCount = reader.readInt();
            for (int j = 0; j < actionCount; j++) {
                short type = reader.readShort();

                CCpuEvent event;
                if (type == 1) {
                    event = new CCpuEventMove();
                    event.readInfo(reader);
                } else if (type == 2) {
                    event = new CCpuEventAttack();
                    event.readInfo(reader);
                } else {
                    log.warn("未知AI子对象类型: {}, 偏移: {}", type, reader.getPosition());
                    event = new CCpuEvent();
                }

                event.setType(type);
                mekAiInfo.getCpuEventList().add(event);
            }
            aiInfoList.add(mekAiInfo);
        }
    }

    private static void parseMekAiInfoBlock2(Mek mek, byte[] bytes, String charset) {
        // 前面的蛆，以后再探索吧
        mek.getMekVoiceInfo().setInfo(bytes);
    }

    private static void parseMekMaterialBlock(Mek mek, byte[] blockBytes) {
        Mek.MekMaterialBlock out = new Mek.MekMaterialBlock();
        BinaryReader reader = new BinaryReader(blockBytes);

        // 收集所有条目（保持“文件顺序”）
        List<Mek.MekMaterialBlock.PluginEntry> all = new ArrayList<>();

        // 1) 固定 7 条
        for (int i = 0; i < 7; i++) {
            all.add(readPluginEntry(reader));
        }

        // 2) extra（等价 au_re_File::read_2）
        int extra = reader.readInt();
        if (extra < 0) {
            throw new OperationException(500, "invalid extraRegularCount: " + extra);
        }
        out.setExtraRegularCount(extra);

        // 3) 再读 extra 条
        for (int i = 0; i < extra; i++) {
            all.add(readPluginEntry(reader));
        }

        // 4) 直到 EOF；防御性：若读不到进度则中止，避免死循环
        while (reader.getPosition() < blockBytes.length) {
            int before = reader.getPosition();
            all.add(readPluginEntry(reader));
            if (reader.getPosition() <= before) {
                break;
            }
        }

        // 避免视图带来的并发修改问题
        int regular = 7 + extra;
        out.setRegularCount(regular);

        int split = Math.min(regular, all.size());
        List<Mek.MekMaterialBlock.PluginEntry> regularCopy  = new ArrayList<>(all.subList(0, split));
        List<Mek.MekMaterialBlock.PluginEntry> trailingCopy = new ArrayList<>(all.subList(split, all.size()));

        // entries为直接二进制映射，避免外部误改影响内部一致性
        out.setEntries(new ArrayList<>(all));
        out.setRegularEntries(regularCopy);
        out.setTrailingEntries(trailingCopy);

        mek.setMekMaterialBlock(out);
    }

    /** 读取一条 Material 条目， CMaterial::readArraysFromFile 的按字节实现） */
    private static Mek.MekMaterialBlock.PluginEntry readPluginEntry(BinaryReader reader) {
        int start = reader.getPosition();

        Mek.MekMaterialBlock.PluginEntry e = new Mek.MekMaterialBlock.PluginEntry();

        // 段 A：spriteGroups
        int n1 = reader.readInt();
        if (n1 < 0) {
            throw new OperationException(500, "negative group count A(spriteGroups) at " + start);
        }
        List<int[]> A = new ArrayList<>(n1);
        for (int i = 0; i < n1; i++) {
            int len = reader.readInt();
            if (len < 0) {
                throw new OperationException(500, "negative group len A(spriteGroups) at " + reader.getPosition());
            }
            int[] arr = new int[len];
            for (int k = 0; k < len; k++) {
                arr[k] = reader.readInt();
            }
            A.add(arr);
        }

        // 段 B：seGroups
        int n2 = reader.readInt();
        if (n2 < 0) {
            throw new OperationException(500, "negative group count B(seGroups) at " + reader.getPosition());
        }
        List<int[]> B = new ArrayList<>(n2);
        for (int i = 0; i < n2; i++) {
            int len = reader.readInt();
            if (len < 0) {
                throw new OperationException(500, "negative group len BseGroups at " + reader.getPosition());
            }
            int[] arr = new int[len];
            for (int k = 0; k < len; k++) {
                arr[k] = reader.readInt();
            }
            B.add(arr);
        }

        // 段 C：voiceGroups
        int n3 = reader.readInt();
        if (n3 < 0) {
            throw new OperationException(500, "negative group count C(voiceGroups) at " + reader.getPosition());
        }
        List<int[]> C = new ArrayList<>(n3);
        for (int i = 0; i < n3; i++) {
            int len = reader.readInt();
            if (len < 0) {
                throw new OperationException(500, "negative group len C(voiceGroups) at " + reader.getPosition());
            }
            int[] arr = new int[len];
            for (int k = 0; k < len; k++) {
                arr[k] = reader.readInt();
            }
            C.add(arr);
        }

        e.setSpriteGroups(A);
        e.setSeGroups(B);
        e.setVoiceGroups(C);

        e.setOffset(start);
        e.setLength(reader.getPosition() - start);
        return e;
    }

}
