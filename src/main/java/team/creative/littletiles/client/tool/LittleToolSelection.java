package team.creative.littletiles.client.tool;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleSelector;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.packet.item.SelectionModePacket;

public class LittleToolSelection extends LittleTool {
    
    private final ILittleSelector selector;
    private SelectionComponent cachedSelection;
    private BlockPos cacheOrigin;
    private SelectionRenderResult lineCache;
    private SelectionRenderResult boxCache;
    
    public LittleToolSelection(ItemStack stack) {
        super(stack);
        this.selector = (ILittleSelector) stack.getItem();
    }
    
    private void build(PreviewRenderer renderer) {
        if (!selector.hasSelection(stack))
            return;
        
        var selection = selector.getSelection(stack);
        if (selection.equals(cachedSelection))
            return;
        
        cachedSelection = selection;
        SelectionRenderQueue queue = new SelectionRenderQueue(renderer);
        cachedSelection.mode.buildRender(renderer.level(), stack, selector.getSelection(stack), queue);
        cacheOrigin = queue.origin;
        lineCache = queue.build(true);
        boxCache = queue.build(false);
    }
    
    @Override
    public boolean onRightClick(PreviewRenderer renderer, BlockHitResult result) {
        if (result == null)
            return false;
        
        var packet = new SelectionModePacket(result, renderer.selectFocused(result), renderer.isUsingSecondMode(), true);
        packet.execute(renderer.player());
        LittleTiles.NETWORK.sendToServer(packet);
        return true;
    }
    
    @Override
    public boolean onLeftClick(PreviewRenderer renderer, BlockHitResult result) {
        if (result == null)
            return false;
        
        var packet = new SelectionModePacket(result, renderer.selectFocused(result), renderer.isUsingSecondMode(), false);
        packet.execute(renderer.player());
        LittleTiles.NETWORK.sendToServer(packet);
        return true;
    }
    
    @Override
    protected void tickInternal(PreviewRenderer renderer) {
        if (cachedSelection == null)
            return;
        
        cachedSelection.mode.tick((LittleActionSource) renderer.player(), renderer.level(), stack, cachedSelection, this);
    }
    
    public SelectionRenderResult getRenderResult(boolean lines) {
        return lines ? lineCache : boxCache;
    }
    
    @Override
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (!selector.hasSelection(stack))
            return;
        var sel = selector.getSelection(stack);
        if (cachedSelection == null || !cachedSelection.equals(sel))
            build(renderer);
        
        if (cachedSelection == null)
            return;
        
        var result = getRenderResult(lines);
        if (result == null || result.data == null)
            return;
        
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        renderer.setupPreviewRenderer(lines);
        matrix.translate((float) (cacheOrigin.getX() - cam.x), (float) (cacheOrigin.getY() - cam.y), (float) (cacheOrigin.getZ() - cam.z));
        RenderSystem.applyModelViewMatrix();
        BufferUploader.drawWithShader(result.data);
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
        
        if (cachedSelection.mode.hasRenderTick(stack, cachedSelection)) {
            pose.pushPose();
            pose.translate(-cam.x, -cam.y, -cam.z);
            cachedSelection.mode.renderTick((LittleActionSource) renderer.player(), renderer.level(), stack, cachedSelection, pose, lines);
            pose.popPose();
        }
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
    
    @Override
    public void remove() {
        super.remove();
        if (lineCache != null)
            lineCache.close();
        lineCache = null;
        if (boxCache != null)
            boxCache.close();
        boxCache = null;
        
        cachedSelection = null;
        cacheOrigin = null;
    }
    
    public class SelectionRenderQueue {
        
        private ByteBufferBuilder lineBuffer;
        private BufferBuilder lineBuilder;
        private ByteBufferBuilder boxBuffer;
        private BufferBuilder boxBuilder;
        
        private BlockPos origin = BlockPos.ZERO;
        
        public final PreviewRenderer renderer;
        
        public SelectionRenderQueue(PreviewRenderer renderer) {
            this.renderer = renderer;
        }
        
        protected void checkOrigin() {
            if (origin == null)
                throw new RuntimeException("First origin needs to be set");
        }
        
        public void setOrigin(BlockPos pos) {
            origin = pos;
        }
        
        public BufferBuilder getBuilder(boolean line) {
            if (line) {
                if (lineBuffer == null) {
                    lineBuffer = renderer.createBuffer();
                    lineBuilder = renderer.createBuilder(lineBuffer, line);
                }
                return lineBuilder;
            }
            
            if (boxBuffer == null) {
                boxBuffer = renderer.createBuffer();
                boxBuilder = renderer.createBuilder(boxBuffer, line);
            }
            return boxBuilder;
        }
        
        public void addBox(RenderBox box, boolean line) {
            addBox(box, line, 255);
        }
        
        public void addBox(RenderBox box, boolean line, int alpha) {
            checkOrigin();
            renderer.buildBox(PreviewRenderer.EMPTY, box, getBuilder(line), alpha, line);
        }
        
        public void addLine(Vec3 start, Vec3 end, int color) {
            checkOrigin();
            int red = ColorUtils.red(color);
            int green = ColorUtils.green(color);
            int blue = ColorUtils.blue(color);
            int alpha = ColorUtils.alpha(color);
            Vec3f normal = new Vec3f();
            var builder = getBuilder(true);
            VectorFan.setLineNormal(normal, (float) start.x, (float) start.y, (float) start.z, (float) end.x, (float) end.y, (float) end.z);
            builder.addVertex(PreviewRenderer.EMPTY.last().pose(), (float) start.x, (float) start.y, (float) start.z).setColor(red, green, blue, alpha).setNormal(
                PreviewRenderer.EMPTY.last(), normal.x, normal.y, normal.z);
            builder.addVertex(PreviewRenderer.EMPTY.last().pose(), (float) end.x, (float) end.y, (float) end.z).setColor(red, green, blue, alpha).setNormal(PreviewRenderer.EMPTY
                    .last(), normal.x, normal.y, normal.z);
        }
        
        public SelectionRenderResult build(boolean lines) {
            if (lines) {
                if (lineBuffer == null)
                    return null;
                var data = lineBuilder.build();
                ((MeshDataExtender) data).keepAlive(true);
                return new SelectionRenderResult(lineBuffer, data);
            }
            if (boxBuffer == null)
                return null;
            var data = boxBuilder.build();
            ((MeshDataExtender) data).keepAlive(true);
            return new SelectionRenderResult(boxBuffer, data);
        }
        
    }
    
    public static record SelectionRenderResult(ByteBufferBuilder buffer, MeshData data) {
        
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
