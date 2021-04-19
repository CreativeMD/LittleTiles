package com.creativemd.littletiles.common.util.place;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMarkMode {
    
    public boolean allowLowResolution();
    
    public PlacementPosition getPosition();
    
    @SideOnly(Side.CLIENT)
    public SubGui getConfigurationGui();
    
    public void render(double x, double y, double z);
    
    public void move(LittleGridContext context, EnumFacing facing);
    
    public void done();
    
}
