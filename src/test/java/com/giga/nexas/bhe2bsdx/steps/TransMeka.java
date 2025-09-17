package com.giga.nexas.bhe2bsdx.steps;

import cn.hutool.core.bean.BeanUtil;
import com.giga.nexas.dto.bsdx.grp.groupmap.BatVoiceGrp;
import com.giga.nexas.dto.bsdx.mek.Mek;
import com.giga.nexas.dto.bsdx.spm.Spm;
import com.giga.nexas.dto.bsdx.waz.Waz;
import com.giga.nexas.dto.bsdx.waz.wazfactory.SkillInfoFactory;
import com.giga.nexas.dto.bsdx.waz.wazfactory.wazinfoclass.SkillUnit;
import com.giga.nexas.dto.bsdx.waz.wazfactory.wazinfoclass.obj.*;
import com.giga.nexas.exception.OperationException;
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
                        com.giga.nexas.dto.bhe.spm.Spm bheCSpm,
                        com.giga.nexas.dto.bhe.spm.Spm bheSSpm,
                        com.giga.nexas.dto.bhe.spm.Spm bheGSpm,
                        com.giga.nexas.dto.bhe.spm.Spm bheMSpm,

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
        // spm
        Spm bsdxSpm = transSprite(bheSpm);
        Spm bsdxCSpm = transSprite(bheCSpm);
        Spm bsdxSSpm = transSprite(bheSSpm);
        Spm bsdxMSpm = transSprite(bheMSpm);
        Spm bsdxGSpm = transSprite(bheGSpm);

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

    private static com.giga.nexas.dto.bsdx.spm.Spm transSprite(com.giga.nexas.dto.bhe.spm.Spm bheSpm) {
        if (bheSpm == null) {
            return null;
        }
        Spm bsdxSpm = new Spm();

        // 仅保留 SPM 顶层字段，逐层重建嵌套集合以便填充 BSDX 特有字段（unk0/unk1/unk2）。
        BeanUtil.copyProperties(bheSpm, bsdxSpm, "pageData", "imageData", "animData");

        if (bheSpm.getImageData() != null) {
            List<Spm.SPMImageData> dstImages = new ArrayList<>(bheSpm.getImageData().size());
            for (com.giga.nexas.dto.bhe.spm.Spm.SPMImageData src : bheSpm.getImageData()) {
                Spm.SPMImageData dst = new Spm.SPMImageData();
                BeanUtil.copyProperties(src, dst);
                dstImages.add(dst);
            }
            bsdxSpm.setImageData(dstImages);
        }

        if (bheSpm.getAnimData() != null) {
            List<Spm.SPMAnimData> dstAnimData = new ArrayList<>(bheSpm.getAnimData().size());
            for (com.giga.nexas.dto.bhe.spm.Spm.SPMAnimData srcAnim : bheSpm.getAnimData()) {
                Spm.SPMAnimData dstAnim = new Spm.SPMAnimData();
                BeanUtil.copyProperties(srcAnim, dstAnim, "patData");
                if (srcAnim.getPatData() != null) {
                    List<Spm.SPMPatData> patList = new ArrayList<>(srcAnim.getPatData().size());
                    for (com.giga.nexas.dto.bhe.spm.Spm.SPMPatData srcPat : srcAnim.getPatData()) {
                        Spm.SPMPatData dstPat = new Spm.SPMPatData();
                        BeanUtil.copyProperties(srcPat, dstPat);
                        patList.add(dstPat);
                    }
                    dstAnim.setPatData(patList);
                }
                dstAnimData.add(dstAnim);
            }
            bsdxSpm.setAnimData(dstAnimData);
        }

        if (bheSpm.getPageData() != null) {
            List<Spm.SPMPageData> dstPages = new ArrayList<>(bheSpm.getPageData().size());
            for (com.giga.nexas.dto.bhe.spm.Spm.SPMPageData srcPage : bheSpm.getPageData()) {
                Spm.SPMPageData dstPage = new Spm.SPMPageData();
                BeanUtil.copyProperties(srcPage, dstPage, "hitRects", "chipData");

                if (srcPage.getChipData() != null) {
                    List<Spm.SPMChipData> chipData = new ArrayList<>(srcPage.getChipData().size());
                    for (com.giga.nexas.dto.bhe.spm.Spm.SPMChipData srcChip : srcPage.getChipData()) {
                        Spm.SPMChipData dstChip = new Spm.SPMChipData();
                        BeanUtil.copyProperties(srcChip, dstChip);
                        chipData.add(dstChip);
                    }
                    dstPage.setChipData(chipData);
                }

                List<Spm.SPMHitArea> dstHitAreas = new ArrayList<>();
                if (srcPage.getHitRects() != null) {
                    for (com.giga.nexas.dto.bhe.spm.Spm.SPMHitArea srcHit : srcPage.getHitRects()) {
                        dstHitAreas.add(srcHit.transHitbox());
                    }
                }
                dstPage.setHitRects(dstHitAreas);
                dstPages.add(dstPage);
            }
            bsdxSpm.setPageData(dstPages);
        }

        return bsdxSpm;
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

            // phasesInfo重建
            List<Waz.Skill.SkillPhase> dstPhases = new ArrayList<>();
            for (com.giga.nexas.dto.bhe.waz.Waz.Skill.SkillPhase srcSkillPhase : srcSkill.getPhasesInfo()) {

                Waz.Skill.SkillPhase dstPhase = new Waz.Skill.SkillPhase();
                for (com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.SkillUnit srcSkillUnit : srcSkillPhase.getSkillUnitCollection()) {
                    // 读取bhe槽位
                    Integer bheSlot = srcSkillUnit.getUnitQuantity();

                    // 槽位映射 bhe->bsdx
                    Integer bsdxSlot = bheToBsdxSlotMap.get(bheSlot);

                    // 无映射或越界则丢弃该单元
                    if (bsdxSlot == null || bsdxSlot < 0 || bsdxSlot >= 72) {
                        continue;
                    }

                    // 新建bsdx的SkillUnit
                    SkillUnit dstUnit = new SkillUnit();

                    // 记录目标槽位到单元 把unitQuantity用作槽位记录
                    dstUnit.setUnitQuantity(bsdxSlot);

                    // 目标事件列表
                    List<SkillInfoObject> dstInfos = new ArrayList<>();

                    // 源事件列表（bhe）
                    List<com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.SkillInfoObject> srcSkillInfoObjectList =
                            srcSkillUnit.getSkillInfoObjectList();

                    // 为空跳过
                    if (srcSkillInfoObjectList == null || srcSkillInfoObjectList.isEmpty()) {
                        continue;
                    }

                    // 将每个bhe事件用bsdx工厂按bsdx槽位，创建正确子类并拷贝共有字段
                    for (com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.SkillInfoObject srcInfo : srcSkillInfoObjectList) {

                        SkillInfoObject dstInfo;
                        // 37: 汎用変数  BHE:CEventFreeParam -> BSDX:CEventVal
                        if (bheSlot == 37 && srcInfo instanceof com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.CEventFreeParam srcBhe) {
                            dstInfo = com.giga.nexas.dto.bsdx.waz.wazfactory.SkillInfoFactory.createEventObjectBsdx(35);
                            if (dstInfo instanceof CEventVal ev) {
                                for (var unit : srcBhe.getUnitList()) {
                                    if (unit.getBuffer() != 0) {
                                        continue;
                                    }
                                    if (unit.getData() instanceof com.giga.nexas.dto.bhe.waz.wazfactory.wazinfoclass.obj.CEventVal v) {
                                        ev.setInt1(v.getInt1());
                                        ev.setInt2(v.getInt2());
                                        ev.setInt3(v.getInt3());
                                        ev.setInt4(v.getInt4());
                                    }
                                }
                                dstInfos.add(ev);
                            } else {
                                throw new OperationException(500, "汎用変数error");
                            }
                            continue;
                        }

                        dstInfo = SkillInfoFactory.createEventObjectBsdx(bsdxSlot);

                        // 按具体子类进行BeanCopy
                        if (dstInfo instanceof CEventSpriteAttr ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventCpuButton ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventCpuButtonToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventVoice ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventRadialLine ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventRadialLineToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventValRandom ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventMove ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventSe ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventTouch ev) {
                            // diff but copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventEffect ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类 ⚠
                            ev.transBheCEventEffectToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventScreenLine ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventScreenLineToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventSlipHosei ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventVal ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventBlur ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventCharge ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventChargeToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventScreenAttr ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventTerm ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventEscape ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventEscapeToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventScreenEffect ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventHit ev) {
                            // diff
//                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventHitToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventStatus ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventStatusToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventChange ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventSprite ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventHeight ev) {
                            BeanUtil.copyProperties(srcInfo, ev);
                            // 手动对应子类
                            ev.transBheCEventHeightToBsdx(srcInfo, ev);
                        } else if (dstInfo instanceof CEventCamera ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventScreenYure ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventSpriteYure ev) {
                            // copy
                            BeanUtil.copyProperties(srcInfo, ev);
                        } else if (dstInfo instanceof CEventBlink ev) {
                            // diff
                            BeanUtil.copyProperties(srcInfo, ev);
                            ev.transBheCEventBlinkToBsdx(srcInfo, ev);
                        } else {
                            // ？？？
                            throw new OperationException(500, "error");
                        }

                        // 槽位号同步
                        dstInfo.setSlotNum(bsdxSlot);

                        // 收集该条bsdx事件
                        dstInfos.add(dstInfo);
                    }

                    // 写回该单元的事件集合
                    dstUnit.setSkillInfoObjectList(dstInfos);

                    // 将该单元加入当前Phase
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
