```java
class HitArea{
    u16 id;
    u16 typeId;
    if (typeId) {
        case 0 -> new CRect().readInfo();
        case 1  -> new CRotatableRect().readInfo();
        case 2  -> new CCircle().readInfo();
        case 7  -> new C2DLineSegment().readInfo();
        case 8  -> new C2DDot().readInfo();
        case 9  -> new CBox().readInfo();
        case 10 -> new CRotatableBox().readInfo();
        case 11 -> new CSphere().readInfo();
    } else {
        u32 int1;
        u32 int2;
        u32 int3;
        u32 int4;
        u32 int5;
        u32 int6;
    }
}
```