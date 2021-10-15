package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeWall extends LittleShape {
    
    public LittleShapeWall() {
        super(2);
    }
    
    public void shrinkEdge(CornerCache cache, Axis axis, Axis one, Axis two, boolean positive, LittleBox box) {
        Facing facing = Facing.get(axis, positive);
        BoxCorner[] corners = BoxCorner.faceCorners(facing);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? box.getMax(one) : box.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? box.getMax(two) : box.getMin(two));
        }
    }
    
    public void shrinkEdge(CornerCache cache, Axis axis, Axis one, Axis two, boolean positive, Facing targetFace, LittleBox box) {
        Facing facing = Facing.get(axis, positive);
        if (targetFace == null)
            targetFace = facing;
        else if (targetFace == facing.opposite())
            targetFace = facing;
        Axis targetAxis = targetFace.axis;
        BoxCorner[] corners = BoxCorner.faceCorners(facing);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? box.getMax(one) : box.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? box.getMax(two) : box.getMin(two));
            if (facing != targetFace) {
                if (corner.isFacingPositive(targetAxis) != targetFace.positive)
                    cache.setAbsolute(corner, axis, positive ? box.getMin(axis) : box.getMax(axis));
                else
                    cache.setAbsolute(corner, targetAxis, targetFace.positive ? box.getMin(targetAxis) : box.getMax(targetAxis));
            }
        }
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        int direction = selection.getNBT().getInt("direction");
        PlacementPosition originalMin = selection.getFirst().pos.copy();
        PlacementPosition originalMax = selection.getLast().pos.copy();
        originalMin.convertTo(boxes.getGrid());
        originalMax.convertTo(boxes.getGrid());
        
        int thickness = Math.max(0, selection.getNBT().getInt("thickness") - 1);
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        Axis toIgnore = direction == 0 ? Axis.Y : direction == 1 ? Axis.X : Axis.Z;
        Axis oneIgnore = toIgnore.one();
        Axis twoIgnore = toIgnore.two();
        Axis axis = box.getSize(oneIgnore) > box.getSize(twoIgnore) ? oneIgnore : twoIgnore;
        
        CornerCache cache = box.new CornerCache(false);
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        Axis one = axis.one();
        Axis two = axis.two();
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        Facing minFacing = originalMin.facing;
        Facing maxFacing = originalMax.facing;
        
        if (minFacing.axis == toIgnore || box.getSize(minFacing.axis) == 1)
            minFacing = null;
        if (maxFacing.axis == toIgnore || box.getSize(maxFacing.axis) == 1)
            maxFacing = null;
        
        int invSize = thickness / 2;
        int size = thickness - invSize;
        minBox.growCentered(thickness);
        LittleVec vec = new LittleVec(originalMin.facing);
        if (originalMin.facing.positive)
            vec.scale(size);
        else
            vec.scale(-invSize);
        minBox.add(vec);
        
        maxBox.growCentered(thickness);
        vec = new LittleVec(originalMax.facing);
        if (originalMax.facing.positive)
            vec.scale(size);
        else
            vec.scale(-invSize);
        maxBox.add(vec);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        minBox.setMin(toIgnore, box.getMin(toIgnore));
        maxBox.setMin(toIgnore, box.getMin(toIgnore));
        minBox.setMax(toIgnore, box.getMax(toIgnore));
        maxBox.setMax(toIgnore, box.getMax(toIgnore));
        
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        shrinkEdge(cache, axis, one, two, facingPositive, minFacing, minBox);
        shrinkEdge(cache, axis, one, two, !facingPositive, maxFacing, maxBox);
        
        box.setData(cache.getData());
        boxes.add(box);
        
        return;
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<String> list) {
        list.add("thickness: " + nbt.getInt("thickness") + " tiles");
        
        int facing = nbt.getInt("direction");
        String text = "facing: ";
        switch (facing) {
        case 0:
            text += "y";
            break;
        case 1:
            text += "x";
            break;
        case 2:
            text += "z";
            break;
        }
        list.add(text);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        controls.add(new GuiStateButton("direction", nbt.getInt("direction"), "facing: y", "facing: x", "facing: z"));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.value);
        
        GuiStateButton state = (GuiStateButton) gui.get("direction");
        nbt.putInt("direction", state.getState());
        
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {
        int direction = nbt.getInt("direction");
        if (rotation.axis != Axis.Y)
            direction = 0;
        else {
            if (direction == 1)
                direction = 2;
            else
                direction = 1;
        }
        
        nbt.putInt("direction", direction);
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
}
