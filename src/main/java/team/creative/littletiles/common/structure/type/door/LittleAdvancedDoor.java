package team.creative.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline.KeyDeselectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.GuiTimeline.KeySelectedEvent;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.KeyControl;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel.TimelineChannelDouble;
import com.creativemd.creativecore.common.gui.controls.gui.timeline.TimelineChannel.TimelineChannelInteger;
import com.creativemd.creativecore.common.gui.event.gui.GuiToolTipEvent;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.DoorController;
import team.creative.littletiles.common.animation.ValueTimeline;
import team.creative.littletiles.common.animation.property.AnimationProperty;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittleAdvancedDoor extends LittleDoorBase {
    
    public static PairList<Integer, Double> loadPairListDouble(int[] array) {
        PairList<Integer, Double> list = new PairList<>();
        int i = 0;
        while (i < array.length) {
            list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
            i += 3;
        }
        return list;
    }
    
    public static PairList<Integer, Double> loadPairListInteger(int[] array) {
        PairList<Integer, Double> list = new PairList<>();
        int i = 0;
        while (i < array.length) {
            list.add(array[i], (double) array[i + 1]);
            i += 2;
        }
        return list;
    }
    
    public static PairList<Integer, Double> loadPairListDouble(int[] array, int from, int length) {
        PairList<Integer, Double> list = new PairList<>();
        int i = from;
        while (i < from + length) {
            list.add(array[i], Double.longBitsToDouble((((long) array[i + 1]) << 32) | (array[i + 2] & 0xffffffffL)));
            i += 3;
        }
        return list;
    }
    
    public static int[] savePairListDouble(PairList<Integer, Double> list) {
        if (list == null)
            return null;
        
        int[] array = new int[list.size() * 3];
        for (int i = 0; i < list.size(); i++) {
            Pair<Integer, Double> pair = list.get(i);
            array[i * 3] = pair.key;
            long value = Double.doubleToLongBits(pair.value);
            array[i * 3 + 1] = (int) (value >> 32);
            array[i * 3 + 2] = (int) value;
        }
        return array;
    }
    
    public static int[] savePairListInteger(PairList<Integer, Integer> list) {
        if (list == null)
            return null;
        
        int[] array = new int[list.size() * 2];
        for (int i = 0; i < list.size(); i++) {
            Pair<Integer, Integer> pair = list.get(i);
            array[i * 2] = pair.key;
            array[i * 2 + 1] = pair.value;
        }
        return array;
    }
    
    public PairList<Integer, Double> interpolateToDouble(PairList<Integer, Integer> list) {
        if (list == null)
            return null;
        
        PairList<Integer, Double> converted = new PairList<>();
        for (Pair<Integer, Integer> pair : list) {
            converted.add(pair.key, offGrid.pixelSize * pair.value);
        }
        
        return converted;
    }
    
    public PairList<Integer, Double> invert(PairList<Integer, Double> list) {
        if (list == null)
            return null;
        
        PairList<Integer, Double> inverted = new PairList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            Pair<Integer, Double> pair = list.get(i);
            inverted.add(duration - pair.key, pair.value);
        }
        return inverted;
    }
    
    public static boolean isAligned(AnimationProperty key, ValueTimeline timeline) {
        if (timeline == null)
            return true;
        
        return key.isAligned(timeline.first(key));
    }
    
    public LittleAdvancedDoor(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @StructureDirectional(color = ColorUtils.RED)
    public StructureRelative axisCenter;
    
    public ValueTimeline rotX;
    public ValueTimeline rotY;
    public ValueTimeline rotZ;
    
    public LittleGridContext offGrid;
    public ValueTimeline offX;
    public ValueTimeline offY;
    public ValueTimeline offZ;
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        super.writeToNBTExtra(nbt);
        
        NBTTagCompound animation = new NBTTagCompound();
        if (rotX != null)
            animation.setIntArray("rotX", rotX.write());
        if (rotY != null)
            animation.setIntArray("rotY", rotY.write());
        if (rotZ != null)
            animation.setIntArray("rotZ", rotZ.write());
        
        if (offGrid != null) {
            animation.setInteger("offGrid", offGrid.size);
            if (offX != null)
                animation.setIntArray("offX", offX.write());
            if (offY != null)
                animation.setIntArray("offY", offY.write());
            if (offZ != null)
                animation.setIntArray("offZ", offZ.write());
        }
        nbt.setTag("animation", animation);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        super.loadFromNBTExtra(nbt);
        
        if (nbt.hasKey("animation")) {
            NBTTagCompound animation = nbt.getCompoundTag("animation");
            if (animation.hasKey("rotX"))
                rotX = ValueTimeline.read(animation.getIntArray("rotX"));
            if (animation.hasKey("rotY"))
                rotY = ValueTimeline.read(animation.getIntArray("rotY"));
            if (animation.hasKey("rotZ"))
                rotZ = ValueTimeline.read(animation.getIntArray("rotZ"));
            
            if (animation.hasKey("offGrid")) {
                offGrid = LittleGridContext.get(animation.getInteger("offGrid"));
                if (animation.hasKey("offX"))
                    offX = ValueTimeline.read(animation.getIntArray("offX"));
                if (animation.hasKey("offY"))
                    offY = ValueTimeline.read(animation.getIntArray("offY"));
                if (animation.hasKey("offZ"))
                    offZ = ValueTimeline.read(animation.getIntArray("offZ"));
            }
        } else { // before pre132
            if (nbt.hasKey("rotX"))
                rotX = ValueTimeline.create(interpolation).addPoints(loadPairListDouble(nbt.getIntArray("rotX")));
            if (nbt.hasKey("rotY"))
                rotY = ValueTimeline.create(interpolation).addPoints(loadPairListDouble(nbt.getIntArray("rotY")));
            if (nbt.hasKey("rotZ"))
                rotZ = ValueTimeline.create(interpolation).addPoints(loadPairListDouble(nbt.getIntArray("rotZ")));
            
            if (nbt.hasKey("offGrid")) {
                offGrid = LittleGridContext.get(nbt.getInteger("offGrid"));
                if (nbt.hasKey("offX"))
                    offX = ValueTimeline.create(interpolation).addPoints(loadPairListInteger(nbt.getIntArray("offX")));
                if (nbt.hasKey("offY"))
                    offY = ValueTimeline.create(interpolation).addPoints(loadPairListInteger(nbt.getIntArray("offY")));
                if (nbt.hasKey("offZ"))
                    offZ = ValueTimeline.create(interpolation).addPoints(loadPairListInteger(nbt.getIntArray("offZ")));
            }
        }
    }
    
    @Override
    public void transformDoorPreview(LittleAbsolutePreviews previews) {
        StructureRelative axisCenter = (StructureRelative) previews.getStructureType().loadDirectional(previews, "axisCenter");
        axisCenter.forceContext(previews);
    }
    
    @Override
    public DoorController createController(UUIDSupplier supplier, Placement placement, int completeDuration) {
        LittleAdvancedDoor newDoor = (LittleAdvancedDoor) placement.origin.getStructure();
        int duration = newDoor.duration;
        
        PairList<AnimationProperty, ValueTimeline> open = new PairList<>();
        PairList<AnimationProperty, ValueTimeline> close = new PairList<>();
        
        AnimationState opened = new AnimationState();
        AnimationState closed = new AnimationState();
        if (offX != null) {
            opened.set(AnimationProperty.offX, offGrid.toVanillaGrid(offX.last(AnimationProperty.offX)));
            closed.set(AnimationProperty.offX, offGrid.toVanillaGrid(offX.first(AnimationProperty.offX)));
            
            open.add(AnimationProperty.offX, offX.copy().factor(offGrid.pixelSize));
            close.add(AnimationProperty.offX, offX.invert(duration).factor(offGrid.pixelSize));
        }
        if (offY != null) {
            opened.set(AnimationProperty.offY, offGrid.toVanillaGrid(offY.last(AnimationProperty.offY)));
            closed.set(AnimationProperty.offY, offGrid.toVanillaGrid(offY.first(AnimationProperty.offY)));
            
            open.add(AnimationProperty.offY, offY.copy().factor(offGrid.pixelSize));
            close.add(AnimationProperty.offY, offY.invert(duration).factor(offGrid.pixelSize));
        }
        if (offZ != null) {
            opened.set(AnimationProperty.offZ, offGrid.toVanillaGrid(offZ.last(AnimationProperty.offZ)));
            closed.set(AnimationProperty.offZ, offGrid.toVanillaGrid(offZ.first(AnimationProperty.offZ)));
            
            open.add(AnimationProperty.offZ, offZ.copy().factor(offGrid.pixelSize));
            close.add(AnimationProperty.offZ, offZ.invert(duration).factor(offGrid.pixelSize));
        }
        if (rotX != null) {
            opened.set(AnimationProperty.rotX, rotX.last(AnimationProperty.rotX));
            closed.set(AnimationProperty.rotX, rotX.first(AnimationProperty.rotX));
            
            open.add(AnimationProperty.rotX, rotX);
            close.add(AnimationProperty.rotX, rotX.invert(duration));
        }
        if (rotY != null) {
            opened.set(AnimationProperty.rotY, rotY.last(AnimationProperty.rotY));
            closed.set(AnimationProperty.rotY, rotY.first(AnimationProperty.rotY));
            
            open.add(AnimationProperty.rotY, rotY);
            close.add(AnimationProperty.rotY, rotY.invert(duration));
        }
        if (rotZ != null) {
            opened.set(AnimationProperty.rotZ, rotZ.last(AnimationProperty.rotZ));
            closed.set(AnimationProperty.rotZ, rotZ.first(AnimationProperty.rotZ));
            
            open.add(AnimationProperty.rotZ, rotZ);
            close.add(AnimationProperty.rotZ, rotZ.invert(duration));
        }
        
        return new DoorController(supplier, closed, opened, stayAnimated ? null : false, duration, completeDuration, new AnimationTimeline(duration, open), new AnimationTimeline(duration, close), interpolation);
    }
    
    @Override
    public StructureAbsolute getAbsoluteAxis() {
        if (axisCenter == null)
            return new StructureAbsolute(getPos(), new LittleBox(0, 0, 0, 1, 1, 1), mainBlock.getContext());
        return new StructureAbsolute(new LittleVecAbsolute(getPos(), mainBlock.getContext()), axisCenter);
    }
    
    public static class LittleAdvancedDoorType extends LittleDoorType {
        
        public LittleAdvancedDoorType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
            super(id, category, structureClass, attribute);
        }
        
        @Override
        public void flip(LittlePreviews previews, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
            super.flip(previews, context, axis, doubledCenter);
            
            ValueTimeline rotX = null;
            ValueTimeline rotY = null;
            ValueTimeline rotZ = null;
            
            ValueTimeline offX = null;
            ValueTimeline offY = null;
            ValueTimeline offZ = null;
            NBTTagCompound animation = previews.structureNBT.getCompoundTag("animation");
            if (animation.hasKey("rotX"))
                rotX = ValueTimeline.read(animation.getIntArray("rotX"));
            if (animation.hasKey("rotY"))
                rotY = ValueTimeline.read(animation.getIntArray("rotY"));
            if (animation.hasKey("rotZ"))
                rotZ = ValueTimeline.read(animation.getIntArray("rotZ"));
            
            if (animation.hasKey("offGrid")) {
                if (animation.hasKey("offX"))
                    offX = ValueTimeline.read(animation.getIntArray("offX"));
                if (animation.hasKey("offY"))
                    offY = ValueTimeline.read(animation.getIntArray("offY"));
                if (animation.hasKey("offZ"))
                    offZ = ValueTimeline.read(animation.getIntArray("offZ"));
            }
            
            switch (axis) {
            case X:
                if (rotY != null)
                    rotY.flip();
                if (rotZ != null)
                    rotZ.flip();
                
                if (offX != null)
                    offX.flip();
                break;
            case Y:
                if (rotX != null)
                    rotX.flip();
                if (rotZ != null)
                    rotZ.flip();
                
                if (offY != null)
                    offY.flip();
                break;
            case Z:
                if (rotX != null)
                    rotX.flip();
                if (rotY != null)
                    rotY.flip();
                
                if (offZ != null)
                    offZ.flip();
                break;
            }
            
            if (rotX == null)
                animation.removeTag("rotX");
            else
                animation.setIntArray("rotX", rotX.write());
            if (rotY == null)
                animation.removeTag("rotY");
            else
                animation.setIntArray("rotY", rotY.write());
            if (rotZ == null)
                animation.removeTag("rotZ");
            else
                animation.setIntArray("rotZ", rotZ.write());
            
            if (offX == null)
                animation.removeTag("offX");
            else
                animation.setIntArray("offX", offX.write());
            if (offY == null)
                animation.removeTag("offY");
            else
                animation.setIntArray("offY", offY.write());
            if (offZ == null)
                animation.removeTag("offZ");
            else
                animation.setIntArray("offZ", offZ.write());
        }
        
        @Override
        public void rotate(LittlePreviews previews, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
            super.rotate(previews, context, rotation, doubledCenter);
            
            ValueTimeline rotX = null;
            ValueTimeline rotY = null;
            ValueTimeline rotZ = null;
            
            ValueTimeline offX = null;
            ValueTimeline offY = null;
            ValueTimeline offZ = null;
            NBTTagCompound animation = previews.structureNBT.getCompoundTag("animation");
            if (animation.hasKey("rotX"))
                rotX = ValueTimeline.read(animation.getIntArray("rotX"));
            if (animation.hasKey("rotY"))
                rotY = ValueTimeline.read(animation.getIntArray("rotY"));
            if (animation.hasKey("rotZ"))
                rotZ = ValueTimeline.read(animation.getIntArray("rotZ"));
            
            if (animation.hasKey("offGrid")) {
                if (animation.hasKey("offX"))
                    offX = ValueTimeline.read(animation.getIntArray("offX"));
                if (animation.hasKey("offY"))
                    offY = ValueTimeline.read(animation.getIntArray("offY"));
                if (animation.hasKey("offZ"))
                    offZ = ValueTimeline.read(animation.getIntArray("offZ"));
            }
            
            ValueTimeline rotTempX = rotX;
            ValueTimeline rotTempY = rotY;
            ValueTimeline rotTempZ = rotZ;
            
            rotX = rotation.getX(rotTempX, rotTempY, rotTempZ);
            if (rotation.negativeX() && rotX != null)
                rotX.flip();
            rotY = rotation.getY(rotTempX, rotTempY, rotTempZ);
            if (rotation.negativeY() && rotY != null)
                rotY.flip();
            rotZ = rotation.getZ(rotTempX, rotTempY, rotTempZ);
            if (rotation.negativeZ() && rotZ != null)
                rotZ.flip();
            
            ValueTimeline offTempX = offX;
            ValueTimeline offTempY = offY;
            ValueTimeline offTempZ = offZ;
            
            offX = rotation.getX(offTempX, offTempY, offTempZ);
            if (rotation.negativeX() && offX != null)
                offX.flip();
            offY = rotation.getY(offTempX, offTempY, offTempZ);
            if (rotation.negativeY() && offY != null)
                offY.flip();
            offZ = rotation.getZ(offTempX, offTempY, offTempZ);
            if (rotation.negativeZ() && offZ != null)
                offZ.flip();
            
            if (rotX == null)
                animation.removeTag("rotX");
            else
                animation.setIntArray("rotX", rotX.write());
            if (rotY == null)
                animation.removeTag("rotY");
            else
                animation.setIntArray("rotY", rotY.write());
            if (rotZ == null)
                animation.removeTag("rotZ");
            else
                animation.setIntArray("rotZ", rotZ.write());
            
            if (offX == null)
                animation.removeTag("offX");
            else
                animation.setIntArray("offX", offX.write());
            if (offY == null)
                animation.removeTag("offY");
            else
                animation.setIntArray("offY", offY.write());
            if (offZ == null)
                animation.removeTag("offZ");
            else
                animation.setIntArray("offZ", offZ.write());
        }
        
    }
    
}
