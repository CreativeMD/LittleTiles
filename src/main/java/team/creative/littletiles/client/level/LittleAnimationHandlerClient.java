package team.creative.littletiles.client.level;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.client.render.entity.LittleLevelEntityRenderer;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.common.entity.level.LittleEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.mixin.client.render.GameRendererAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationHandlerClient extends LittleAnimationHandler implements Iterable<LittleEntity> {
    
    private static Minecraft mc = Minecraft.getInstance();
    
    private final PriorityBlockingQueue<LittleRenderChunk.ChunkCompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
    private final Queue<LittleRenderChunk.ChunkCompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
    private int highPriorityQuota = 2;
    private final Queue<ChunkBufferBuilderPack> freeBuffers;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    private volatile int toBatchCount;
    private volatile int freeBufferCount;
    public final ChunkBufferBuilderPack fixedBuffers;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    
    public LittleAnimationHandlerClient(Level level) {
        super(level);
        int threadCount = LittleTiles.CONFIG.rendering.entityCacheBuildThreads;
        this.fixedBuffers = mc.renderBuffers().fixedBufferPack();
        List<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(threadCount);
        
        try {
            for (int i = 0; i < threadCount; i++)
                list.add(new ChunkBufferBuilderPack());
        } catch (OutOfMemoryError error) {
            LittleTiles.LOGGER.warn("Allocated only {}/{} buffers", list.size(), threadCount);
            int newSize = Math.min(list.size() * 2 / 3, list.size() - 1);
            for (int i = 0; i < newSize; i++)
                list.remove(list.size() - 1);
            
            System.gc();
        }
        
        this.freeBuffers = Queues.newArrayDeque(list);
        this.freeBufferCount = this.freeBuffers.size();
        this.executor = Util.backgroundExecutor();
        this.mailbox = ProcessorMailbox.create(executor, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }
    
    private void runTask() {
        if (!this.freeBuffers.isEmpty()) {
            LittleRenderChunk.ChunkCompileTask task = this.pollTask();
            if (task != null) {
                ChunkBufferBuilderPack pack = this.freeBuffers.poll();
                this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                this.freeBufferCount = this.freeBuffers.size();
                CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(task.name(), () -> task.doTask(pack)), this.executor).thenCompose(x -> x)
                        .whenComplete((result, throwable) -> {
                            if (throwable != null)
                                Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Batching chunks"));
                            else
                                this.mailbox.tell(() -> {
                                    if (result == LittleRenderChunk.ChunkTaskResult.SUCCESSFUL)
                                        pack.clearAll();
                                    else
                                        pack.discardAll();
                                    
                                    this.freeBuffers.add(pack);
                                    this.freeBufferCount = this.freeBuffers.size();
                                    this.runTask();
                                });
                        });
            }
        }
    }
    
    @Nullable
    private LittleRenderChunk.ChunkCompileTask pollTask() {
        if (this.highPriorityQuota <= 0) {
            LittleRenderChunk.ChunkCompileTask task = this.toBatchLowPriority.poll();
            if (task != null) {
                this.highPriorityQuota = 2;
                return task;
            }
        }
        
        LittleRenderChunk.ChunkCompileTask task2 = this.toBatchHighPriority.poll();
        if (task2 != null) {
            --this.highPriorityQuota;
            return task2;
        }
        
        this.highPriorityQuota = 2;
        return this.toBatchLowPriority.poll();
    }
    
    public String getStats() {
        return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
    }
    
    public int getToBatchCount() {
        return this.toBatchCount;
    }
    
    public int getToUpload() {
        return this.toUpload.size();
    }
    
    public int getFreeBufferCount() {
        return this.freeBufferCount;
    }
    
    public void uploadAllPendingUploads() {
        Runnable runnable;
        while ((runnable = this.toUpload.poll()) != null)
            runnable.run();
    }
    
    public void blockUntilClear() {
        this.clearBatchQueue();
    }
    
    public void schedule(LittleRenderChunk.ChunkCompileTask task) {
        this.mailbox.tell(() -> {
            if (task.isHighPriority)
                this.toBatchHighPriority.offer(task);
            else
                this.toBatchLowPriority.offer(task);
            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.runTask();
        });
    }
    
    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer rendered, VertexBuffer buffer) {
        return CompletableFuture.runAsync(() -> {
            if (!buffer.isInvalid()) {
                buffer.bind();
                buffer.upload(rendered);
                VertexBuffer.unbind();
            }
        }, this.toUpload::add);
    }
    
    private void clearBatchQueue() {
        while (!this.toBatchHighPriority.isEmpty()) {
            LittleRenderChunk.ChunkCompileTask task = this.toBatchHighPriority.poll();
            if (task != null)
                task.cancel();
        }
        
        while (!this.toBatchLowPriority.isEmpty()) {
            LittleRenderChunk.ChunkCompileTask task1 = this.toBatchLowPriority.poll();
            if (task1 != null)
                task1.cancel();
        }
        
        this.toBatchCount = 0;
    }
    
    public boolean isQueueEmpty() {
        return this.toBatchCount == 0 && this.toUpload.isEmpty();
    }
    
    public void dispose() {
        this.clearBatchQueue();
        this.mailbox.close();
        this.freeBuffers.clear();
    }
    
    public void allChanged() {
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                animation.getRenderManager().allChanged();
            
        synchronized (this.globalBlockEntities) {
            this.globalBlockEntities.clear();
        }
    }
    
    public void updateGlobalBlockEntities(Collection<BlockEntity> oldBlockEntities, Collection<BlockEntity> newBlockEntities) {
        synchronized (this.globalBlockEntities) {
            this.globalBlockEntities.removeAll(oldBlockEntities);
            this.globalBlockEntities.addAll(newBlockEntities);
        }
    }
    
    @Override
    public synchronized Iterator<LittleEntity> iterator() {
        return new FilterIterator<>(entities, x -> x.hasLoaded() && BooleanUtils.isTrue(x.getRenderManager().isInSight));
    }
    
    public void needsUpdate() {
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                animation.getRenderManager().needsFullRenderChunkUpdate = true;
    }
    
    public void setupRender(Camera camera, Frustum frustum, boolean capturedFrustum, boolean spectator) {
        mc.getProfiler().push("setup_animation_render");
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                animation.getRenderManager().setupRender(animation, new Vec3d(camera.getPosition()), frustum, capturedFrustum, spectator);
        mc.getProfiler().pop();
    }
    
    public void compileChunks(Camera camera) {
        mc.getProfiler().push("compile_animation_chunks");
        
        Runnable run;
        while ((run = this.toUpload.poll()) != null)
            run.run();
        
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                LittleLevelEntityRenderer.INSTANCE.compileChunks(animation);
            
        mc.getProfiler().pop();
    }
    
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                LittleLevelEntityRenderer.INSTANCE.resortTransparency(animation, layer, x, y, z);
    }
    
    public void renderBlockEntitiesAndDestruction(PoseStack pose, Frustum frustum, float frameTime) {
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        for (LittleEntity animation : this)
            LittleLevelEntityRenderer.INSTANCE.renderBlockEntitiesAndDestruction(pose, animation, frustum, cam, frameTime, bufferSource);
        
        synchronized (this.globalBlockEntities) {
            for (BlockEntity blockentity : this.globalBlockEntities) {
                if (!frustum.isVisible(blockentity.getRenderBoundingBox()))
                    continue;
                BlockPos blockpos3 = blockentity.getBlockPos();
                pose.pushPose();
                
                LittleClientLevel level = (LittleClientLevel) blockentity.getLevel();
                level.getOrigin().setupRendering(pose, level.getHolder(), frameTime);
                
                pose.translate(blockpos3.getX() - cam.x, blockpos3.getY() - cam.y, blockpos3.getZ() - cam.z);
                
                mc.getBlockEntityRenderDispatcher()
                        .render(blockentity, frameTime, pose, LittleLevelEntityRenderer.INSTANCE.prepareBlockEntity(pose, level, blockpos3, bufferSource));
                pose.popPose();
            }
        }
    }
    
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix) {
        for (LittleEntity animation : this)
            LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(animation, layer, pose, x, y, z, projectionMatrix);
    }
    
    @SubscribeEvent
    public void renderEnd(RenderTickEvent event) {
        if (event.phase == Phase.END)
            for (LittleEntity animation : entities)
                if (animation.hasLoaded())
                    animation.getRenderManager().isInSight = null;
    }
    
    @SubscribeEvent
    public void rightClick(PlayerInteractEvent event) {
        if (event instanceof RightClickBlock || event instanceof RightClickEmpty || event instanceof RightClickItem || event instanceof EntityInteractSpecific) {
            
            if (mc.hitResult instanceof LittleHitResult result && result.level instanceof ISubLevel) {
                Entity entity = ((ISubLevel) result.level).getHolder();
                if (entity instanceof LittleEntity levelEntity)
                    levelEntity.onRightClick(event.getEntity(), result.hit);
            }
        }
    }
    
    @Override
    public void unload() {
        globalBlockEntities.clear();
    }
    
    @SubscribeEvent
    public void tickClient(ClientTickEvent event) {
        if (event.phase == Phase.END && (!mc.hasSingleplayerServer() || !mc.isPaused())) {
            tick();
            
            for (LittleEntity entity : entities) {
                entity.getRenderManager().clientTick();
                if (entity.level instanceof ISubLevel || !entity.hasLoaded())
                    continue;
                entity.performTick();
            }
        }
    }
    
    private boolean shouldRenderBlockOutline() {
        if (!((GameRendererAccessor) mc.gameRenderer).getRenderBlockOutline() || !(mc.hitResult instanceof LittleHitResult))
            return false;
        
        LittleHitResult result = (LittleHitResult) mc.hitResult;
        Entity entity = mc.getCameraEntity();
        boolean flag = entity instanceof Player && !mc.options.hideGui;
        if (flag && !((Player) entity).getAbilities().mayBuild) {
            ItemStack itemstack = ((LivingEntity) entity).getMainHandItem();
            if (result.isBlock()) {
                BlockPos blockpos = result.asBlockHit().getBlockPos();
                BlockState blockstate = result.level.getBlockState(blockpos);
                if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
                    flag = blockstate.getMenuProvider((Level) result.level, blockpos) != null;
                else {
                    BlockInWorld blockinworld = new BlockInWorld(result.level, blockpos, false);
                    Registry<Block> registry = result.level.registryAccess().registryOrThrow(Registries.BLOCK);
                    flag = !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(registry, blockinworld) || itemstack
                            .hasAdventureModePlaceTagForBlock(registry, blockinworld));
                }
            }
        }
        
        return flag;
    }
    
    @SubscribeEvent
    public void tick(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_SKY)
            return;
        
        if (!shouldRenderBlockOutline())
            return;
        
        LittleHitResult result = (LittleHitResult) mc.hitResult;
        
        event.setCanceled(true);
        
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        RenderSystem.applyModelViewMatrix();
        BlockPos pos = result.asBlockHit().getBlockPos();
        BlockState state = result.level.getBlockState(pos);
        VertexConsumer vertexconsumer2 = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LittleEntity entity = result.getHolder();
        entity.getOrigin().setupRendering(event.getPoseStack(), entity, event.getPartialTick());
        RenderSystem.enableDepthTest();
        
        Vec3 position = mc.gameRenderer.getMainCamera().getPosition();
        double d0 = pos.getX() - position.x();
        double d1 = pos.getY() - position.y();
        double d2 = pos.getZ() - position.z();
        
        if (!state.isAir() && this.level.getWorldBorder().isWithinBounds(pos)) {
            PoseStack.Pose posestack$pose = event.getPoseStack().last();
            state.getShape(result.level, pos, CollisionContext.of(mc.cameraEntity)).forAllEdges((p_194324_, p_194325_, p_194326_, p_194327_, p_194328_, p_194329_) -> {
                float f = (float) (p_194327_ - p_194324_);
                float f1 = (float) (p_194328_ - p_194325_);
                float f2 = (float) (p_194329_ - p_194326_);
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                vertexconsumer2.vertex(posestack$pose.pose(), (float) (p_194324_ + d0), (float) (p_194325_ + d1), (float) (p_194326_ + d2)).color(0.0F, 0.0F, 0.0F, 0.4F)
                        .normal(posestack$pose.normal(), f, f1, f2).endVertex();
                vertexconsumer2.vertex(posestack$pose.pose(), (float) (p_194327_ + d0), (float) (p_194328_ + d1), (float) (p_194329_ + d2)).color(0.0F, 0.0F, 0.0F, 0.4F)
                        .normal(posestack$pose.normal(), f, f1, f2).endVertex();
            });
        }
        
        pose.popPose();
    }
}
