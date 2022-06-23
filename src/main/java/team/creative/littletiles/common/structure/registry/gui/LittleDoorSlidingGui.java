package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiControlClickEvent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.animation.property.AnimationProperty;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.controls.GuiLTDistance;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.door.LittleSlidingDoor;

@OnlyIn(Dist.CLIENT)
public class LittleDoorSlidingGui extends LittleDoorBaseGui {
    
    public LittleDoorSlidingGui(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @SideOnly(Side.CLIENT)
    @CustomEventSubscribe
    public void buttonClicked(GuiControlClickEvent event) {
        if (event.source.is("direction")) {
            GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
            EnumFacing direction = EnumFacing.getFront(((GuiStateButton) event.source).getState());
            GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) parent.get("relativeDirection");
            updateButtonDirection(viewer, direction, relativeDirection);
        }
    }
    
    @Override
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onChanged(GuiControlChangedEvent event) {
        super.onChanged(event);
        if (event.source.is("distance"))
            updateTimeline();
    }
    
    @SideOnly(Side.CLIENT)
    public void updateButtonDirection(GuiTileViewer viewer, EnumFacing direction, GuiDirectionIndicator relativeDirection) {
        updateDirection(viewer, direction, relativeDirection);
        updateTimeline();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void createControls(LittlePreviews previews, @Nullable LittleStructure structure) {
        LittleSlidingDoor door = null;
        if (structure instanceof LittleSlidingDoor)
            door = (LittleSlidingDoor) structure;
        
        LittleVec size = previews.getSize();
        
        int index = EnumFacing.UP.ordinal();
        if (door != null)
            index = door.direction.ordinal();
        EnumFacing direction = EnumFacing.getFront(index);
        
        LittleGridContext context = previews.getContext();
        
        GuiTileViewer viewer = new GuiTileViewer("tileviewer", 0, 0, 100, 100, context);
        viewer.visibleAxis = false;
        parent.addControl(viewer);
        
        parent.addControl(new GuiStateButton("direction", index, 110, 0, 37, 12, RotationUtils.getFacingNames()));
        
        GuiDirectionIndicator relativeDirection = new GuiDirectionIndicator("relativeDirection", 155, 0, EnumFacing.UP);
        parent.addControl(relativeDirection);
        
        int distance = size.get(direction.getAxis());
        if (door != null) {
            distance = door.moveDistance;
            context = door.moveContext;
        }
        parent.addControl(new GuiLTDistance("distance", 110, 21, context, distance));
        
        parent.addControl(new GuiIconButton("reset view", 20, 107, 8) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.offsetX.set(0);
                viewer.offsetY.set(0);
                viewer.scale.set(40);
            }
        }.setCustomTooltip("reset view"));
        parent.addControl(new GuiIconButton("change view", 40, 107, 7) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                switch (viewer.getAxis()) {
                    case X:
                        viewer.setViewAxis(EnumFacing.Axis.Y);
                        break;
                    case Y:
                        viewer.setViewAxis(EnumFacing.Axis.Z);
                        break;
                    case Z:
                        viewer.setViewAxis(EnumFacing.Axis.X);
                        break;
                    default:
                        break;
                }
                
                updateButtonDirection(viewer, direction, relativeDirection);
            }
        }.setCustomTooltip("change view"));
        parent.addControl(new GuiIconButton("flip view", 60, 107, 4) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.setViewDirection(viewer.getViewDirection().getOpposite());
                updateDirection(viewer, direction, relativeDirection);
            }
        }.setCustomTooltip("flip view"));
        
        super.createControls(previews, structure);
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public LittleSlidingDoor parseStructure() {
        EnumFacing direction = EnumFacing.getFront(((GuiStateButton) parent.get("direction")).getState());
        GuiLTDistance distance = (GuiLTDistance) parent.get("distance");
        
        LittleSlidingDoor door = createStructure(LittleSlidingDoor.class, null);
        door.direction = direction;
        door.moveDistance = distance.getDistance();
        door.moveContext = distance.getDistanceContext();
        
        return door;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void onLoaded(AnimationPreview animationPreview) {
        super.onLoaded(animationPreview);
        
        GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
        GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) parent.get("relativeDirection");
        
        EnumFacing direction = EnumFacing.getFront(((GuiStateButton) parent.get("direction")).getState());
        
        updateButtonDirection(viewer, direction, relativeDirection);
    }
    
    @Override
    public void populateTimeline(AnimationTimeline timeline, int interpolation) {
        EnumFacing direction = EnumFacing.getFront(((GuiStateButton) parent.get("direction")).getState());
        GuiLTDistance distance = (GuiLTDistance) parent.get("distance");
        
        timeline.values.add(AnimationProperty.getOffset(direction.getAxis()), ValueTimeline.create(interpolation).addPoint(0, 0D)
                .addPoint(timeline.duration, direction.getAxisDirection().getOffset() * distance.getDistanceContext().toVanillaGrid(distance.getDistance())));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleSlidingDoor.class);
    }
}