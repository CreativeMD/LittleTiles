package team.creative.littletiles.common.structure.registry.gui;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.controls.GuiTileViewer;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.bed.LittleBed;

public class LittleBedParser extends LittleStructureGuiControl {
    
    public LittleBedParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
        parent.registerEventClick(x -> {
            GuiTileViewer viewer = (GuiTileViewer) parent.get("tileviewer");
            GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) parent.get("relativeDirection");
            
            EnumFacing direction = EnumFacing.getHorizontal(((GuiStateButton) parent.get("direction")).getState());
            
            LittleSlidingDoorParser.updateDirection(viewer, direction.getOpposite(), relativeDirection);
        });
    }
    
    @Override
    public void createControls(LittleGroup previews, LittleStructure structure) {
        GuiTileViewer tile = new GuiTileViewer("tileviewer", previews.getGrid());
        tile.setViewDirection(Facing.UP);
        parent.add(tile);
        
        LittleVec size = previews.getSize();
        int index = EnumFacing.EAST.getHorizontalIndex();
        if (size.x < size.z)
            index = EnumFacing.SOUTH.getHorizontalIndex();
        if (structure instanceof LittleBed)
            index = ((LittleBed) structure).direction.getHorizontalIndex();
        if (index < 0)
            index = 0;
        parent.add(new GuiStateButton("direction", index, RotationUtils.getHorizontalFacingNames()));
        
        GuiDirectionIndicator relativeDirection = new GuiDirectionIndicator("relativeDirection", Facing.UP);
        parent.add(relativeDirection);
        LittleSlidingDoorParser.updateDirection(tile, Facing.getHorizontal(index).getOpposite(), relativeDirection);
    }
    
    @Override
    public LittleBed parseStructure(LittleGroup previews) {
        Facing direction = Facing.getHorizontal(((GuiStateButton) parent.get("direction")).getState());
        LittleBed bed = createStructure(LittleBed.class, null);
        bed.direction = direction;
        return bed;
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleBed.class);
    }
}