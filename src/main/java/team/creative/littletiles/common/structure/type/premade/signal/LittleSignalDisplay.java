package team.creative.littletiles.common.structure.type.premade.signal;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class LittleSignalDisplay extends LittleStructurePremade {
    
    public static final int renderDistance = 64;
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vec3f topRight;
    
    private int textureId = -1;
    
    public LittleSignalDisplay(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(CompoundTag nbt) {}
    
    @Override
    protected void writeToNBTExtra(CompoundTag nbt) {}
    
    @Override
    public void receiveInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("pixels") && isClient())
            updateTexture();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void updateTexture() {
        if (textureId == -1)
            textureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(textureId);
        boolean[] state = getOutput(0).getState();
        ByteBuffer buffer = ByteBuffer.allocateDirect(state.length * 3);
        for (int i = 0; i < state.length; i++) {
            if (state[i]) {
                buffer.put((byte) 255);
                buffer.put((byte) 255);
                buffer.put((byte) 255);
            } else {
                buffer.put((byte) 0);
                buffer.put((byte) 0);
                buffer.put((byte) 0);
            }
        }
        buffer.rewind();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA2, 4, 4, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTick(PoseStack pose, BlockPos pos, double x, double y, double z, float partialTickTime) {
        if (textureId == -1)
            updateTexture();
        
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.disableLighting();
        GlStateManager.bindTexture(textureId);
        
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        GlStateManager.pushMatrix();
        
        GlStateManager.translate(x, y, z);
        
        AlignedBox box = frame.getBox().getCube(frame.getContext());
        BoxFace face = BoxFace.get(facing);
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            box.setMax(facing.getAxis(), box.getMin(facing.getAxis()) + 0.01F);
        else
            box.setMin(facing.getAxis(), box.getMax(facing.getAxis()) - 0.01F);
        Axis uAxis = face.getTexUAxis();
        Axis vAxis = face.getTexVAxis();
        
        GlStateManager.enableRescaleNormal();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
        for (BoxCorner corner : face.corners)
            builder.pos(box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    
                    .tex(corner.isFacingPositive(uAxis) != (VectorUtils.get(uAxis, topRight) > 0) ? 1 : 0, corner
                            .isFacingPositive(vAxis) != (VectorUtils.get(vAxis, topRight) > 0) ? 1 : 0)
                    .endVertex();
        tessellator.draw();
        
        GlStateManager.popMatrix();
        
        GlStateManager.cullFace(CullFace.BACK);
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Math.pow(renderDistance, 2);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return frame.getBox().getBB(frame.getGrid());
    }
    
    @Override
    public void unload() {
        super.unload();
        if (getLevel().isClientSide && textureId != -1)
            GlStateManager._deleteTexture(textureId);
    }
    
}
