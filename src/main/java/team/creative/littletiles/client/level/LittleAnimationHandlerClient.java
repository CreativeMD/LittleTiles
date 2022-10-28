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

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
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
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
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
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.event.InputEventHandler;
import team.creative.littletiles.client.event.WheelClick;
import team.creative.littletiles.client.render.entity.LittleLevelEntityRenderer;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.event.GetVoxelShapesEvent;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;
import team.creative.littletiles.common.math.vec.LittleHitResult;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationHandlerClient extends LittleAnimationHandler implements Iterable<LittleLevelEntity> {
    
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
        for (LittleLevelEntity animation : entities)
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
    public synchronized Iterator<LittleLevelEntity> iterator() {
        return new FilterIterator<>(entities, x -> x.getRenderManager().isInSight);
    }
    
    public void needsUpdate() {
        for (LittleLevelEntity animation : entities)
            animation.getRenderManager().needsFullRenderChunkUpdate = true;
    }
    
    public void setupRender(Camera camera, Frustum frustum, boolean capturedFrustum, boolean spectator) {
        mc.getProfiler().push("setup_animation_render");
        for (LittleLevelEntity animation : entities)
            animation.getRenderManager().setupRender(animation, camera, frustum, capturedFrustum, spectator);
        mc.getProfiler().pop();
    }
    
    public void compileChunks(Camera camera) {
        mc.getProfiler().push("compile_animation_chunks");
        
        Runnable run;
        while ((run = this.toUpload.poll()) != null)
            run.run();
        
        for (LittleLevelEntity animation : entities)
            LittleLevelEntityRenderer.INSTANCE.compileChunks(animation, camera);
        
        mc.getProfiler().pop();
    }
    
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        for (LittleLevelEntity animation : entities)
            LittleLevelEntityRenderer.INSTANCE.resortTransparency(animation, layer, x, y, z);
    }
    
    public void renderBlockEntities(PoseStack pose, Frustum frustum, float frameTime) {
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        for (LittleLevelEntity animation : this)
            LittleLevelEntityRenderer.INSTANCE.renderBlockEntities(pose, animation, frustum, cam, frameTime, bufferSource);
        
        synchronized (this.globalBlockEntities) {
            for (BlockEntity blockentity : this.globalBlockEntities) {
                if (!frustum.isVisible(blockentity.getRenderBoundingBox()))
                    continue;
                BlockPos blockpos3 = blockentity.getBlockPos();
                pose.pushPose();
                pose.translate(blockpos3.getX() - cam.x, blockpos3.getY() - cam.y, blockpos3.getZ() - cam.z);
                mc.getBlockEntityRenderDispatcher().render(blockentity, frameTime, pose, bufferSource);
                pose.popPose();
            }
        }
    }
    
    @SubscribeEvent
    public void renderChunkLayer(RenderLevelStageEvent event) {
        if (event.getStage() == Stage.AFTER_SOLID_BLOCKS || event.getStage() == Stage.AFTER_CUTOUT_BLOCKS || event.getStage() == Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS || event
                .getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS)
            for (LittleLevelEntity animation : this)
                LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(animation, event);
    }
    
    @SubscribeEvent
    public void renderEnd(RenderTickEvent event) {
        if (event.phase == Phase.END)
            for (LittleLevelEntity animation : entities)
                animation.getRenderManager().isInSight = null;
    }
    
    @SubscribeEvent
    public void rightClick(PlayerInteractEvent event) {
        if (event instanceof RightClickBlock || event instanceof RightClickEmpty || event instanceof RightClickItem || event instanceof EntityInteractSpecific) {
            
            LittleHitResult result = getHit();
            if (result != null && result.level instanceof ISubLevel) {
                Entity entity = ((ISubLevel) result.level).getHolder();
                if (entity instanceof LittleLevelEntity levelEntity)
                    levelEntity.onRightClick(event.getEntity(), result.hit);
            }
        }
    }
    
    @SubscribeEvent
    public void mouseWheel(WheelClick event) {
        LittleHitResult target = getHit();
        if (target == null || !target.isBlock())
            return;
        
        Player player = event.player;
        
        if (InputEventHandler.onPickBlock(target.asBlockHit(), player, target.level)) {
            event.setCanceled(true);
            return;
        }
        
        BlockState state = target.level.getBlockState(target.asBlockHit().getBlockPos());
        
        if (state.isAir())
            return;
        
        ItemStack stack = state.getCloneItemStack(target.hit, level, target.asBlockHit().getBlockPos(), player);
        
        if (stack.isEmpty())
            return;
        
        if (event.player.isCreative()) {
            player.getInventory().setPickedItem(stack);
            Minecraft.getInstance().gameMode.handleCreativeModeItemAdd(player.getItemInHand(InteractionHand.MAIN_HAND), 36 + player.getInventory().selected);
            event.setCanceled(true);
        }
        int slot = player.getInventory().findSlotMatchingItem(stack);
        if (slot != -1) {
            if (Inventory.isHotbarSlot(slot))
                player.getInventory().selected = slot;
            else
                Minecraft.getInstance().gameMode.handlePickItem(slot);
            event.setCanceled(true);
        }
    }
    
    /*private static final Method syncCurrentPlayItemMethod = ReflectionHelper.findMethod(PlayerControllerMP.class, "syncCurrentPlayItem", "func_78750_j");
    private static final Field blockHitDelayField = ReflectionHelper.findField(PlayerControllerMP.class, new String[] { "blockHitDelay", "field_78781_i" });
    private static final Field leftClickCounterField = ReflectionHelper.findField(Minecraft.class, new String[] { "leftClickCounter", "field_71429_W" });
    
    public boolean isHittingBlock = false;
    private float curBlockDamageMP;
    public int stepSoundTickCounter;
    public World currentDestroyWorld;
    public BlockPos currentDestroyPos = new BlockPos(-1, -1, -1);
    public ItemStack currentItemHittingBlock = ItemStack.EMPTY;
    
    public boolean isHittingPos(Level level, BlockPos pos) {
        ItemStack itemstack = LittleAnimationHandlerClient.mc.player.getMainHandItem();
        boolean flag = this.currentItemHittingBlock.isEmpty() && itemstack.isEmpty();
        
        if (!this.currentItemHittingBlock.isEmpty() && !itemstack.isEmpty())
            flag = !net.minecraftforge.client.ForgeHooksClient.shouldCauseBlockBreakReset(this.currentItemHittingBlock, itemstack);
        
        return flag && isHittingBlock && currentDestroyPos.equals(pos) && currentDestroyWorld == world;
    }
    
    public void resetBlockRemoving() {
        if (this.isHittingBlock) {
            //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            sendBlockBreakProgress(mc.player.getEntityId(), currentDestroyWorld, currentDestroyPos, -1);
            LittleAnimationHandlerClient.mc.player.resetCooldown();
        }
    }
    
    public boolean clickBlockCreative(Level level, Player player, BlockPos pos, Facing facing) {
        if (!level.extinguishFire(player, pos, facing))
            return onPlayerDestroyBlock(player, level, pos);
        return true;
    }
    
    public void sendBlockBreakProgress(int breakerId, Level level, BlockPos pos, int progress) {
        /*if (progress >= 0 && progress < 10) {
        	DestroyBlockProgress destroyblockprogress = this.damagedBlocks.get(Integer.valueOf(breakerId));
        	
        	if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
        		destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
        		this.damagedBlocks.put(Integer.valueOf(breakerId), destroyblockprogress);
        	}
        	
        	destroyblockprogress.setPartialBlockDamage(progress);
        	destroyblockprogress.setCloudUpdateTick(mc.renderGlobal.cloudTickCounter);
        } else {
        	this.damagedBlocks.remove(Integer.valueOf(breakerId));
    }
     }
     
     public void addBlockHitEffects(LittleHitResult result) {
     BlockState state = level.getBlockState(result.asBlockHit().getBlockPos());
     if (!net.minecraftforge.client.RenderProperties.get(state).addHitEffects(state, level, result.asBlockHit(), mc.particleEngine))
     mc.particleEngine.crack(result.asBlockHit().getBlockPos(), result.asBlockHit().getDirection());
     }
     
     @SubscribeEvent
     public void holdClick(HoldLeftClick event) {
     LittleHitResult result = getHit();
     if (result == null || !event.leftClick) {
     if (isHittingBlock)
     resetBlockRemoving();
     return;
     }
     
     Player player = event.player;
     
     try {
     if (leftClickCounterField.getInt(mc) <= 0 && !player.isUsingItem()) {
     if (onPlayerDamageBlock(player, result, event)) {
        addBlockHitEffects(result);
        player.swing(InteractionHand.MAIN_HAND);
        event.setLeftClickResult(false);
     }
     }
     } catch (IllegalArgumentException | IllegalAccessException e) {
     e.printStackTrace();
     }
     
     }
     
     public boolean onPlayerDamageBlock(Player player, LittleHitResult result, HoldLeftClick event) {
     try {
     syncCurrentPlayItemMethod.invoke(mc.playerController);
     } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
     e.printStackTrace();
     }
     
     try {
     if (blockHitDelayField.getInt(mc.playerController) > 0) {
     blockHitDelayField.setInt(mc.playerController, blockHitDelayField.getInt(mc.playerController) - 1);
     event.setLeftClickResult(false);
     return false;
     }
     } catch (IllegalArgumentException | IllegalAccessException e) {
     e.printStackTrace();
     }
     
     if (mc.playerController.getCurrentGameType().isCreative() && LittleAnimationHandlerClient.mc.world.getWorldBorder()
     .contains(world instanceof CreativeWorld ? ((CreativeWorld) world).transformToRealWorld(pos) : pos)) {
     try {
     blockHitDelayField.setInt(mc.playerController, 5);
     } catch (IllegalArgumentException | IllegalAccessException e) {
     e.printStackTrace();
     }
     //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
     if (clickBlockCreative(world, player, pos, facing))
     return true;
     } else if (isHittingPos(world, pos)) {
     IBlockState iblockstate = world.getBlockState(pos);
     Block block = iblockstate.getBlock();
     
     if (iblockstate.getMaterial() == Material.AIR)
     return false;
     this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(player, world, pos);
     
     if (this.stepSoundTickCounter % 4 == 0) {
     SoundType soundtype = block.getSoundType(iblockstate, world, pos, mc.player);
     LittleAnimationHandlerClient.mc.getSoundHandler()
            .playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype
                    .getPitch() * 0.5F, world instanceof CreativeWorld ? ((CreativeWorld) world).transformToRealWorld(pos) : pos));
     }
     
     ++this.stepSoundTickCounter;
     if (this.curBlockDamageMP >= 1.0F) {
     this.isHittingBlock = false;
     //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
     onPlayerDestroyBlock(player, world, pos);
     this.curBlockDamageMP = 0.0F;
     this.stepSoundTickCounter = 0;
     }
     
     sendBlockBreakProgress(LittleAnimationHandlerClient.mc.player.getEntityId(), world, pos, (int) (this.curBlockDamageMP * 10.0F) - 1);
     return true;
     } else if (this.clickBlock(world, pos, facing))
     return true;
     return false;
     }
     
     public boolean clickBlock(LittleHitResult result) {
     if (mc.playerController.getCurrentGameType().hasLimitedInteractions()) {
     if (mc.playerController.getCurrentGameType() == GameType.SPECTATOR)
     return false;
     
     if (!LittleAnimationHandlerClient.mc.player.isAllowEdit()) {
     ItemStack itemstack = LittleAnimationHandlerClient.mc.player.getHeldItemMainhand();
     
     if (itemstack.isEmpty())
        return false;
     
     if (!itemstack.canDestroy(LittleAnimationHandlerClient.mc.world.getBlockState(loc).getBlock()))
        return false;
     }
     }
     
     if (!LittleAnimationHandlerClient.mc.world.getWorldBorder().contains(world instanceof CreativeWorld ? ((CreativeWorld) world).transformToRealWorld(loc) : loc))
     return false;
     
     if (mc.playerController.getCurrentGameType().isCreative()) {
     //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
     clickBlockCreative(world, mc.player, loc, face);
     try {
     blockHitDelayField.setInt(mc.playerController, 5);
     } catch (IllegalArgumentException | IllegalAccessException e) {
     e.printStackTrace();
     }
     } else if (!this.isHittingBlock || !this.isHittingPos(world, loc)) {
     
     //if (this.isHittingBlock)
     //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
     
     IBlockState iblockstate = LittleAnimationHandlerClient.mc.world.getBlockState(loc);
     //this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
     boolean flag = iblockstate.getMaterial() != Material.AIR;
     
     if (flag && this.curBlockDamageMP == 0.0F)
     iblockstate.getBlock().onBlockClicked(LittleAnimationHandlerClient.mc.world, loc, LittleAnimationHandlerClient.mc.player);
     
     if (flag && iblockstate.getPlayerRelativeBlockHardness(LittleAnimationHandlerClient.mc.player, LittleAnimationHandlerClient.mc.player.world, loc) >= 1.0F)
     this.onPlayerDestroyBlock(mc.player, world, loc);
     else {
     this.isHittingBlock = true;
     this.currentDestroyPos = loc;
     this.currentDestroyWorld = world;
     this.currentItemHittingBlock = LittleAnimationHandlerClient.mc.player.getHeldItemMainhand();
     this.curBlockDamageMP = 0.0F;
     this.stepSoundTickCounter = 0;
     sendBlockBreakProgress(LittleAnimationHandlerClient.mc.player.getEntityId(), currentDestroyWorld, currentDestroyPos, (int) (this.curBlockDamageMP * 10.0F) - 1);
     }
     }
     
     return true;
     }
     
     @SubscribeEvent
     public void leftClick(LeftClick event) {
     LittleHitResult result = getHit();
     if (result == null)
     return;
     
     if (clickBlock(result))
     event.setCanceled(true);
     }
     
     public boolean onPlayerDestroyBlock(Player player, Level world, BlockPos pos) {
     if (mc.playerController.getCurrentGameType().hasLimitedInteractions()) {
     if (mc.playerController.getCurrentGameType() == GameType.SPECTATOR)
     return false;
     
     if (!mc.player.isAllowEdit()) {
     ItemStack itemstack = mc.player.getHeldItemMainhand();
     
     if (itemstack.isEmpty()) {
        return false;
     }
     
     if (!itemstack.canDestroy(world.getBlockState(pos).getBlock())) {
        return false;
     }
     }
     }
     
     ItemStack stack = mc.player.getHeldItemMainhand();
     if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, mc.player))
     return false;
     
     if (mc.playerController.getCurrentGameType().isCreative() && !stack.isEmpty() && !stack.getItem().canDestroyBlockInCreative(world, pos, stack, mc.player))
     return false;
     
     IBlockState iblockstate = world.getBlockState(pos);
     Block block = iblockstate.getBlock();
     
     if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !player.canUseCommandBlock())
     return false;
     
     if (iblockstate.getMaterial() == Material.AIR)
     return false;
     
     world.playEvent(2001, pos, Block.getStateId(iblockstate));
     
     currentDestroyPos = new BlockPos(currentDestroyPos.getX(), -1, currentDestroyPos.getZ());
     
     if (!mc.playerController.getCurrentGameType().isCreative()) {
     ItemStack itemstack1 = player.getHeldItemMainhand();
     ItemStack copyBeforeUse = itemstack1.copy();
     
     if (!itemstack1.isEmpty()) {
     itemstack1.onBlockDestroyed(world, iblockstate, pos, player);
     
     if (itemstack1.isEmpty()) {
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, EnumHand.MAIN_HAND);
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
     }
     }
     }
     
     boolean destroyed = block.removedByPlayer(iblockstate, world, pos, player, false);
     
     if (destroyed) {
     block.onBlockDestroyedByPlayer(world, pos, iblockstate);
     
     try {
     blockHitDelayField.setInt(mc.playerController, 5);
     } catch (IllegalArgumentException | IllegalAccessException e) {
     e.printStackTrace();
     }
     }
     
     return destroyed;
     
     }*/
    
    @Override
    public void unload() {
        globalBlockEntities.clear();
    }
    
    @SubscribeEvent
    public void tickClient(ClientTickEvent event) {
        if (event.phase == Phase.END && (!mc.hasSingleplayerServer() || !mc.isPaused())) {
            tick();
            
            for (LittleLevelEntity entity : entities) {
                if (entity.level instanceof ISubLevel)
                    continue;
                entity.performTick();
            }
        }
    }
    
    public LittleHitResult getHit() {
        Player player = mc.player;
        HitResult result = mc.hitResult;
        float partialTicks = TickUtils.getFrameTime(level);
        
        Vec3 pos = player.getEyePosition(partialTicks);
        double reachDistance = result != null ? pos.distanceTo(result.getLocation()) : PlayerUtils.getReach(player);
        Vec3 look = player.getViewVector(partialTicks);
        look = pos.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        
        return getHit(pos, look, reachDistance);
    }
    
    @SubscribeEvent
    public void drawHighlight(RenderHighlightEvent event) {
        LittleHitResult result = getHit();
        
        if (result == null || !result.isBlock())
            return;
        
        event.setCanceled(true);
        BlockPos pos = result.asBlockHit().getBlockPos();
        BlockState state = result.level.getBlockState(pos);
        VertexConsumer vertexconsumer2 = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LittleLevelEntity entity = result.getHolder();
        float partialTicks = TickUtils.getFrameTime(level);
        entity.getOrigin().setupRendering(event.getPoseStack(), entity, partialTicks);
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
    }
    
    @SubscribeEvent
    public void collisionEvent(GetVoxelShapesEvent event) {
        if (event.level.isClientSide)
            collision(event);
    }
}
