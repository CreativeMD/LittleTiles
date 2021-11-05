package com.creativemd.littletiles.common.tile.math.box.face;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.math.vec.VectorFan;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public class LittleBoxFace {
    
    public LittleGridContext context;
    public LittleBox box;
    public final Axis one;
    public final Axis two;
    public final EnumFacing facing;
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
    
    public LittleBoxFace(LittleBox box, List<VectorFan> faceFans, Iterable<VectorFan> tiltedFans, LittleGridContext context, EnumFacing facing, int minOne, int minTwo, int maxOne, int maxTwo, int origin) {
        this.box = box;
        this.faceFans = faceFans;
        this.tiltedFans = tiltedFans;
        this.context = context;
        this.facing = facing;
        this.one = RotationUtils.getOne(facing.getAxis());
        this.two = RotationUtils.getTwo(facing.getAxis());
        this.minOne = minOne;
        this.minTwo = minTwo;
        this.maxOne = maxOne;
        this.maxTwo = maxTwo;
        this.origin = origin;
        this.oldOrigin = origin;
        this.filled = new boolean[maxOne - minOne][maxTwo - minTwo];
    }
    
    public void ensureContext(LittleGridContext context) {
        if (context == this.context || this.context.size > context.size)
            return;
        
        int ratio = context.size / this.context.size;
        this.minOne *= ratio;
        this.minTwo *= ratio;
        this.maxOne *= ratio;
        this.maxTwo *= ratio;
        this.origin *= ratio;
        this.oldOrigin *= ratio;
        box = box.copy(); // Make sure the original one will not be modified
        box.convertTo(this.context, context);
        this.context = context;
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
        EnumFacing direction = EnumFacing.VALUES[index];
        if (direction.getAxis() == one)
            return (direction.getAxisDirection() == AxisDirection.POSITIVE ? box.maxX : box.minX) + minOne;
        if (direction.getAxis() == two)
            return (direction.getAxisDirection() == AxisDirection.POSITIVE ? box.maxY : box.minY) + minTwo;
        return oldOrigin;
    }
    
    public void cut(List<VectorFan> fans) {
        if (toCut == null)
            toCut = new ArrayList<>();
        Axis axis = RotationUtils.getThird(one, two);
        for (VectorFan fan : fans) {
            fan = fan.copy();
            for (int i = 0; i < fan.count(); i++)
                VectorUtils.set(fan.get(i), oldOrigin, axis);
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
        
        BasicCombiner.combineBoxes(boxes);
        
        List<VectorFan> fans = new ArrayList<>(boxes.size());
        for (LittleBox box : boxes) {
            EnumFaceDirection face = EnumFaceDirection.getFacing(facing);
            Vector3f[] coords = new Vector3f[4];
            for (int i = 0; i < coords.length; i++) {
                VertexInformation info = face.getVertexInformation(i);
                Vector3f vec = new Vector3f();
                vec.x = get(box, info.xIndex);
                vec.y = get(box, info.yIndex);
                vec.z = get(box, info.zIndex);
                coords[i] = vec;
            }
            fans.add(new VectorFan(coords));
        }
        
        if (faceFans != null) {
            List<VectorFan> newFans = new ArrayList<>();
            for (VectorFan fan : fans)
                newFans.addAll(fan.cut2d(faceFans, one, two, facing.getAxisDirection() == AxisDirection.POSITIVE, true));
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
            result.addAll(fan.cut2d(toCut, one, two, facing.getAxisDirection() != AxisDirection.POSITIVE, false));
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
        return origin > 0 && origin < context.maxPos;
    }
    
    public void move(EnumFacing facing) {
        origin = facing.getAxisDirection() == AxisDirection.POSITIVE ? 0 : context.maxPos;
    }
    
}
