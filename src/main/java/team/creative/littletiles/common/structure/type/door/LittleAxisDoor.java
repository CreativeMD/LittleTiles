package team.creative.littletiles.common.structure.type.door;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.DoorController;
import team.creative.littletiles.common.animation.ValueTimeline;
import team.creative.littletiles.common.animation.property.AnimationProperty;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelativeAxis;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittleAxisDoor extends LittleDoorBase {
    
    public LittleAxisDoor(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        
        if (nbt.hasKey("ndirection"))
            doorRotation = new DirectionRotation();
        else
            doorRotation = parseRotation(nbt);
    }
    
    @Override
    protected Object failedLoadingRelative(NBTTagCompound nbt, StructureDirectionalField relative) {
        if (relative.key.equals("axisCenter")) {
            
            LittleRelativeDoubledAxis doubledRelativeAxis;
            if (nbt.hasKey("ax")) {
                LittleVecContext vec = new LittleVecContext("a", nbt);
                //if (getMainTile() != null)
                //vec.sub(new LittleVecContext(getMainTile().getMinVec(), getMainTile().getContext()));
                doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.getContext(), vec.getVec(), new LittleVec(1, 1, 1));
                
            } else if (nbt.hasKey("av")) {
                LittleVecContext vec = new LittleVecContext("av", nbt);
                doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.getContext(), vec.getVec(), new LittleVec(1, 1, 1));
            } else {
                doubledRelativeAxis = new LittleRelativeDoubledAxis("avec", nbt);
            }
            return new StructureRelative(StructureAbsolute.convertAxisToBox(doubledRelativeAxis.getNonDoubledVec(), doubledRelativeAxis.additional), doubledRelativeAxis
                    .getContext());
        } else
            return super.failedLoadingRelative(nbt, relative);
    }
    
    @Override
    protected void saveExtra(NBTTagCompound nbt) {
        super.saveExtra(nbt);
        doorRotation.writeToNBT(nbt);
    }
    
    public AxisDoorRotation doorRotation;
    @StructureDirectional
    public Axis axis;
    @StructureDirectional(color = ColorUtils.RED)
    public StructureRelative axisCenter;
    
    @Override
    public StructureAbsolute getAbsoluteAxis() {
        return new StructureAbsolute(new LittleVecAbsolute(getPos(), mainBlock.getContext()), axisCenter);
    }
    
    @Override
    public DoorController createController(UUIDSupplier supplier, Placement placement, int completeDuration) {
        return doorRotation.createController(supplier, this, completeDuration, interpolation);
    }
    
    @Override
    public void transformDoorPreview(LittleAbsolutePreviews previews) {
        StructureRelative axisCenter = (StructureRelative) previews.getStructureType().loadDirectional(previews, "axisCenter");
        axisCenter.forceContext(previews);
    }
    
    @Deprecated
    public static class LittleRelativeDoubledAxis extends LittleVecContext {
        
        public LittleVec additional;
        
        public LittleRelativeDoubledAxis(LittleGridContext context, LittleVec vec, LittleVec additional) {
            super(vec, context);
            this.additional = additional;
        }
        
        public LittleRelativeDoubledAxis(String name, NBTTagCompound nbt) {
            super();
            
            int[] array = nbt.getIntArray(name);
            if (array.length == 3) {
                this.vec = new LittleVec(array[0], array[1], array[2]);
                this.context = LittleGridContext.get();
                this.additional = new LittleVec(vec.x % 2, vec.y % 2, vec.z % 2);
                this.vec.x /= 2;
                this.vec.y /= 2;
                this.vec.z /= 2;
            } else if (array.length == 4) {
                this.vec = new LittleVec(array[0], array[1], array[2]);
                this.context = LittleGridContext.get(array[3]);
                this.additional = new LittleVec(vec.x % 2, vec.y % 2, vec.z % 2);
                this.vec.x /= 2;
                this.vec.y /= 2;
                this.vec.z /= 2;
            } else if (array.length == 7) {
                this.vec = new LittleVec(array[0], array[1], array[2]);
                this.context = LittleGridContext.get(array[3]);
                this.additional = new LittleVec(array[4], array[5], array[6]);
            } else
                throw new InvalidParameterException("No valid coords given " + nbt);
        }
        
        public LittleVecContext getNonDoubledVec() {
            return new LittleVecContext(vec.copy(), context);
        }
        
        public LittleVec getRotationVec() {
            LittleVec vec = this.vec.copy();
            vec.scale(2);
            vec.add(additional);
            return vec;
        }
        
        @Override
        public LittleRelativeDoubledAxis copy() {
            return new LittleRelativeDoubledAxis(context, vec.copy(), additional.copy());
        }
        
        @Override
        public void convertTo(LittleGridContext to) {
            LittleVec newVec = getRotationVec();
            newVec.convertTo(context, to);
            super.convertTo(to);
            additional = new LittleVec(newVec.x % 2, newVec.y % 2, newVec.z % 2);
            vec = newVec;
            vec.x /= 2;
            vec.y /= 2;
            vec.z /= 2;
        }
        
        @Override
        public void convertToSmallest() {
            if (isEven())
                super.convertToSmallest();
        }
        
        public int getSmallestContext() {
            if (isEven())
                return vec.getSmallestContext(this.context);
            return this.context.size;
        }
        
        @Override
        public boolean equals(Object paramObject) {
            if (paramObject instanceof LittleRelativeDoubledAxis)
                return super.equals(paramObject) && additional.equals(((LittleRelativeDoubledAxis) paramObject).additional);
            return false;
        }
        
        public boolean isEven() {
            return additional.x % 2 == 0;
        }
        
        @Override
        public void writeToNBT(String name, NBTTagCompound nbt) {
            nbt.setIntArray(name, new int[] { vec.x, vec.y, vec.z, context.size, additional.x, additional.y, additional.z });
        }
        
        @Override
        public String toString() {
            return "[" + vec.x + "," + vec.y + "," + vec.z + ",grid:" + context.size + ",additional:" + additional + "]";
        }
    }
    
    private static List<Class<? extends AxisDoorRotation>> rotationTypes = new ArrayList<>();
    private static List<String> rotationTypeNames = new ArrayList<>();
    static {
        rotationTypes.add(DirectionRotation.class);
        rotationTypeNames.add("direction");
        rotationTypes.add(FixedRotation.class);
        rotationTypeNames.add("fixed");
    }
    
    protected static AxisDoorRotation parseRotation(NBTTagCompound nbt) {
        int index = nbt.getInteger("rot-type");
        
        if (index >= 0 && index < rotationTypes.size()) {
            Class<? extends AxisDoorRotation> rotationType = rotationTypes.get(index);
            try {
                AxisDoorRotation rotation = rotationType.getConstructor().newInstance();
                rotation.readFromNBT(nbt);
                return rotation;
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {}
        }
        return new DirectionRotation();
    }
    
    protected static AxisDoorRotation createRotation(int index) {
        if (index >= 0 && index < rotationTypes.size()) {
            Class<? extends AxisDoorRotation> rotationType = rotationTypes.get(index);
            try {
                return rotationType.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        
        throw new RuntimeException("Invalid axis door rotation found index: " + index);
    }
    
    public abstract static class AxisDoorRotation {
        
        protected abstract void writeToNBTCore(NBTTagCompound nbt);
        
        public void writeToNBT(NBTTagCompound nbt) {
            nbt.setInteger("rot-type", rotationTypes.indexOf(this.getClass()));
            writeToNBTCore(nbt);
        }
        
        protected abstract void readFromNBT(NBTTagCompound nbt);
        
        protected abstract void rotate(Axis doorAxis, Rotation rotation);
        
        protected abstract void flip(Axis doorAxis, Axis axis);
        
        protected abstract boolean shouldRotatePreviews(LittleAxisDoor door);
        
        protected abstract DoorController createController(UUIDSupplier supplier, LittleAxisDoor door, int completeDuration, int interpolation);
        
        @SideOnly(Side.CLIENT)
        public abstract boolean shouldUpdateTimeline(GuiControl control);
        
        @SideOnly(Side.CLIENT)
        protected abstract void onSelected(GuiTileViewer viewer, GuiParent parent);
        
        @SideOnly(Side.CLIENT)
        protected abstract void parseGui(GuiTileViewer viewer, GuiParent parent);
        
        public abstract void populateTimeline(AnimationTimeline timeline, int duration, int interpolation, AnimationProperty key);
        
    }
    
    public static class DirectionRotation extends AxisDoorRotation {
        
        public boolean clockwise;
        
        @Override
        protected void writeToNBTCore(NBTTagCompound nbt) {
            nbt.setBoolean("clockwise", clockwise);
        }
        
        @Override
        protected void readFromNBT(NBTTagCompound nbt) {
            clockwise = nbt.getBoolean("clockwise");
        }
        
        @Override
        protected void rotate(Axis doorAxis, Rotation rotation) {
            clockwise = RotationUtils.rotate(Rotation.getRotation(doorAxis, clockwise), rotation).clockwise;
        }
        
        @Override
        protected void flip(Axis doorAxis, Axis axis) {
            if (doorAxis != axis)
                clockwise = !clockwise;
        }
        
        protected Rotation getRotation(EntityPlayer player, LittleAxisDoor door) {
            return Rotation.getRotation(door.axis, clockwise);
        }
        
        protected Rotation getDefaultRotation(LittleAxisDoor door) {
            return Rotation.getRotation(door.axis, clockwise);
        }
        
        @Override
        protected DoorController createController(UUIDSupplier supplier, LittleAxisDoor door, int completeDuration, int interpolation) {
            Rotation rotation = getRotation(null, door);
            return new DoorController(supplier, new AnimationState(), new AnimationState().set(AnimationProperty
                    .getRotation(rotation.axis), rotation.clockwise ? 90 : -90), door.stayAnimated ? null : false, door.duration, completeDuration, interpolation);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected void onSelected(GuiTileViewer viewer, GuiParent parent) {
            parent.addControl(new GuiStateButton("direction", clockwise ? 0 : 1, 0, 0, 70, GuiControl.translate("gui.door.axis.clockwise"), GuiControl
                    .translate("gui.door.axis.counterclockwise")));
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected void parseGui(GuiTileViewer viewer, GuiParent parent) {
            this.clockwise = ((GuiStateButton) parent.get("direction")).getState() == 0;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public boolean shouldUpdateTimeline(GuiControl control) {
            return control.is("direction");
        }
        
        @Override
        protected boolean shouldRotatePreviews(LittleAxisDoor door) {
            return !door.stayAnimated;
        }
        
        @Override
        public void populateTimeline(AnimationTimeline timeline, int duration, int interpolation, AnimationProperty key) {
            timeline.values.add(key, ValueTimeline.create(interpolation).addPoint(0, 0D).addPoint(duration, clockwise ? 90D : -90D));
        }
        
    }
    
    public static class FixedRotation extends AxisDoorRotation {
        
        public double degree;
        
        @Override
        protected void writeToNBTCore(NBTTagCompound nbt) {
            nbt.setDouble("degree", degree);
        }
        
        @Override
        protected void readFromNBT(NBTTagCompound nbt) {
            degree = nbt.getDouble("degree");
        }
        
        @Override
        protected void rotate(Axis doorAxis, Rotation rotation) {
            degree = RotationUtils.rotate(Rotation.getRotation(doorAxis, degree > 0 ? true : false), rotation).clockwise ? Math.abs(degree) : -Math.abs(degree);
        }
        
        @Override
        protected void flip(Axis doorAxis, Axis axis) {
            if (doorAxis != axis)
                degree = -degree;
        }
        
        @Override
        protected DoorController createController(UUIDSupplier supplier, LittleAxisDoor door, int completeDuration, int interpolation) {
            return new DoorController(supplier, new AnimationState()
                    .set(AnimationProperty.getRotation(door.axis), degree), door.stayAnimated ? null : false, door.duration, completeDuration, interpolation);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected void onSelected(GuiTileViewer viewer, GuiParent parent) {
            viewer.visibleNormalAxis = false;
            if (this.degree == 0)
                this.degree = 90;
            parent.addControl(new GuiTextfield("degree", "" + degree, 0, 0, 30, 12).setFloatOnly());
            
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected void parseGui(GuiTileViewer viewer, GuiParent parent) {
            float degree;
            try {
                degree = Float.parseFloat(((GuiTextfield) parent.get("degree")).text);
            } catch (NumberFormatException e) {
                degree = 0;
            }
            this.degree = degree;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public boolean shouldUpdateTimeline(GuiControl control) {
            return control.is("degree");
        }
        
        @Override
        protected boolean shouldRotatePreviews(LittleAxisDoor door) {
            return false;
        }
        
        @Override
        public void populateTimeline(AnimationTimeline timeline, int duration, int interpolation, AnimationProperty key) {
            timeline.values.add(key, ValueTimeline.create(interpolation).addPoint(0, 0D).addPoint(duration, degree));
        }
        
    }
    
    public static class LittleAxisDoorType extends LittleDoorType {
        
        public LittleAxisDoorType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
            super(id, category, structureClass, attribute);
        }
        
        @Override
        protected PlacePreview getPlacePreview(Object value, StructureDirectionalField type, LittlePreviews previews) {
            if (type.key.equals("axisCenter"))
                return new LittlePlaceBoxRelativeAxis(((StructureRelative) value)
                        .getBox(), (StructureRelative) value, type, Axis.values()[previews.structureNBT.getInteger("axis")]);
            return super.getPlacePreview(value, type, previews);
        }
        
        @Override
        public void flip(LittlePreviews previews, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
            AxisDoorRotation doorRotation = parseRotation(previews.structureNBT);
            
            doorRotation.flip(Axis.values()[previews.structureNBT.getInteger("axis")], axis);
            doorRotation.writeToNBT(previews.structureNBT);
            super.flip(previews, context, axis, doubledCenter);
        }
        
        @Override
        public void rotate(LittlePreviews previews, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
            AxisDoorRotation doorRotation = parseRotation(previews.structureNBT);
            doorRotation.rotate(Axis.values()[previews.structureNBT.getInteger("axis")], rotation);
            doorRotation.writeToNBT(previews.structureNBT);
            super.rotate(previews, context, rotation, doubledCenter);
        }
        
    }
    
}
