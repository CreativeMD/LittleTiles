package team.creative.littletiles.common.placement.mark;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementPosition;

@OnlyIn(Dist.CLIENT)
public class MarkMode implements IMarkMode {
    
    public PlacementPosition position = null;
    public boolean allowLowResolution = true;
    
    public MarkMode(Player player, PlacementPosition position) {
        this.position = position;
    }
    
    @Override
    public boolean allowLowResolution() {
        return allowLowResolution;
    }
    
    @Override
    public PlacementPosition getPosition() {
        return position.copy();
    }
    
    @Override
    public void render(LittleGrid positionGrid, PoseStack pose) {
        position.render(pose, true, positionGrid);
    }
    
    @Override
    public void move(LittleGrid positionGrid, Facing facing) {
        LittleVec vec = new LittleVec(facing.opposite());
        vec.scale(Screen.hasControlDown() ? positionGrid.count : 1);
        position.sub(new LittleVecGrid(vec, positionGrid));
    }
    
    @Override
    public void done() {}
    
}
