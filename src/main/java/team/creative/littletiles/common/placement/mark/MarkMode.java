package team.creative.littletiles.common.placement.mark;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.GuiMarkMode;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;

@OnlyIn(Dist.CLIENT)
public class MarkMode implements IMarkMode {
    
    public static PlacementPosition loadPosition(PlacementPosition position, PlacementPreview preview) {
        /*if (!preview.previews.isAbsolute()) {
            position.setVecContext(new LittleVecGrid(preview.box.getCenter(), preview.previews.getGrid()));
            
            Facing facing = position.facing;
            if (preview.mode.placeInside)
                facing = facing.opposite();
            
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
        }*/
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
    public GuiConfigure getConfigurationGui() {
        return new GuiMarkMode(this);
    }
    
    @Override
    public PlacementPosition getPosition() {
        return position.copy();
    }
    
    @Override
    public void render(LittleGrid positionGrid, PoseStack pose) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        
        AABB box = position.getBox(positionGrid).inflate(0.002);
        
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        
        RenderSystem.lineWidth(4.0F);
        LevelRenderer.renderLineBox(pose, bufferbuilder, box, 0, 0, 0, 1F);
        tesselator.end();
        
        RenderSystem.disableDepthTest();
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderSystem.lineWidth(1.0F);
        LevelRenderer.renderLineBox(pose, bufferbuilder, box, 1F, 0.3F, 0.0F, 1F);
        tesselator.end();
        RenderSystem.enableDepthTest();
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
