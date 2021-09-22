package com.creativemd.littletiles.common.structure.type.premade.signal;

import java.nio.ByteBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.math.box.BoxCorner;
import com.creativemd.creativecore.common.utils.math.box.BoxFace;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.structure.registry.LittleStructureType;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleSignalDisplay extends LittleStructurePremade {
    
    public static final int renderDistance = 64;
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public EnumFacing facing;
    
    @StructureDirectional
    public Vector3f topRight;
    
    private int textureId = -1;
    
    public LittleSignalDisplay(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {}
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {}
    
    @Override
    public void receiveInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("pixels") && isClient())
            updateTexture();
    }
    
    @SideOnly(Side.CLIENT)
    public void updateTexture() {
        if (textureId == -1)
            textureId = GlStateManager.generateTexture();
        GlStateManager.bindTexture(textureId);
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
    @SideOnly(Side.CLIENT)
    public void renderTick(BlockPos pos, double x, double y, double z, float partialTickTime) {
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
            builder.pos(box.getValueOfFacing(corner.x), box.getValueOfFacing(corner.y), box.getValueOfFacing(corner.z))
                
                .tex(corner.isFacingPositive(uAxis) != (VectorUtils.get(uAxis, topRight) > 0) ? 1 : 0, corner.isFacingPositive(vAxis) != (VectorUtils.get(vAxis, topRight) > 0) ? 1 : 0)
                .endVertex();
        tessellator.draw();
        
        GlStateManager.popMatrix();
        
        GlStateManager.cullFace(CullFace.BACK);
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Math.pow(renderDistance, 2);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return frame.getBox().getBox(frame.getContext());
    }
    
    @Override
    public void unload() {
        super.unload();
        if (getWorld().isRemote && textureId != -1)
            GlStateManager.deleteTexture(textureId);
    }
    
}
