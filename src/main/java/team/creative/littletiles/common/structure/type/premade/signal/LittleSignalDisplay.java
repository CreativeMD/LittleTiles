package team.creative.littletiles.common.structure.type.premade.signal;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittleSignalDisplay extends LittleStructurePremade {
    
    public static final int RENDER_DISTANCE = 64;
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vec3f topRight;
    
    public int color = ColorUtils.WHITE;
    
    private int textureId = -1;
    
    public LittleSignalDisplay(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (nbt.contains("color"))
            color = nbt.getInt("color");
        else
            color = ColorUtils.WHITE;
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (color != -1)
            nbt.putInt("color", color);
        else
            nbt.remove("color");
    }
    
    @Override
    public void receiveInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("pixels") && isClient())
            updateTexture();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void updateTexture() {
        if (textureId == -1)
            textureId = GlStateManager._genTexture();
        
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, GL11.GL_ZERO);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, GL11.GL_ZERO);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, GL11.GL_ZERO);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, GL11.GL_ONE);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        int bandwidth = getOutput(0).getBandwidth();
        SignalState state = getOutput(0).getState();
        ByteBuffer buffer = ByteBuffer.allocateDirect(bandwidth * 4);
        byte r = (byte) ColorUtils.red(color);
        byte g = (byte) ColorUtils.green(color);
        byte b = (byte) ColorUtils.blue(color);
        byte a = (byte) ColorUtils.alpha(color);
        for (int i = 0; i < bandwidth; i++) {
            if (state.is(i)) {
                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
                buffer.put(a);
            } else {
                buffer.put((byte) 0);
                buffer.put((byte) 0);
                buffer.put((byte) 0);
                buffer.put(a);
            }
        }
        buffer.rewind();
        
        int size = (int) Math.sqrt(bandwidth);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, size, size, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTick(PoseStack pose, MultiBufferSource buffer, BlockPos pos, float partialTickTime) {
        super.renderTick(pose, buffer, pos, partialTickTime);
        if (textureId == -1)
            updateTexture();
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.bindTexture(textureId);
        RenderSystem.setShaderTexture(0, textureId);
        
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        pose.pushPose();
        
        AlignedBox box = frame.getBox().getBox(frame.getGrid());
        BoxFace face = BoxFace.get(facing);
        if (facing.positive)
            box.setMax(facing.axis, box.getMin(facing.axis) + 0.005F);
        else
            box.setMin(facing.axis, box.getMax(facing.axis) - 0.005F);
        Axis uAxis = face.getTexUAxis();
        Axis vAxis = face.getTexVAxis();
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (BoxCorner corner : face.corners)
            builder.addVertex(pose.last().pose(), box.get(corner.x), box.get(corner.y), box.get(corner.z)).setUv(corner.isFacingPositive(uAxis) != (topRight.get(
                uAxis) > 0) ? 1 : 0, corner.isFacingPositive(vAxis) != (topRight.get(vAxis) > 0) ? 1 : 0);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        
        pose.popPose();
        
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
    }
    
    @Override
    public boolean hasStructureColor() {
        return true;
    }
    
    @Override
    public int getStructureColor() {
        return color;
    }
    
    @Override
    public int getDefaultColor() {
        return ColorUtils.WHITE;
    }
    
    @Override
    public void paint(int color) {
        this.color = color;
        if (isClient())
            updateTexture();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistance() {
        return RENDER_DISTANCE;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return frame.getBox().getBB(frame.getGrid());
    }
    
    @Override
    public void unload() {
        super.unload();
        if (isClient() && textureId != -1)
            GlStateManager._deleteTexture(textureId);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void getRenderingBoxes(BlockPos pos, ChunkLayerMapList<LittleRenderBox> cubes) {
        if (ColorUtils.isInvisible(color))
            return;
        
        int color = this.color == ColorUtils.WHITE ? LittleSignalCableBase.DEFAULT_CABLE_COLOR : this.color;
        RenderType layer = ColorUtils.isTransparent(color) ? RenderType.translucent() : RenderType.solid();
        
        try {
            SurroundingBox box = getSurroundingBox();
            LittleVec min = box.getMinPosOffset();
            LittleVec max = box.getSize();
            max.add(min);
            LittleBox overallBox = new LittleBox(min, max);
            BlockPos difference = pos.subtract(box.getMinPos());
            overallBox.sub(box.getGrid().toGrid(difference.getX()), box.getGrid().toGrid(difference.getY()), box.getGrid().toGrid(difference.getZ()));
            
            LittleRenderBox block = new LittleRenderBox(box.getGrid(), overallBox, LittleTilesRegistry.CLEAN.value().defaultBlockState()).setColor(color);
            block.allowOverlap = true;
            cubes.add(layer, block);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
}
