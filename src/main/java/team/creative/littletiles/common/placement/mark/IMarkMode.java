package team.creative.littletiles.common.placement.mark;

import com.creativemd.creativecore.common.gui.container.SubGui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.placement.PlacementPosition;

public interface IMarkMode {
    
    public boolean allowLowResolution();
    
    public PlacementPosition getPosition();
    
    @OnlyIn(Dist.CLIENT)
    public SubGui getConfigurationGui();
    
    public void render(double x, double y, double z);
    
    public void move(LittleGrid context, Facing facing);
    
    public void done();
    
}
