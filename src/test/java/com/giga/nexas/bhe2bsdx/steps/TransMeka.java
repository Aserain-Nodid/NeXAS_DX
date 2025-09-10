package com.giga.nexas.bhe2bsdx.steps;

import cn.hutool.core.bean.BeanUtil;
import com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.dto.bsdx.waz.Waz;
import com.giga.nexas.dto.bsdx.waz.wazfactory.SkillInfoFactory;
import com.giga.nexas.dto.bsdx.waz.wazfactory.wazinfoclass.SkillUnit;
import com.giga.nexas.dto.bsdx.waz.wazfactory.wazinfoclass.obj.*;
import com.giga.nexas.service.BsdxBinService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TransMeka {

    private final BsdxBinService bsdxBinService = new BsdxBinService();

    public static void process(
                        com.giga.nexas.dto.bhe.mek.Mek bheMek,
                        com.giga.nexas.dto.bhe.waz.Waz bheWaz,
                        com.giga.nexas.dto.bhe.spm.Spm bheSpm,

                        com.giga.nexas.dto.bhe.grp.groupmap.BatVoiceGrp.BatVoiceGroup tsukuyomiBatvoice,
                        com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp bsdxBatVoice,

                        com.giga.nexas.dto.bsdx.spm.Spm mekaPilotSpm,
                        com.giga.nexas.dto.bsdx.spm.Spm selectMekaMenuMekaSpm
    ) {
        // add to batvoice
        BatVoiceGrp.BatVoiceGroup bsdxBatVoiceGroup = transBatVoice(tsukuyomiBatvoice); // 30
        bsdxBatVoice.getVoiceList().add(bsdxBatVoiceGroup);

        // meka
        Mek bsdxMeka = transMeka(bheMek);
        // waza
        Waz bsdxWaz = transWaza(bheWaz);

        //
        log.info("");
    }

    private static com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp.BatVoiceGroup transBatVoice(com.giga.nexas.dto.bhe.grp.groupmap.BatVoiceGrp.BatVoiceGroup bheBatVoiceGrp) {
        BatVoiceGrp.BatVoiceGroup bsdxBatVoiceGroup = new BatVoiceGrp.BatVoiceGroup();

        // a->b
        BeanUtil.copyProperties(bheBatVoiceGrp, bsdxBatVoiceGroup);

        return bsdxBatVoiceGroup;
    }

    private static com.giga.nexas.dto.bsdx.mek.Mek transMeka(com.giga.nexas.dto.bhe.mek.Mek bheMek) {
        Mek bsdxMek = new Mek();

        BeanUtil.copyProperties(bheMek, bsdxMek);

        // batVoice
        bsdxMek.getMekVoiceInfo().setVersion(30);

        return bsdxMek;
    }

    private static com.giga.nexas.dto.bsdx.waz.Waz transWaza(com.giga.nexas.dto.bhe.waz.Waz bheWaz) {
        Waz bsdxWaz = new Waz();

        processWazaSkillUnitCollection(bsdxWaz, bheWaz);

        return bsdxWaz;
    }

    // unitQuantity与实际情况不符，需额外处理
    private static void processWazaSkillUnitCollection(
            com.giga.nexas.dto.bsdx.waz.Waz bsdxWaz,
            com.giga.nexas.dto.bhe.waz.Waz  bheWaz
    ) {
        bsdxWaz.setFileName(bheWaz.getFileName());
        bsdxWaz.setExtensionName(bheWaz.getExtensionName());

        // 类对应表
        Map<Integer, Integer> bheToBsdxSlotMap = bheToBsdxSlotMap();
        // 目标技能列表
        List<com.giga.nexas.dto.bsdx.waz.Waz.Skill> dstSkills = new ArrayList<>();

        // for each Skill
        for (com.giga.nexas.dto.bhe.waz.Waz.Skill srcSkill : bheWaz.getSkillList()) {
            com.giga.nexas.dto.bsdx.waz.Waz.Skill dstSkill = new com.giga.nexas.dto.bsdx.waz.Waz.Skill();

            dstSkill.setPhaseQuantity(srcSkill.getPhaseQuantity());
            dstSkill.setSkillNameJapanese(srcSkill.getSkillNameJapanese());
            dstSkill.setSkillNameEnglish(srcSkill.getSkillNameEnglish());

            // suffix 结构一一拷贝
            if (srcSkill.getSkillSuffixList() != null) {
                List<com.giga.nexas.dto.bsdx.waz.Waz.Skill.SkillSuffix> dstSuffix = new ArrayList<>();
                for (com.giga.nexas.dto.bhe.waz.Waz.Skill.SkillSuffix s : srcSkill.getSkillSuffixList()) {
                    com.giga.nexas.dto.bsdx.waz.Waz.Skill.SkillSuffix t = new com.giga.nexas.dto.bsdx.waz.Waz.Skill.SkillSuffix();
                    BeanUtil.copyProperties(s, t);
                    dstSuffix.add(t);
                }
                dstSkill.setSkillSuffixList(dstSuffix);
            }

            // phasesInfo 重建
            List<Waz.Skill.SkillPhase> dstPhases = new ArrayList<>();
            for (com.giga.nexas.dto.bhe.waz.Waz.Skill.SkillPhase srcPhase : srcSkill.getPhasesInfo()) {

                Waz.Skill.SkillPhase dstPhase = new Waz.Skill.SkillPhase();
                for (com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.SkillUnit srcSkillUnit : srcPhase.getSkillUnitCollection()) {
                    // 1) 读取 BHE 槽位（此处你的 UnitQuantity 用作槽位号）
                    Integer bheSlot = srcSkillUnit.getUnitQuantity();

                    // 2) 槽位映射：BHE -> BSDX
                    Integer bsdxSlot = bheToBsdxSlotMap.get(bheSlot);

                    // 3) 无映射或越界则丢弃该单元
                    if (bsdxSlot == null || bsdxSlot < 0 || bsdxSlot >= 72) {
                        continue;
                    }

                    // 4) 新建 BSDX 的 SkillUnit（不预设，逐个 new）
                    SkillUnit dstUnit = new SkillUnit();

                    // 5) 记录目标槽位到单元（你当前把 unitQuantity 用作槽位记录）
                    dstUnit.setUnitQuantity(bsdxSlot);

                    // 6) 目标事件列表
                    List<SkillInfoObject> dstInfos = new ArrayList<>();

                    // 7) 源事件列表（BHE）
                    List<com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.SkillInfoObject> srcInfos =
                            srcSkillUnit.getSkillInfoObjectList();

                    // 8) 源事件为空：写回空列表并添加该单元
                    if (srcInfos == null || srcInfos.isEmpty()) {

                        dstUnit.setSkillInfoObjectList(dstInfos);

                        dstPhase.getSkillUnitCollection().add(dstUnit);

                        continue;
                    }

                    // 9) 特例：BHE 槽位 37（汎用変数）→ BSDX 工厂会返回 CEventVal，这里仅注释说明
                    //    if (bheSlot == 37) { /* BHE:CEventFreeParam -> BSDX:CEventVal */ }

                    // 10) 将每个 BHE 事件用 BSDX 工厂按“BSDX 槽位”创建正确子类并拷贝共有字段
                    for (com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.SkillInfoObject srcInfo : srcInfos) {

                        SkillInfoObject dstInfo = SkillInfoFactory.createEventObjectBsdx(bsdxSlot);

                        // 槽位号同步
                        dstInfo.setSlotNum(bsdxSlot);

                        // 按具体子类进行 BeanCopy（BSDX 类已导入，使用简单类名便于阅读）
                        if (dstInfo instanceof CEventSpriteAttr) {
                            BeanUtil.copyProperties(srcInfo, (CEventSpriteAttr) dstInfo);
                        } else if (dstInfo instanceof CEventCpuButton) {
                            BeanUtil.copyProperties(srcInfo, (CEventCpuButton) dstInfo);
                        } else if (dstInfo instanceof CEventVoice) {
                            BeanUtil.copyProperties(srcInfo, (CEventVoice) dstInfo);
                        } else if (dstInfo instanceof CEventRadialLine) {
                            BeanUtil.copyProperties(srcInfo, (CEventRadialLine) dstInfo);
                        } else if (dstInfo instanceof CEventValRandom) {
                            BeanUtil.copyProperties(srcInfo, (CEventValRandom) dstInfo);
                        } else if (dstInfo instanceof CEventMove) {
                            BeanUtil.copyProperties(srcInfo, (CEventMove) dstInfo);
                        } else if (dstInfo instanceof CEventSe) {
                            BeanUtil.copyProperties(srcInfo, (CEventSe) dstInfo);
                        } else if (dstInfo instanceof CEventTouch) {
                            BeanUtil.copyProperties(srcInfo, (CEventTouch) dstInfo);
                        } else if (dstInfo instanceof CEventEffect) {
                            BeanUtil.copyProperties(srcInfo, (CEventEffect) dstInfo);
                        } else if (dstInfo instanceof CEventScreenLine) {
                            BeanUtil.copyProperties(srcInfo, (CEventScreenLine) dstInfo);
                        } else if (dstInfo instanceof CEventSlipHosei) {
                            BeanUtil.copyProperties(srcInfo, (CEventSlipHosei) dstInfo);
                        } else if (dstInfo instanceof CEventVal) {
                            BeanUtil.copyProperties(srcInfo, (CEventVal) dstInfo);
                        } else if (dstInfo instanceof CEventBlur) {
                            BeanUtil.copyProperties(srcInfo, (CEventBlur) dstInfo);
                        } else if (dstInfo instanceof CEventCharge) {
                            BeanUtil.copyProperties(srcInfo, (CEventCharge) dstInfo);
                        } else if (dstInfo instanceof CEventScreenAttr) {
                            BeanUtil.copyProperties(srcInfo, (CEventScreenAttr) dstInfo);
                        } else if (dstInfo instanceof CEventTerm) {
                            BeanUtil.copyProperties(srcInfo, (CEventTerm) dstInfo);
                        } else if (dstInfo instanceof CEventEscape) {
                            BeanUtil.copyProperties(srcInfo, (CEventEscape) dstInfo);
                        } else if (dstInfo instanceof CEventScreenEffect) {
                            BeanUtil.copyProperties(srcInfo, (CEventScreenEffect) dstInfo);
                        } else if (dstInfo instanceof CEventHit) {
                            BeanUtil.copyProperties(srcInfo, (CEventHit) dstInfo);
                        } else if (dstInfo instanceof CEventStatus) {
                            BeanUtil.copyProperties(srcInfo, (CEventStatus) dstInfo);
                        } else if (dstInfo instanceof CEventChange) {
                            BeanUtil.copyProperties(srcInfo, (CEventChange) dstInfo);
                        } else if (dstInfo instanceof CEventSprite) {
                            BeanUtil.copyProperties(srcInfo, (CEventSprite) dstInfo);
                        } else if (dstInfo instanceof CEventHeight) {
                            BeanUtil.copyProperties(srcInfo, (CEventHeight) dstInfo);
                        } else if (dstInfo instanceof CEventCamera) {
                            BeanUtil.copyProperties(srcInfo, (CEventCamera) dstInfo);
                        } else if (dstInfo instanceof CEventScreenYure) {
                            BeanUtil.copyProperties(srcInfo, (CEventScreenYure) dstInfo);
                        } else if (dstInfo instanceof CEventSpriteYure) {
                            BeanUtil.copyProperties(srcInfo, (CEventSpriteYure) dstInfo);
                        } else if (dstInfo instanceof CEventBlink) {
                            BeanUtil.copyProperties(srcInfo, (CEventBlink) dstInfo);
                        } else {
                            // 兜底：父类到父类的共有字段拷贝
                            BeanUtil.copyProperties(srcInfo, dstInfo);
                        }

                        // 收集该条 BSDX 事件
                        dstInfos.add(dstInfo);
                    }

                    // 11) 写回该单元的事件集合
                    dstUnit.setSkillInfoObjectList(dstInfos);

                    // 12) 将该单元加入当前 Phase
                    dstPhase.getSkillUnitCollection().add(dstUnit);
                }
                if (dstPhase.getSkillUnitCollection().size()!=0){
                    dstPhases.add(dstPhase);
                }
            }

            dstSkill.setPhasesInfo(dstPhases);
            dstSkills.add(dstSkill);
        }

        bsdxWaz.setSkillList(dstSkills);
    }

    // bhe -> bsdx
    private static Map<Integer, Integer> bheToBsdxSlotMap() {
        Map<Integer, Integer> m = new HashMap<>();

        m.put(0, 0);
        m.put(1, 1);
        m.put(2, 2);
        m.put(3, 3);
        m.put(4, 4);
        m.put(5, 5);
        m.put(6, 6);
        m.put(7, 7);
        m.put(8, 8);
        m.put(9, 9);
        m.put(10, 10);
        m.put(11, 11);
        m.put(12, 12);
        m.put(13, 13);
        m.put(14, 14);
        m.put(15, 15);
        m.put(16, 16);
        m.put(17, 17);
        m.put(18, 18);
        m.put(19, 19);
        m.put(20, 20);
        m.put(21, 21);
        m.put(22, 22);

        // 23 速度XYZ
        m.put(23, -1);

        m.put(24, 23);
        m.put(25, 24);
        m.put(26, 25);
        m.put(27, 26);
        m.put(28, 27);
        m.put(29, 28);
        m.put(30, 29);
        m.put(31, 30);
        m.put(32, 31);
        m.put(33, 32);
        m.put(34, 33);

        // 35 標的
        m.put(35, -1);

        m.put(36, 34);
        m.put(37, 35);
        
        // 38 技ツールパラメータ
        m.put(38, -1);
        m.put(39, 36);

        // 40 ハイパーアーマー
        m.put(40, -1);
        m.put(41, 37);

        // 42 無敵
        m.put(42, 38);

        // 43 死亡(自爆)
        m.put(43, -1);

        m.put(44, 39);
        m.put(45, 40);
        m.put(46, 41);
        m.put(47, 42);
        m.put(48, 43);
        m.put(49, 44);
        m.put(50, 45);
        m.put(51, 46);

        // 52 CPU 特殊行動
        m.put(52, -1);

        m.put(53, 47);
        m.put(54, 48);
        m.put(55, 49);
        m.put(56, 50);
        m.put(57, 51);
        m.put(58, 52);
        m.put(59, 53);
        m.put(60, 54);
        m.put(61, 55);

        // 62 属性
        m.put(62, -1);

        m.put(63, 56);
        m.put(64, 57);
        m.put(65, 58);

        // 66-69 マルチロック
        m.put(66, -1);
        m.put(67, -1);
        m.put(68, -1);
        m.put(69, -1);

        m.put(70, 59);
        m.put(71, 60);
        m.put(72, 61);
        m.put(73, 62);
        m.put(74, 63);
        m.put(75, 64);
        m.put(76, 65);
        m.put(77, 66);
        m.put(78, 67);
        m.put(79, 68);
        m.put(80, 69);
        m.put(81, 70);
        m.put(82, 71);

        return m;
    }

}
