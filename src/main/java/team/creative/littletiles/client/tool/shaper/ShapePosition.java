package team.creative.littletiles.client.tool.shaper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;

public class ShapePosition extends PlacementPosition {
    
    public final BlockHitResult ray;
    public final LittleTileContext result;
    
    public ShapePosition(Player player, PlacementPosition position, BlockHitResult result, boolean whenClicked, boolean inside) {
        super(position);
        this.ray = result;
        this.result = LittleTileContext.selectFocused(player.level(), result.getBlockPos(), player, whenClicked ? 1 : TickUtils.getFrameTime(player.level()));
        LittleGrid grid = getGrid();
        if (position.facing != null)
            if (inside) {
                if (position.facing.positive && grid.isAtEdge(position.facing.axis.get(result.getLocation())))
                    getVec().sub(position.facing);
            } else if (!position.facing.positive && grid.isAtEdge(position.facing.axis.get(result.getLocation())))
                getVec().add(position.facing);
    }
    
    public ShapePosition(PlacementPosition position, BlockHitResult ray, LittleTileContext result) {
        super(position);
        this.ray = ray;
        this.result = result;
    }
    
    public void move(LittleGrid context, Facing facing) {
        LittleVec vec = new LittleVec(facing);
        vec.scale(Screen.hasControlDown() ? context.count : 1);
        subVec(vec);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShapePosition s) {
            if (!super.equals(obj))
                return false;
            if (result.parent != s.result.parent)
                return false;
            if (result.tile != s.result.tile)
                return false;
            return true;
        }
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack pose, boolean selected) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        
        AABB box = this.getBox().inflate(0.002);
        
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        
        RenderSystem.lineWidth(4.0F);
        LevelRenderer.renderLineBox(pose, bufferbuilder, box, 0, 0, 0, 1F);
        
        RenderSystem.disableDepthTest();
        if (selected) {
            RenderSystem.lineWidth(1.0F);
            LevelRenderer.renderLineBox(pose, bufferbuilder, box, 1F, 0.3F, 0.0F, 1F);
        }
        
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        
        RenderSystem.enableDepthTest();
    }
    
    @Override
    public ShapePosition copy() {
        return new ShapePosition(super.copy(), ray, result);
    }
    
}
