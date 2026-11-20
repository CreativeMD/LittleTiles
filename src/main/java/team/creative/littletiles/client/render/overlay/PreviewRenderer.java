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
import team.creative.littletiles.common.math.box.LittleBoxLevel;
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
        return LittleTileContext.selectFocused(blockHitLevel(), result.getBlockPos(), player());
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
    
    public int selectBox(List<LittleBoxLevel> positions) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        var player = player();
        float partialTickTime = partialTickTime();
        Vec3 pos = player.getEyePosition(partialTickTime);
        double reach = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
        
        for (int i = 0; i < positions.size(); i++) {
            var context = positions.get(i).context;
            var contextPos = context.toFakeWorld(pos);
            var contextLook = context.toFakeWorld(look);
            Optional<Vec3> result = positions.get(i).toAABB().clip(contextPos, contextLook);
            if (result.isPresent()) {
                double tempDistance = contextPos.distanceToSqr(result.get());
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
    
    protected void startPreviewRender(boolean lines) {
        var config = LittleTiles.CONFIG.rendering.preview;
        if (lines) {
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth((float) config.lineThickness);
            config.lineColor.glColor();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        } else {
            if (config.darkerBoxShading) {
                GL14.glBlendColor(0.25F, 0.25F, 0.25F, 0.25F);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.CONSTANT_COLOR, GlStateManager.DestFactor.ONE_MINUS_DST_COLOR, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            } else
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            
            float alpha = (float) (Math.sin(System.nanoTime() / 200000000D) * config.boxAlphaFade);
            RenderSystem.setShaderColor(config.boxColor.getRedDecimal(), config.boxColor.getGreenDecimal(), config.boxColor.getBlueDecimal(), config.boxColor
                    .getAlphaDecimal() + alpha);
            
            RenderSystem.setShaderTexture(0, PreviewManager.WHITE_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            RenderSystem.enableCull();
        }
    }
    
    protected void endPreviewRender() {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
    }
    
    public void wrapPreviewRender(ILittleLevelContext context, BlockPos pos, Vec3 cam, boolean lines, Runnable run) {
        startPreviewRender(lines);
        
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        context.transformMatrix(matrix, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
        RenderSystem.applyModelViewMatrix();
        
        run.run();
        
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
        
        endPreviewRender();
    }
    
    public void wrapPreviewRender(boolean lines, Runnable run) {
        startPreviewRender(lines);
        run.run();
        endPreviewRender();
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
            box.buildLines(pose, builder, colorAlpha, box.getCenter(), 0.001, forceWhite);
        else
            box.buildBox(pose, builder, colorAlpha);
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
    
    public void drawLines(MeshData data, boolean highlight) {
        drawLines(data, LittleTiles.CONFIG.rendering.preview.lineColor.getAlphaDecimal(), highlight);
    }
    
    public void drawLines(MeshData data, float alpha, boolean highlight) {
        RenderSystem.enableDepthTest();
        
        var config = LittleTiles.CONFIG.rendering.preview;
        RenderSystem.lineWidth((float) config.lineThickness);
        RenderSystem.setShaderColor(config.lineColor.getRedDecimal(), config.lineColor.getGreenDecimal(), config.lineColor.getBlueDecimal(), alpha);
        
        boolean forceClose = false;
        if (highlight && data instanceof MeshDataExtender e && !e.isKeepAlive()) {
            e.keepAlive(true);
            forceClose = true;
        }
        
        BufferUploader.drawWithShader(data);
        
        if (highlight) {
            RenderSystem.disableDepthTest();
            RenderSystem.lineWidth((float) config.hightlightLineThickness);
            RenderSystem.setShaderColor(config.highlightLineColor.getRedDecimal(), config.highlightLineColor.getGreenDecimal(), config.highlightLineColor.getBlueDecimal(), alpha);
            
            BufferUploader.drawWithShader(data);
            
            RenderSystem.enableDepthTest();
        }
        
        if (forceClose && data instanceof MeshDataExtender e) {
            e.keepAlive(false);
            data.close();
        }
    }
    
    public void drawABB(PoseStack pose, ABB box, boolean highlight) {
        box.inflate(0.002);
        
        BufferBuilder bufferbuilder = createTesselatorBuilder(true);
        box.renderLines(pose, bufferbuilder, 1, 1, 1, 1F);
        
        drawLines(bufferbuilder.buildOrThrow(), highlight);
    }
    
    public void drawLine(PoseStack pose, Vec3 start, Vec3 end, boolean selected) {
        Vec3 dir = end.subtract(start).normalize();
        
        var last = pose.last();
        BufferBuilder bufferbuilder = createTesselatorBuilder(true);
        bufferbuilder.addVertex(last, (float) start.x, (float) start.y, (float) start.z).setColor(-1).setNormal(last, (float) dir.x, (float) dir.y, (float) dir.z);
        bufferbuilder.addVertex(last, (float) end.x, (float) end.y, (float) end.z).setColor(-1).setNormal(last, (float) dir.x, (float) dir.y, (float) dir.z);
        drawLines(bufferbuilder.buildOrThrow(), selected);
    }
    
    public void renderLineBox(PoseStack pose, Vec3 cam, LittleBoxLevel box) {
        wrapPreviewRender(true, () -> {
            pose.pushPose();
            var pos = box.pos;
            box.context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            drawABB(pose, box.toABB(), false);
            pose.popPose();
        });
    }
    
    public void renderLineBox(ILittleLevelContext context, PoseStack pose, Vec3 cam, ABB bb, BlockPos pos, boolean highlight) {
        wrapPreviewRender(true, () -> {
            pose.pushPose();
            context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            drawABB(pose, bb, false);
            pose.popPose();
        });
    }
    
    public void renderLineBoxes(PoseStack pose, Vec3 cam, List<LittleBoxLevel> boxes, @Nullable Int2BooleanFunction marked) {
        wrapPreviewRender(true, () -> {
            for (int i = 0; i < boxes.size(); i++) {
                pose.pushPose();
                var box = boxes.get(i);
                var pos = box.pos;
                box.context.transformPose(pose, pos.getX(), pos.getY(), pos.getZ(), cam, partialTickTime());
                pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
                var buffer = createTesselatorBuilder(true);
                buildBox(pose, box.getRenderingBox(), buffer, 255, true);
                drawLines(buffer.buildOrThrow(), marked != null && marked.get(i));
                pose.popPose();
            }
        });
    }
    
    public void renderLineBoxes(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<LittleBoxAbsolute> boxes, @Nullable Int2BooleanFunction marked) {
        wrapPreviewRender(context, pos, cam, true, () -> {
            pose.pushPose();
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            for (int i = 0; i < boxes.size(); i++)
                drawABB(pose, boxes.get(i).toABB(), marked != null && marked.get(i));
            pose.popPose();
        });
    }
    
    public void renderLinesHighlight(ILittleLevelContext context, Vec3 cam, BlockPos pos, MeshData data, int color) {
        wrapPreviewRender(context, pos, cam, true, () -> drawLines(data, true));
    }
    
    public void renderLinesHighlight(ILittleLevelContext context, Vec3 cam, BlockPos pos, MeshData data, int color, float alpha) {
        wrapPreviewRender(context, pos, cam, true, () -> drawLines(data, alpha, true));
    }
    
    public void renderPositions(PoseStack pose, ILittleLevelContext context, BlockPos pos, Vec3 cam, List<ShapePosition> positions, @Nullable Int2BooleanFunction marked) {
        wrapPreviewRender(context, pos, cam, true, () -> {
            for (int i = 0; i < positions.size(); i++) {
                pose.pushPose();
                pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
                
                drawABB(pose, positions.get(i).getBB(), marked != null && marked.get(i));
                pose.popPose();
            }
        });
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
