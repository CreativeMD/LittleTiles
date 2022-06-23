package team.creative.littletiles.common.structure.registry.gui;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiControlClickEvent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.animation.property.AnimationProperty;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.gui.controls.GuiTileViewer.GuiTileViewerAxisChangedEvent;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.type.door.LittleAxisDoor;
import team.creative.littletiles.common.structure.type.door.LittleAxisDoor.AxisDoorRotation;
import team.creative.littletiles.common.structure.type.door.LittleAxisDoor.DirectionRotation;

@OnlyIn(Dist.CLIENT)
public class LittleDoorAxisGui extends LittleDoorBaseGui {
    
    public LittleDoorAxisGui(LittleStructureType type, AnimationGuiHandler handler) {
        super(type, handler);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public LittleAxisDoor parseStructure() {
        LittleAxisDoor door = createStructure(LittleAxisDoor.class, null);
        GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
        door.axisCenter = new StructureRelative(viewer.getBox(), viewer.getAxisContext());
        door.axis = viewer.getAxis();
        
        GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
        door.doorRotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
        door.doorRotation.parseGui(viewer, typePanel);
        return door;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void createControls(LittlePreviews previews, LittleStructure structure) {
        LittleAxisDoor door = null;
        if (structure instanceof LittleAxisDoor)
            door = (LittleAxisDoor) structure;
        
        LittleGridContext stackContext = previews.getContext();
        LittleGridContext axisContext = stackContext;
        
        GuiTileViewer viewer = new GuiTileViewer("tileviewer", 0, 0, 100, 100, stackContext);
        parent.addControl(viewer);
        boolean even = false;
        AxisDoorRotation doorRotation;
        if (door != null) {
            even = door.axisCenter.isEven();
            viewer.setEven(even);
            
            door.axisCenter.convertToSmallest();
            axisContext = door.axisCenter.getContext();
            viewer.setViewAxis(door.axis);
            viewer.setAxis(door.axisCenter.getBox(), axisContext);
            
            doorRotation = door.doorRotation;
            
        } else {
            viewer.setEven(false);
            viewer.setAxis(new LittleBox(0, 0, 0, 1, 1, 1), viewer.context);
            doorRotation = new DirectionRotation();
        }
        viewer.visibleAxis = true;
        
        parent.addControl(new GuiTabStateButton("doorRotation", rotationTypes.indexOf(doorRotation.getClass()), 110, 0, 12, rotationTypeNames.toArray(new String[0])));
        
        GuiPanel typePanel = new GuiPanel("typePanel", 110, 20, 80, 25);
        parent.addControl(typePanel);
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
                updateTimeline();
            }
        }.setCustomTooltip("change view"));
        parent.addControl(new GuiIconButton("flip view", 60, 107, 4) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.setViewDirection(viewer.getViewDirection().getOpposite());
            }
        }.setCustomTooltip("flip view"));
        
        parent.addControl(new GuiIconButton("up", 124, 58, 1) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.moveY(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1);
            }
        });
        
        parent.addControl(new GuiIconButton("right", 141, 75, 0) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.moveX(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1);
            }
        });
        
        parent.addControl(new GuiIconButton("left", 107, 75, 2) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.moveX(-(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1));
            }
        });
        
        parent.addControl(new GuiIconButton("down", 124, 75, 3) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                viewer.moveY(-(GuiScreen.isCtrlKeyDown() ? viewer.context.size : 1));
            }
        });
        
        parent.controls.add(new GuiCheckBox("even", 147, 55, even));
        
        GuiStateButton contextBox = new GuiStateButton("grid", LittleGridContext.getNames().indexOf(axisContext + ""), 170, 75, 20, 12, LittleGridContext.getNames()
                .toArray(new String[0]));
        parent.controls.add(contextBox);
        
        doorRotation.onSelected(viewer, typePanel);
        
        super.createControls(previews, structure);
    }
    
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onAxisChanged(GuiTileViewerAxisChangedEvent event) {
        GuiTileViewer viewer = (GuiTileViewer) event.source;
        handler.setCenter(new StructureAbsolute(new BlockPos(0, 75, 0), viewer.getBox().copy(), viewer.getAxisContext()));
    }
    
    @Override
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onChanged(GuiControlChangedEvent event) {
        super.onChanged(event);
        AxisDoorRotation rotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
        if (rotation.shouldUpdateTimeline((GuiControl) event.source))
            updateTimeline();
        else if (event.source.is("doorRotation")) {
            GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
            typePanel.controls.clear();
            rotation.onSelected((GuiTileViewer) parent.get("tileviewer"), typePanel);
            updateTimeline();
        } else if (event.source.is("grid")) {
            GuiStateButton contextBox = (GuiStateButton) event.source;
            LittleGridContext context;
            try {
                context = LittleGridContext.get(Integer.parseInt(contextBox.getCaption()));
            } catch (NumberFormatException e) {
                context = LittleGridContext.get();
            }
            
            GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
            LittleBox box = viewer.getBox();
            box.convertTo(viewer.getAxisContext(), context);
            
            if (viewer.isEven())
                box.maxX = box.minX + 2;
            else
                box.maxX = box.minX + 1;
            
            if (viewer.isEven())
                box.maxY = box.minY + 2;
            else
                box.maxY = box.minY + 1;
            
            if (viewer.isEven())
                box.maxZ = box.minZ + 2;
            else
                box.maxZ = box.minZ + 1;
            
            viewer.setAxis(box, context);
        }
    }
    
    @CustomEventSubscribe
    @SideOnly(Side.CLIENT)
    public void onButtonClicked(GuiControlClickEvent event) {
        GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
        if (event.source.is("even")) {
            viewer.setEven(((GuiCheckBox) event.source).value);
        }
    }
    
    @Override
    public void populateTimeline(AnimationTimeline timeline, int interpolation) {
        GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
        GuiPanel typePanel = (GuiPanel) parent.get("typePanel");
        AxisDoorRotation doorRotation = createRotation(((GuiTabStateButton) parent.get("doorRotation")).getState());
        doorRotation.parseGui(viewer, typePanel);
        
        doorRotation.populateTimeline(timeline, timeline.duration, interpolation, AnimationProperty.getRotation(viewer.getAxis()));
    }
    
    @Override
    public void onLoaded(AnimationPreview animationPreview) {
        super.onLoaded(animationPreview);
        GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
        onAxisChanged(new GuiTileViewerAxisChangedEvent(viewer));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleAxisDoor.class);
    }
}
