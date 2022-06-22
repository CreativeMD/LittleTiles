package team.creative.littletiles.common.structure.type.door;

import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.DoorController;
import team.creative.littletiles.common.animation.ValueTimeline;
import team.creative.littletiles.common.animation.property.AnimationProperty;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleSlidingDoor extends LittleDoorBase {
    
    public LittleSlidingDoor(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @StructureDirectional
    public Facing direction;
    public int moveDistance;
    public LittleGrid moveContext;
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        moveDistance = nbt.getInt("distance");
        moveContext = LittleGrid.get(nbt);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.putInt("distance", moveDistance);
        moveContext.set(nbt);
    }
    
    @Override
    public DoorController createController(UUIDSupplier supplier, Placement placement, int completeDuration) {
        //((LittleSlidingDoor) placement.origin.getStructure()).direction = direction.getOpposite();
        return new DoorController(supplier, new AnimationState(), new AnimationState().set(AnimationProperty.getOffset(direction.getAxis()), direction.getAxisDirection()
                .getOffset() * moveContext.toVanillaGrid(moveDistance)), stayAnimated ? null : false, duration, completeDuration, interpolation);
    }
    
    @Override
    public void transformDoorPreview(LittleAbsolutePreviews previews) {
        
    }
    
    @Override
    public StructureAbsolute getAbsoluteAxis() {
        return new StructureAbsolute(getPos(), new LittleBox(0, 0, 0, 1, 1, 1), mainBlock.getContext());
    }
    
}
