package team.creative.littletiles.common.math.face;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.FaceInfo.VertexInfo;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxCombiner;

public non-sealed class LittleFace implements ILittleFace {
    
    public LittleGrid grid;
    public LittleBox box;
    public final Axis one;
    public final Axis two;
    public final Facing facing;
    public int minOne;
    public int minTwo;
    public int maxOne;
    public int maxTwo;
    public int origin;
    public int oldOrigin;
    
    public boolean[][] filled;
    private List<VectorFan> toCut = null;
    private List<VectorFan> cachedFans = null;
    private List<VectorFan> faceFans = null;
    private Iterable<VectorFan> tiltedFans = null;
    
    public LittleFace(LittleBox box, List<VectorFan> faceFans, Iterable<VectorFan> tiltedFans, LittleGrid grid, Facing facing, int minOne, int minTwo, int maxOne, int maxTwo, int origin) {
        this.box = box;
        this.faceFans = faceFans;
        this.tiltedFans = tiltedFans;
        this.grid = grid;
        this.facing = facing;
        this.one = facing.one();
        this.two = facing.two();
        this.minOne = minOne;
        this.minTwo = minTwo;
        this.maxOne = maxOne;
        this.maxTwo = maxTwo;
        this.origin = origin;
        this.oldOrigin = origin;
        this.filled = new boolean[maxOne - minOne][maxTwo - minTwo];
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public LittleBox box() {
        return box;
    }
    
    @Override
    public void ensureGrid(LittleGrid context) {
        if (context == this.grid || this.grid.count > grid.count)
            return;
        
        int ratio = context.count / this.grid.count;
        this.minOne *= ratio;
        this.minTwo *= ratio;
        this.maxOne *= ratio;
        this.maxTwo *= ratio;
        this.origin *= ratio;
        this.oldOrigin *= ratio;
        box = box.copy(); // Make sure the original one will not be modified
        box.convertTo(this.grid, context);
        this.grid = context;
        filled = new boolean[maxOne - minOne][maxTwo - minTwo];
        if (faceFans != null) {
            List<VectorFan> newFans = new ArrayList<>(faceFans.size());
            for (VectorFan fan : faceFans) {
                fan = fan.copy();
                fan.scale(ratio);
                newFans.add(fan);
            }
            if (tiltedFans != null) {
                List<VectorFan> tiledFansNew = new ArrayList<>();
                for (VectorFan fan : tiltedFans) {
                    fan = fan.copy();
                    fan.scale(ratio);
                    tiledFansNew.add(fan);
                }
                this.tiltedFans = tiledFansNew;
            }
            faceFans = newFans;
        }
    }
    
    public boolean isPartiallyFilled() {
        if (toCut != null)
            return true;
        for (int one = 0; one < filled.length; one++)
            for (int two = 0; two < filled[one].length; two++)
                if (filled[one][two])
                    return true;
        return false;
    }
    
    public boolean isFilled(boolean important) {
        if (important && toCut != null)
            return generateFans().isEmpty();
        for (int one = 0; one < filled.length; one++)
            for (int two = 0; two < filled[one].length; two++)
                if (!filled[one][two])
                    return false;
        return true;
    }
    
    private float get(LittleBox box, int index) {
        Facing direction = Facing.get(index);
        if (direction.axis == one)
            return (direction.positive ? box.maxX : box.minX) + minOne;
        if (direction.axis == two)
            return (direction.positive ? box.maxY : box.minY) + minTwo;
        return oldOrigin;
    }
    
    @Override
    public void setPartiallyFilled() {}
    
    @Override
    public boolean supportsCutting() {
        return true;
    }
    
    @Override
    public void cut(List<VectorFan> fans) {
        if (toCut == null)
            toCut = new ArrayList<>();
        Axis axis = Axis.third(one, two);
        for (VectorFan fan : fans) {
            fan = fan.copy();
            for (int i = 0; i < fan.count(); i++)
                fan.get(i).set(axis, oldOrigin);
            toCut.add(fan);
        }
        
    }
    
    public List<VectorFan> generateFans() {
        if (cachedFans != null)
            return cachedFans;
        List<LittleBox> boxes = new ArrayList<>();
        int startOne = 0;
        int startTwo = 0;
        boolean toAdd = false;
        for (int one = 0; one < filled.length; one++) {
            for (int two = 0; two < filled[one].length; two++)
                if (filled[one][two]) {
                    if (toAdd) {
                        boxes.add(new LittleBox(startOne, startTwo, 0, one + 1, two, 0));
                        toAdd = false;
                    }
                    startOne = one;
                    startTwo = two;
                } else if (!toAdd) {
                    startOne = one;
                    startTwo = two;
                    toAdd = true;
                }
            if (toAdd) {
                boxes.add(new LittleBox(startOne, startTwo, 0, one + 1, filled[one].length, 0));
                toAdd = false;
            }
        }
        
        LittleBoxCombiner.combine(boxes);
        
        List<VectorFan> fans = new ArrayList<>(boxes.size());
        for (LittleBox box : boxes) {
            FaceInfo face = FaceInfo.fromFacing(facing.toVanilla());
            Vec3f[] coords = new Vec3f[4];
            for (int i = 0; i < coords.length; i++) {
                VertexInfo info = face.getVertexInfo(i);
                Vec3f vec = new Vec3f();
                vec.x = get(box, info.xFace);
                vec.y = get(box, info.yFace);
                vec.z = get(box, info.zFace);
                coords[i] = vec;
            }
            fans.add(new VectorFan(coords));
        }
        
        if (faceFans != null) {
            List<VectorFan> newFans = new ArrayList<>();
            for (VectorFan fan : fans)
                newFans.addAll(fan.cut2d(faceFans, one, two, facing.positive, true));
            fans = newFans;
        }
        
        if (toCut == null) {
            if (tiltedFans != null)
                for (VectorFan fan : tiltedFans)
                    fans.add(fan);
            return fans;
        }
        
        List<VectorFan> result = new ArrayList<>();
        for (VectorFan fan : fans)
            result.addAll(fan.cut2d(toCut, one, two, !facing.positive, false));
        if (tiltedFans != null)
            for (VectorFan fan : tiltedFans)
                result.add(fan);
        cachedFans = result;
        return result;
    }
    
    public LittleBox getBox() {
        return box;
    }
    
    public boolean isFaceInsideBlock() {
        return origin > 0 && origin < grid.count;
    }
    
    public void move(Facing facing) {
        origin = facing.positive ? 0 : grid.count;
    }
    
    @Override
    public Facing facing() {
        return facing;
    }
    
    @Override
    public Axis one() {
        return one;
    }
    
    @Override
    public Axis two() {
        return two;
    }
    
    @Override
    public int origin() {
        return origin;
    }
    
    @Override
    public int minOne() {
        return minOne;
    }
    
    @Override
    public int minTwo() {
        return minTwo;
    }
    
    @Override
    public int maxOne() {
        return maxOne;
    }
    
    @Override
    public int maxTwo() {
        return maxTwo;
    }
    
    @Override
    public void set(int one, int two, boolean value) {
        filled[one][two] = value;
    }
    
}
