package team.creative.littletiles.client.level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.shaders.Uniform;
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
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.entity.LevelTransitionListener;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.mod.rubidium.RubidiumManager;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.client.render.level.RenderUploader;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.mixin.client.render.GameRendererAccessor;
import team.creative.littletiles.mixin.common.entity.EntityAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationHandlerClient extends LittleAnimationHandler implements Iterable<LittleEntity> {
    
    private static Minecraft mc = Minecraft.getInstance();
    private static final int LONG_TICK_INTERVAL = 40;
    public static final int MAX_INTERVALS_WAITING = 2;
    
    private final HashMap<UUID, EntityTransitionHolder> transitions = new HashMap<>();
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
    private int longTickCounter = LONG_TICK_INTERVAL;
    public int longTickIndex = Integer.MIN_VALUE;
    
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
    
    public boolean checkInTransition(Entity entity) {
        if (transitions.containsKey(entity.getUUID())) {
            ((EntityAccessor) entity).callUnsetRemoved();
            return true;
        }
        return false;
    }
    
    public Entity pollEntityInTransition(ClientboundAddEntityPacket packet) {
        EntityTransitionHolder holder = transitions.get(packet.getUUID());
        if (holder == null)
            return null;
        
        Entity entity = holder.entity;
        Level oldLevel = entity.level();
        if (entity instanceof LevelTransitionListener listener)
            listener.prepareChangeLevel(oldLevel, holder.newLevel);
        
        ((EntityAccessor) entity).callSetLevel(holder.newLevel);
        transitions.remove(packet.getUUID());
        if (entity instanceof LevelTransitionListener listener)
            listener.changedLevel(oldLevel, holder.newLevel);
        return entity;
    }
    
    public void queueEntityForTransition(Entity entity, Level newLevel) {
        transitions.put(entity.getUUID(), new EntityTransitionHolder(entity, newLevel, longTickIndex + MAX_INTERVALS_WAITING));
    }
    
    private void runTask() {
        if (!this.freeBuffers.isEmpty()) {
            LittleRenderChunk.ChunkCompileTask task = this.pollTask();
            if (task != null) {
                ChunkBufferBuilderPack pack = this.freeBuffers.poll();
                this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                this.freeBufferCount = this.freeBuffers.size();
                CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(task.name(), () -> task.doTask(pack)), this.executor).thenCompose(x -> x).whenComplete(
                    (result, throwable) -> {
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
                animation.getRenderManager().setupRender(camera, frustum, capturedFrustum, spectator);
        mc.getProfiler().pop();
    }
    
    protected void longTick() {
        if (!transitions.isEmpty())
            for (Iterator<EntityTransitionHolder> iterator = transitions.values().iterator(); iterator.hasNext();) {
                EntityTransitionHolder holder = iterator.next();
                if (longTickIndex >= holder.index)
                    iterator.remove();
            }
        RenderUploader.longTick(longTickIndex);
    }
    
    @Override
    public void tick(LevelTickEvent event) {
        super.tick(event);
        
        if (event.phase == Phase.END) {
            longTickCounter--;
            if (longTickCounter <= 0) {
                longTickCounter = LONG_TICK_INTERVAL;
                longTick();
                longTickIndex++;
            }
        }
        
    }
    
    public void compileChunks(Camera camera) {
        mc.getProfiler().push("compile_animation_chunks");
        
        Runnable run;
        while ((run = this.toUpload.poll()) != null)
            run.run();
        
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                animation.getRenderManager().compileChunks(camera);
            
        mc.getProfiler().pop();
    }
    
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                animation.getRenderManager().resortTransparency(layer, x, y, z);
    }
    
    public void renderBlockEntitiesAndDestruction(PoseStack pose, Frustum frustum, float frameTime) {
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        for (LittleEntity animation : this)
            animation.getRenderManager().renderBlockEntitiesAndDestruction(pose, frustum, cam, frameTime, bufferSource);
        
        for (LittleEntity animation : entities)
            if (animation.hasLoaded())
                animation.getRenderManager().renderGlobalEntities(pose, frustum, cam, frameTime, bufferSource);
    }
    
    @SubscribeEvent
    public void renderChunkLayer(RenderLevelStageEvent event) {
        RenderType layer = null;
        
        if (event.getStage() == Stage.AFTER_SOLID_BLOCKS)
            layer = RenderType.solid();
        else if (event.getStage() == Stage.AFTER_CUTOUT_BLOCKS)
            layer = RenderType.cutout();
        else if (event.getStage() == Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)
            layer = RenderType.cutoutMipped();
        else if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS)
            layer = RenderType.translucent();
        else if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS)
            layer = RenderType.tripwire();
        
        if (layer == null)
            return;
        
        if (RubidiumManager.installed())
            layer.setupRenderState();
        
        PoseStack pose = event.getPoseStack();
        Matrix4f projectionMatrix = event.getProjectionMatrix();
        
        ShaderInstance shaderinstance = RenderSystem.getShader();
        
        for (int i = 0; i < 12; ++i) {
            int j1 = RenderSystem.getShaderTexture(i);
            shaderinstance.setSampler("Sampler" + i, j1);
        }
        
        if (shaderinstance.MODEL_VIEW_MATRIX != null)
            shaderinstance.MODEL_VIEW_MATRIX.set(pose.last().pose());
        
        if (shaderinstance.PROJECTION_MATRIX != null)
            shaderinstance.PROJECTION_MATRIX.set(projectionMatrix);
        
        if (shaderinstance.COLOR_MODULATOR != null)
            shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        
        if (shaderinstance.GLINT_ALPHA != null)
            shaderinstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        
        if (shaderinstance.FOG_START != null)
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        
        if (shaderinstance.FOG_END != null)
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        
        if (shaderinstance.FOG_COLOR != null)
            shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        
        if (shaderinstance.FOG_SHAPE != null)
            shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        
        if (shaderinstance.TEXTURE_MATRIX != null)
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        
        if (shaderinstance.GAME_TIME != null)
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        
        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        Uniform offset = RenderSystem.getShader().CHUNK_OFFSET;
        float partialTicks = mc.getPartialTick();
        for (LittleEntity animation : this) {
            pose.pushPose();
            animation.getOrigin().setupRendering(pose, cam.x, cam.y, cam.z, partialTicks);
            if (shaderinstance.MODEL_VIEW_MATRIX != null)
                shaderinstance.MODEL_VIEW_MATRIX.set(pose.last().pose());
            shaderinstance.apply();
            animation.getRenderManager().renderChunkLayer(layer, pose, cam.x, cam.y, cam.z, projectionMatrix, offset);
            pose.popPose();
        }
        
        if (RubidiumManager.installed())
            layer.clearRenderState();
        shaderinstance.clear();
    }
    
    @SubscribeEvent
    public void renderEnd(RenderTickEvent event) {
        if (event.phase == Phase.END)
            for (LittleEntity animation : entities)
                if (animation.hasLoaded())
                    animation.getRenderManager().isInSight = null;
    }
    
    @Override
    public void unload() {
        super.unload();
        LittleTilesClient.ANIMATION_HANDLER = null;
        transitions.clear();
        RenderUploader.unload();
    }
    
    @Override
    protected void tickEntity(LittleEntity entity) {
        if (!entity.hasLoaded())
            return;
        entity.getRenderManager().clientTick();
        super.tickEntity(entity);
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
                    flag = !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(registry, blockinworld) || itemstack.hasAdventureModePlaceTagForBlock(registry,
                        blockinworld));
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
        
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        RenderSystem.applyModelViewMatrix();
        BlockPos pos = result.asBlockHit().getBlockPos();
        BlockState state = result.level.getBlockState(pos);
        VertexConsumer vertexconsumer2 = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LittleEntity entity = result.getHolder();
        Vec3 position = mc.gameRenderer.getMainCamera().getPosition();
        entity.getOrigin().setupRendering(event.getPoseStack(), position.x, position.y, position.z, event.getPartialTick());
        RenderSystem.enableDepthTest();
        
        double x = pos.getX() - position.x();
        double y = pos.getY() - position.y();
        double z = pos.getZ() - position.z();
        
        if (!state.isAir() && this.level.getWorldBorder().isWithinBounds(pos)) {
            PoseStack.Pose posestack$pose = event.getPoseStack().last();
            VoxelShape shape;
            if (state.getBlock() instanceof BlockTile block)
                shape = block.getSelectionShape(result.level, pos);
            else
                shape = state.getShape(result.level, pos, CollisionContext.of(mc.cameraEntity));
            shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                float f = (float) (x2 - x1);
                float f1 = (float) (y2 - y1);
                float f2 = (float) (z2 - z1);
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                vertexconsumer2.vertex(posestack$pose.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).color(0.0F, 0.0F, 0.0F, 0.4F).normal(posestack$pose.normal(), f,
                    f1, f2).endVertex();
                vertexconsumer2.vertex(posestack$pose.pose(), (float) (x2 + x), (float) (y2 + y), (float) (z2 + z)).color(0.0F, 0.0F, 0.0F, 0.4F).normal(posestack$pose.normal(), f,
                    f1, f2).endVertex();
            });
        }
        
        pose.popPose();
    }
    
    public static record EntityTransitionHolder(Entity entity, Level newLevel, int index) {}
}
