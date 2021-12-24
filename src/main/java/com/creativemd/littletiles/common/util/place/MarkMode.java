package com.creativemd.littletiles.common.util.place;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.littletiles.client.gui.SubGuiMarkMode;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MarkMode implements IMarkMode {
    
    public static PlacementPosition loadPosition(PlacementPosition position, PlacementPreview preview) {
        if (!preview.previews.isAbsolute()) {
            position.setVecContext(new LittleVecContext(preview.box.getCenter(), preview.context));
            
            EnumFacing facing = position.facing;
            if (preview.mode.placeInside)
                facing = facing.getOpposite();
            
            LittleVec center = preview.size.calculateCenter();
            LittleVec centerInv = preview.size.calculateInvertedCenter();
            switch (facing) {
            case EAST:
                position.getVec().x -= center.x;
                break;
            case WEST:
                position.getVec().x += centerInv.x;
                break;
            case UP:
                position.getVec().y -= center.y;
                break;
            case DOWN:
                position.getVec().y += centerInv.y;
                break;
            case SOUTH:
                position.getVec().z -= center.z;
                break;
            case NORTH:
                position.getVec().z += centerInv.z;
                break;
            default:
                break;
            }
            
            if (preview.previews.size() > 1 && preview.fixed)
                position.addVec(preview.cachedOffset);
        }
        return position;
    }
    
    public PlacementPosition position = null;
    public boolean allowLowResolution = true;
    
    public MarkMode(EntityPlayer player, PlacementPosition position, PlacementPreview preview) {
        this.position = loadPosition(position, preview);
    }
    
    @Override
    public boolean allowLowResolution() {
        return allowLowResolution;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGui getConfigurationGui() {
        return new SubGuiMarkMode(this);
    }
    
    @Override
    public PlacementPosition getPosition() {
        return position.copy();
    }
    
    @Override
    public void render(LittleGridContext positionContext, double x, double y, double z) {
        GlStateManager.enableBlend();
        GlStateManager
            .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        double posX = position.getPosX();
        double posY = position.getPosY();
        double posZ = position.getPosZ();
        AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + positionContext.pixelSize, y + positionContext.pixelSize, z + positionContext.pixelSize).grow(0.002).offset(-x, -y, -z);
        
        GlStateManager.glLineWidth(4.0F);
        RenderGlobal.drawSelectionBoundingBox(box, 0.0F, 0.0F, 0.0F, 1F);
        
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0F);
        RenderGlobal.drawSelectionBoundingBox(box, 1F, 0.3F, 0.0F, 1F);
        GlStateManager.enableDepth();
        
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    @Override
    public void move(LittleGridContext positionContext, EnumFacing facing) {
        LittleVec vec = new LittleVec(facing.getOpposite());
        vec.scale(GuiScreen.isCtrlKeyDown() ? positionContext.size : 1);
        position.sub(new LittleVecContext(vec, positionContext));
    }
    
    @Override
    public void done() {}
    
}
