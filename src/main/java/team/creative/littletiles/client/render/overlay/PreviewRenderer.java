package team.creative.littletiles.client.render.overlay;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

public class PreviewRenderer {
    
    public static void renderShape(PoseStack pose, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack.Pose posestack$pose = pose.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float f = (float) (x2 - x1);
            float f1 = (float) (y2 - y1);
            float f2 = (float) (z2 - z1);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            consumer.addVertex(posestack$pose.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).setColor(red, green, blue, alpha).setNormal(posestack$pose, f, f1, f2);
            consumer.addVertex(posestack$pose.pose(), (float) (x2 + x), (float) (y2 + y), (float) (z2 + z)).setColor(red, green, blue, alpha).setNormal(posestack$pose, f, f1, f2);
        });
    }
    
    public static final PoseStack EMPTY = new PoseStack();
    
    public final PreviewManager manager;
    
    public PreviewRenderer(PreviewManager manager) {
        this.manager = manager;
    }
    
    public Player player() {
        return manager.player();
    }
    
    public Level level() {
        return manager.level();
    }
    
    public BlockHitResult blockHit() {
        return manager.blockHit();
    }
    
    public Level blockHitLevel() {
        return manager.blockHitLevel();
    }
    
    public ILittleLevelContext blockHitContext() {
        return manager.blockHitContext();
    }
    
    public boolean isUsingSecondMode() {
        return LittleActionHandlerClient.isUsingSecondMode();
    }
    
    public LittleTileContext selectFocused(BlockHitResult result) {
        return LittleTileContext.selectFocused(level(), result.getBlockPos(), player());
    }
    
    public float partialTickTime() {
        return TickUtils.getFrameTime(level());
    }
    
    public boolean isVisible(AABB bb) {
        return Minecraft.getInstance().levelRenderer.getFrustum().isVisible(bb);
    }
    
    public int select(ILittleLevelContext context, List<ShapePosition> positions) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        var player = player();
        float partialTickTime = partialTickTime();
        Vec3 pos = player.getEyePosition(partialTickTime);
        double reach = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
        
        pos = context.toFakeWorld(pos);
        look = context.toFakeWorld(look);
        for (int i = 0; i < positions.size(); i++) {
            Optional<Vec3> result = positions.get(i).getBox().clip(pos, look);
            if (result.isPresent()) {
                double tempDistance = pos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        return index;
    }
    
    public int selectBox(ILittleLevelContext context, List<LittleBoxAbsolute> positions) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        var player = player();
        float partialTickTime = partialTickTime();
        Vec3 pos = player.getEyePosition(partialTickTime);
        double reach = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
        
        pos = context.toFakeWorld(pos);
        look = context.toFakeWorld(look);
        
        for (int i = 0; i < positions.size(); i++) {
            Optional<Vec3> result = positions.get(i).toAABB().clip(pos, look);
            if (result.isPresent()) {
                double tempDistance = pos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        return index;
    }
    
    public void setupPreviewRendererLines(float red, float green, float blue, float alpha, float lineWidth) {
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(lineWidth);
        
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.enableDepthTest();
    }
    
    public void setupPreviewRenderer(boolean lines) {
        if (lines) {
            setupPreviewRendererLines(0, 0, 0, 0.4F, (float) LittleTiles.CONFIG.rendering.previewLineThickness);
            return;
        }
        if (LittleTiles.CONFIG.rendering.darkerPreviewBoxShading) {
            GL14.glBlendColor(0.25F, 0.25F, 0.25F, 0.25F);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.CONSTANT_COLOR, GlStateManager.DestFactor.ONE_MINUS_DST_COLOR, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        } else
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        
        double alpha = (float) (Math.sin(System.nanoTime() / 200000000D) * 0.2 + 0.5);
        RenderSystem.setShaderColor(1, 1, 1, (float) alpha);
        
        RenderSystem.setShaderTexture(0, PreviewManager.WHITE_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.enableCull();
    }
    
    public ByteBufferBuilder createBuffer() {
        return new ByteBufferBuilder(86432);
    }
    
    public BufferBuilder createBuilder(ByteBufferBuilder buffer, boolean lines) {
        if (lines)
            return new BufferBuilder(buffer, VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }
    
    public BufferBuilder createTesselatorBuilder(boolean lines) {
        if (lines)
            return Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }
    
    public void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines) {
        buildBox(pose, box, builder, colorAlpha, lines, false);
    }
    
    public void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines, boolean forceWhite) {
        if (lines)
            box.renderLines(pose, builder, colorAlpha, box.getCenter(), 0.001, forceWhite);
        else
            box.renderPreview(pose, builder, colorAlpha);
    }
    
    public void renderBoxes(Vec3 cam, BlockPos pos, boolean lines, MeshData data) {
        renderBoxes(cam, pos, lines, data, null);
    }
    
    public void renderBoxes(Vec3 cam, BlockPos pos, boolean lines, MeshData data, @Nullable Runnable adjustGL) {
        renderBoxes(ILittleLevelContext.STANDARD, cam, pos, lines, data, adjustGL);
    }
    
    public void renderBoxes(ILittleLevelContext context, Vec3 cam, BlockPos pos, boolean lines, MeshData data, @Nullable Runnable adjustGL) {
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        context.transformMatrix(matrix, pos.getX(), pos.getY(), pos.getZ(), cam, 0);
        
        RenderSystem.applyModelViewMatrix();
        
        setupPreviewRenderer(lines);
        
        if (adjustGL != null)
            adjustGL.run();
        
        BufferUploader.drawWithShader(data);
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
    
    public BoxRenderResult buildBoxes(PoseStack pose, LittleBoxes boxes, boolean lines) {
        return buildBoxes(pose, boxes, lines, false);
    }
    
    public BoxRenderResult buildBoxes(PoseStack pose, LittleBoxes boxes, boolean lines, boolean forceWhite) {
        ByteBufferBuilder buffer = createBuffer();
        var builder = createBuilder(buffer, lines);
        for (LittleBox box : boxes.all()) {
            LittleRenderBox cube = box.getRenderingBox(boxes.getGrid());
            if (cube != null)
                buildBox(pose, cube, builder, 255, lines, forceWhite);
        }
        var mesh = builder.build();
        if (mesh instanceof MeshDataExtender m)
            m.keepAlive(true);
        return new BoxRenderResult(boxes, boxes.pos, buffer, mesh);
    }
    
    public void renderPositions(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<ShapePosition> positions, @Nullable Int2BooleanFunction marked) {
        pose.pushPose();
        context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
        for (int i = 0; i < positions.size(); i++) {
            var box = positions.get(i).getBB();
            box.move(-pos.getX(), -pos.getY(), -pos.getZ());
            renderLineBox(pose, box, marked != null && marked.get(i));
        }
        pose.popPose();
    }
    
    public void renderBoxes(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<LittleBoxAbsolute> boxes, @Nullable Int2BooleanFunction marked) {
        pose.pushPose();
        context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
        for (int i = 0; i < boxes.size(); i++) {
            var box = boxes.get(i).toABB();
            box.move(-pos.getX(), -pos.getY(), -pos.getZ());
            renderLineBox(pose, box, marked != null && marked.get(i));
        }
        pose.popPose();
    }
    
    public void renderSeethroughLines(Vec3 cam, boolean lines, BlockPos pos, MeshData data, int color) {
        renderSeethroughLines(cam, lines, pos, data, color, 0.4F);
    }
    
    public void renderSeethroughLines(Vec3 cam, boolean lines, BlockPos pos, MeshData data, int color, float alpha) {
        renderBoxes(cam, pos, lines, data, () -> {
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(0, 0, 0, alpha);
            RenderSystem.lineWidth(4);
        });
        renderBoxes(cam, pos, lines, data, () -> {
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(ColorUtils.redF(color), ColorUtils.greenF(color), ColorUtils.blueF(color), alpha);
            RenderSystem.lineWidth(2);
        });
        
        RenderSystem.enableDepthTest();
    }
    
    public void renderLineBox(PoseStack pose, ABB box, boolean selected) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        
        box.inflate(0.002);
        
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        
        RenderSystem.lineWidth(4.0F);
        box.renderLines(pose, bufferbuilder, 0, 0, 0, 1F);
        
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        
        RenderSystem.disableDepthTest();
        if (selected) {
            bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.lineWidth(1.0F);
            box.renderLines(pose, bufferbuilder, 1F, 0.3F, 0.0F, 1F);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        }
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }
    
    public static record BoxRenderResult(LittleBoxes boxes, BlockPos pos, ByteBufferBuilder buffer, MeshData data) {
        
        public void close() {
            if (data instanceof MeshDataExtender m) {
                m.keepAlive(false);
                data.close();
            }
            if (buffer != null)
                buffer.close();
        }
    }
    
}
