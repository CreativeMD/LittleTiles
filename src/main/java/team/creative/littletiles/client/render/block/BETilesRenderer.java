package team.creative.littletiles.client.render.block;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;

public class BETilesRenderer implements BlockEntityRenderer<BETiles> {
    
    private WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();
    private VertexBufferUploader vertexUploader = new VertexBufferUploader();
    
    public static BlockRenderLayer[] layers = BlockRenderLayer.values();
    
    @Override
    public boolean isGlobalRenderer(TileEntityLittleTiles te) {
        AxisAlignedBB box = te.getRenderBoundingBox();
        if (Math.abs(box.maxX - box.minX) > 16)
            return true;
        if (Math.abs(box.maxY - box.minY) > 16)
            return true;
        if (Math.abs(box.maxZ - box.minZ) > 16)
            return true;
        return false;
    }
    
    public void renderDebugBoundingBox(AxisAlignedBB axisalignedbb, double x, double y, double z) {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        // float f = entityIn.width / 2.0F;
        // AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();
        RenderGlobal
                .drawBoundingBox(axisalignedbb.minX + x, axisalignedbb.minY + y, axisalignedbb.minZ + z, axisalignedbb.maxX + x, axisalignedbb.maxY + y, axisalignedbb.maxZ + z, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
    
    @Override
    public void render(TileEntityLittleTiles te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        for (LittleStructure structure : te.loadedStructures(LittleStructureAttribute.TICK_RENDERING))
            structure.renderTick(te.getPos(), x, y, z, partialTicks);
    }
}
