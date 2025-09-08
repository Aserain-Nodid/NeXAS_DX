package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.exception.OperationException;

public class HitBoxFactory {

    public static Spm.SPMHitArea createShapeObj(short shapeType) {
        return switch (shapeType) {
            case 0 -> new DefaultHitArea(); // CRect在逻辑上无法被创建
            case 1  -> new CRotatableRect();
            case 2  -> new CCircle();
            case 7  -> new C2DLineSegment();
            case 8  -> new C2DDot();
            case 9  -> new CBox();
            case 10 -> new CRotatableBox();
            case 11 -> new CSphere();
            default -> throw new OperationException(500, "unexpected hitbox type: " + shapeType);
        };
    }

}
