package team.creative.littletiles.common.placement.mark;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.littletiles.client.gui.SubGuiMarkMode;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;

@OnlyIn(Dist.CLIENT)
public class MarkMode implements IMarkMode {
    
    public static PlacementPosition loadPosition(PlacementPosition position, PlacementPreview preview) {
        if (!preview.previews.isAbsolute()) {
            position.setVecContext(new LittleVecGrid(preview.box.getCenter(), preview.context));
            
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
    
    public MarkMode(Player player, PlacementPosition position, PlacementPreview preview) {
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
    public void render(double x, double y, double z) {
        GlStateManager.enableBlend();
        GlStateManager
                .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        AxisAlignedBB box = position.getBox().grow(0.002).offset(-x, -y, -z);
        
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
    public void move(LittleGrid context, Facing facing) {
        LittleVec vec = new LittleVec(facing);
        vec.scale(GuiScreen.isCtrlKeyDown() ? context.size : 1);
        position.subVec(vec);
    }
    
    @Override
    public void done() {
        
    }
    
}
