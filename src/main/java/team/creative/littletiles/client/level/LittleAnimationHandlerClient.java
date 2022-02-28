package team.creative.littletiles.client.level;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mth;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawSelectionEvent.HighlightBlock;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.event.HoldLeftClick;
import team.creative.littletiles.client.event.InputEventHandler;
import team.creative.littletiles.client.event.LeftClick;
import team.creative.littletiles.client.event.WheelClick;
import team.creative.littletiles.client.render.entity.RenderAnimation;
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.event.GetVoxelShapesEvent;
import team.creative.littletiles.common.level.LittleAnimationHandler;
import team.creative.littletiles.common.math.vec.LittleHitResult;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationHandlerClient extends LittleAnimationHandler {
    
    private static Minecraft mc = Minecraft.getInstance();
    public static RenderAnimation render = new RenderAnimation(mc.get());
    
    public LittleAnimationHandlerClient(Level level) {
        super(level);
    }
    
    public static void renderTick() {
        float partialTicks = TickUtils.getDeltaFrameTime(mc.level);
        
        Entity renderViewEntity = mc.getCameraEntity();
        if (renderViewEntity == null || LittleTilesClient.ANIMATION_HANDLER == null || LittleTilesClient.ANIMATION_HANDLER.entities.isEmpty())
            return;
        double camX = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
        double camY = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
        double camZ = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
        
        TileEntityRendererDispatcher.staticPlayerX = camX;
        TileEntityRendererDispatcher.staticPlayerY = camY;
        TileEntityRendererDispatcher.staticPlayerZ = camZ;
        
        ICamera camera = new Frustum();
        camera.setPosition(camX, camY, camZ);
        
        for (LittleLevelEntity entity : LittleTilesClient.ANIMATION_HANDLER.entities) {
            
            if (!render.shouldRender(door, camera, camX, camY, camZ) || door.isDead)
                continue;
            
            if (door.ticksExisted == 0) {
                door.lastTickPosX = door.posX;
                door.lastTickPosY = door.posY;
                door.lastTickPosZ = door.posZ;
            }
            
            double d0 = door.lastTickPosX + (door.posX - door.lastTickPosX) * partialTicks;
            double d1 = door.lastTickPosY + (door.posY - door.lastTickPosY) * partialTicks;
            double d2 = door.lastTickPosZ + (door.posZ - door.lastTickPosZ) * partialTicks;
            
            float f = door.prevRotationYaw + (door.rotationYaw - door.prevRotationYaw) * partialTicks;
            int i = door.getBrightnessForRender();
            
            if (door.isBurning()) {
                i = 15728880;
            }
            
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            try {
                // render.setRenderOutlines(render.getRenderManager().renderOutlines);
                render.doRender(door, d0 - camX, d1 - camY, d2 - camZ, f, partialTicks);
            } catch (Throwable throwable1) {
                throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
            }
        }
    }
    
    @SubscribeEvent
    public void rightClick(PlayerInteractEvent event) {
        if (event instanceof RightClickBlock || event instanceof RightClickEmpty || event instanceof RightClickItem || event instanceof EntityInteractSpecific) {
            
            LittleHitResult result = getHit();
            if (result != null && result.level instanceof ISubLevel) {
                Entity entity = ((ISubLevel) result.level).getHolder();
                if (entity instanceof LittleLevelEntity levelEntity)
                    levelEntity.onRightClick(event.getPlayer(), result.hit);
            }
        }
    }
    
    @SubscribeEvent
    public void mouseWheel(WheelClick event) {
        LittleHitResult target = getHit();
        if (target == null && !target.isBlock())
            return;
        
        Player player = event.player;
        
        if (InputEventHandler.onPickBlock(target.asBlockHit(), player, target.level))
            return;
        
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
    
    private static final Method syncCurrentPlayItemMethod = ReflectionHelper.findMethod(PlayerControllerMP.class, "syncCurrentPlayItem", "func_78750_j");
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
        }*/
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
        float partialTicks = TickUtils.getDeltaFrameTime(level);
        
        Vec3 pos = player.getEyePosition(partialTicks);
        double reachDistance = result != null ? pos.distanceTo(result.getLocation()) : PlayerUtils.getReach(player);
        Vec3 look = player.getViewVector(partialTicks);
        look = pos.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        
        return getHit(pos, look, reachDistance);
    }
    
    @SubscribeEvent
    public void renderLast(RenderLevelLastEvent event) {
        if (mc.options.hideGui)
            return;
        
        LittleHitResult result = getHit();
        
        if (result == null && !result.isBlock())
            return;
        
        BlockPos pos = result.asBlockHit().getBlockPos();
        BlockState state = result.level.getBlockState(pos);
        VertexConsumer vertexconsumer2 = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        LittleLevelEntity entity = result.getHolder();
        float partialTicks = TickUtils.getDeltaFrameTime(level);
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
    public void drawHighlight(HighlightBlock event) {
        if (getHit() != null)
            event.setCanceled(true);
    }
    
    @SubscribeEvent
    public void collisionEvent(GetVoxelShapesEvent event) {
        if (event.level.isClientSide)
            collision(event);
    }
}
