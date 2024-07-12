package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeSlice extends LittleShape {
    
    public LittleShapeSlice() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        CornerCache cache = box.new CornerCache(false);
        
        Vec3i vec = getVec(selection.getNBT());
        Axis axis = vec.getX() == 0 ? Axis.X : vec.getY() == 0 ? Axis.Y : Axis.Z;
        
        LittleVec size = box.getSize();
        Facing facingOne = axis.one().facing(vec.get(axis.one().toVanilla()) > 0);
        Facing facingTwo = axis.two().facing(vec.get(axis.two().toVanilla()) > 0);
        Facing prefered;
        int sizeOne = size.get(axis.one());
        int sizeTwo = size.get(axis.two());
        if (sizeOne > sizeTwo)
            prefered = facingTwo;
        else if (sizeOne < sizeTwo)
            prefered = facingOne;
        else
            prefered = axis.one() == Axis.Y ? facingOne : facingTwo;
        
        BoxCorner corner = BoxCorner.getCornerUnsorted(axis.facing(false), facingOne, facingTwo);
        
        cache.setAbsolute(corner, prefered.axis, box.get(prefered.opposite()));
        cache.setAbsolute(corner.mirror(axis), prefered.axis, box.get(prefered.opposite()));
        
        box.setData(cache.getData());
        boxes.add(box);
    }
    
    public Vec3i getVec(CompoundTag nbt) {
        if (nbt.contains("vec")) {
            int[] array = nbt.getIntArray("vec");
            if (array.length == 3)
                return new Vec3i(array[0], array[1], array[2]);
        }
        return new Vec3i(0, 1, 1);
    }
    
    public void setVec(CompoundTag nbt, Vec3i vec) {
        nbt.putIntArray("vec", new int[] { vec.getX(), vec.getY(), vec.getZ() });
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        return new ArrayList<>();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {}
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {
        setVec(nbt, rotation.transform(getVec(nbt)));
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {
        setVec(nbt, axis.mirror(getVec(nbt)));
    }
    
}
