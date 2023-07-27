package com.creativemd.littletiles.client.world;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.event.HoldLeftClick;
import com.creativemd.littletiles.client.event.InputEventHandler;
import com.creativemd.littletiles.client.event.LeftClick;
import com.creativemd.littletiles.client.event.WheelClick;
import com.creativemd.littletiles.client.render.entity.RenderAnimation;
import com.creativemd.littletiles.client.render.overlay.PreviewRenderer;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.LittleAnimationHandler;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleAnimationHandlerClient extends LittleAnimationHandler {
    
    private static Minecraft mc = Minecraft.getMinecraft();
    public static RenderAnimation render = new RenderAnimation(mc.getRenderManager());
    
    public LittleAnimationHandlerClient(World world) {
        super(world);
    }
    
    public static void renderTick() {
        float partialTicks = TickUtils.getPartialTickTime();
        
        Entity renderViewEntity = mc.getRenderViewEntity();
        if (renderViewEntity == null || WorldAnimationHandler.client == null || WorldAnimationHandler.client.openDoors.isEmpty())
            return;
        double camX = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
        double camY = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
        double camZ = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
        
        TileEntityRendererDispatcher.staticPlayerX = camX;
        TileEntityRendererDispatcher.staticPlayerY = camY;
        TileEntityRendererDispatcher.staticPlayerZ = camZ;
        
        ICamera camera = new Frustum();
        camera.setPosition(camX, camY, camZ);
        
        for (EntityAnimation door : WorldAnimationHandler.client.openDoors) {
            
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
    
    public void rightClick(PlayerInteractEvent event) {
        if (event instanceof RightClickBlock || event instanceof RightClickEmpty || event instanceof RightClickItem || event instanceof EntityInteractSpecific/* || event instanceof EntityInteract*/) {
            
            RayTraceResult target = (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK) ? mc.objectMouseOver : null;
            EntityPlayer player = event.getEntityPlayer();
            float partialTicks = TickUtils.getPartialTickTime();
            
            Vec3d pos = player.getPositionEyes(partialTicks);
            double d0 = target != null ? pos.distanceTo(target.hitVec) : (player.capabilities.isCreativeMode ? 5.0 : 4.5);
            Vec3d look = player.getLook(partialTicks);
            look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
            
            AxisAlignedBB box = new AxisAlignedBB(pos, target != null ? target.hitVec : look);
            World world = player.world;
            
            EntityAnimation pointedEntity = null;
            
            RayTraceResult result = target;
            double distance = result != null ? pos.distanceTo(result.hitVec) : 0;
            for (EntityAnimation animation : findAnimations(box)) {
                RayTraceResult tempResult = getTarget(animation.fakeWorld, animation.origin.transformPointToFakeWorld(pos), animation.origin
                    .transformPointToFakeWorld(look), pos, look);
                if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
                    continue;
                double tempDistance = pos.distanceTo(animation.origin.transformPointToWorld(tempResult.hitVec));
                if (result == null || tempDistance < distance) {
                    result = tempResult;
                    distance = tempDistance;
                    pointedEntity = animation;
                }
            }
            
            Entity selectedEntity = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
            if (event instanceof EntityInteractSpecific)
                selectedEntity = ((EntityInteractSpecific) event).getTarget();
            else if (event instanceof EntityInteract)
                selectedEntity = ((EntityInteract) event).getTarget();
            
            if (pointedEntity == null && selectedEntity instanceof EntityAnimation)
                pointedEntity = (EntityAnimation) selectedEntity;
            
            if (pointedEntity != null) {
                if (!pointedEntity.onRightClick(player, pos, look))
                    LittleTilesClient.INTERACTION.start(true);
                if (event instanceof RightClickBlock) {
                    event.setCanceled(true);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                }
            }
        }
    }
    
    public void mouseWheel(WheelClick event) {
        RayTraceResult target = getRayTraceResult(event.player, TickUtils.getPartialTickTime(), null);
        if (target == null)
            return;
        
        World world = lastWorldRayTraceResult;
        EntityPlayer player = event.player;
        
        if (InputEventHandler.onPickBlock(target, player, world)) {
            event.setCanceled(true);
            return;
        }
        
        IBlockState state = world.getBlockState(target.getBlockPos());
        
        if (state.getBlock().isAir(state, world, target.getBlockPos()))
            return;
        
        ItemStack stack = state.getBlock().getPickBlock(state, target, world, target.getBlockPos(), player);
        
        if (stack.isEmpty())
            return;
        
        if (event.player.isCreative()) {
            player.inventory.setPickedItemStack(stack);
            mc.playerController.sendSlotPacket(player.getHeldItem(EnumHand.MAIN_HAND), 36 + player.inventory.currentItem);
            event.setCanceled(true);
        }
        int slot = player.inventory.getSlotFor(stack);
        if (slot != -1) {
            if (InventoryPlayer.isHotbar(slot))
                player.inventory.currentItem = slot;
            else
                mc.playerController.pickItem(slot);
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
    
    public boolean isHittingPos(World world, BlockPos pos) {
        ItemStack itemstack = LittleAnimationHandlerClient.mc.player.getHeldItemMainhand();
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
    
    public boolean clickBlockCreative(World world, EntityPlayer player, BlockPos pos, EnumFacing facing) {
        if (!world.extinguishFire(player, pos, facing))
            return onPlayerDestroyBlock(player, world, pos);
        return true;
    }
    
    public void sendBlockBreakProgress(int breakerId, World world, BlockPos pos, int progress) {
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
    
    public void addBlockHitEffects(World world, BlockPos pos, RayTraceResult target) {
        IBlockState state = world.getBlockState(pos);
        if (state != null && !state.getBlock().addHitEffects(state, world, target, mc.effectRenderer)) {
            mc.effectRenderer.addBlockHitEffects(world instanceof CreativeWorld ? ((CreativeWorld) world).transformToRealWorld(pos) : pos, target.sideHit);
        }
    }
    
    public void holdClick(HoldLeftClick event) {
        RayTraceResult result = getRayTraceResult(event.player, TickUtils.getPartialTickTime(), null);
        if (result == null || !event.leftClick) {
            if (isHittingBlock)
                resetBlockRemoving();
            return;
        }
        
        World world = lastWorldRayTraceResult;
        EntityPlayer player = event.player;
        BlockPos pos = result.getBlockPos();
        
        try {
            if (leftClickCounterField.getInt(mc) <= 0 && !player.isHandActive()) {
                if (onPlayerDamageBlock(world, player, pos, result.sideHit, event)) {
                    addBlockHitEffects(world, pos, result);
                    player.swingArm(EnumHand.MAIN_HAND);
                    event.setLeftClickResult(false);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }
    
    public boolean onPlayerDamageBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing facing, HoldLeftClick event) {
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
    
    public boolean clickBlock(World world, BlockPos loc, EnumFacing face) {
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
    
    public void leftClick(LeftClick event) {
        RayTraceResult result = getRayTraceResult(event.player, TickUtils.getPartialTickTime(), null);
        if (result == null)
            return;
        
        if (clickBlock(lastWorldRayTraceResult, result.getBlockPos(), result.sideHit))
            event.setCanceled(true);
    }
    
    public boolean onPlayerDestroyBlock(EntityPlayer player, World world, BlockPos pos) {
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
    
    public void tickClient(ClientTickEvent event) {
        if (event.phase == Phase.END && (!mc.isSingleplayer() || !mc.isGamePaused())) {
            for (EntityAnimation door : openDoors) {
                if (door.world instanceof CreativeWorld)
                    continue;
                door.onUpdateForReal();
            }
            
            openDoors.removeIf((x) -> {
                if (x.isDead) {
                    x.markRemoved();
                    return true;
                }
                return false;
            });
        }
    }
    
    private EntityPlayer lastPlayerRayTraceResult;
    private RayTraceResult lastRayTraceResult;
    private CreativeWorld lastWorldRayTraceResult;
    
    public RayTraceResult getRayTraceResult(EntityPlayer player, float partialTicks, @Nullable RayTraceResult target) {
        if (lastPlayerRayTraceResult == player)
            return lastRayTraceResult;
        
        Vec3d pos = player.getPositionEyes(partialTicks);
        double d0 = target != null ? pos.distanceTo(target.hitVec) : (player.capabilities.isCreativeMode ? 5.0 : 4.5);
        Vec3d look = player.getLook(partialTicks);
        look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
        
        AxisAlignedBB box = new AxisAlignedBB(pos, target != null ? target.hitVec : look);
        World world = player.world;
        
        RayTraceResult result = target;
        double distance = result != null ? pos.distanceTo(result.hitVec) : 0;
        for (EntityAnimation animation : findAnimations(box)) {
            RayTraceResult tempResult = getTarget(animation.fakeWorld, animation.origin.transformPointToFakeWorld(pos), animation.origin
                .transformPointToFakeWorld(look), pos, look);
            if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
                continue;
            double tempDistance = pos.distanceTo(animation.origin.transformPointToWorld(tempResult.hitVec));
            if (result == null || tempDistance < distance) {
                result = tempResult;
                distance = tempDistance;
            }
        }
        
        lastPlayerRayTraceResult = player;
        if (result == target)
            result = null;
        lastRayTraceResult = result;
        lastWorldRayTraceResult = result != null ? (CreativeWorld) result.hitInfo : null;
        return result;
    }
    
    public void renderLast(RenderWorldLastEvent event) {
        if (mc.gameSettings.hideGUI)
            return;
        EntityPlayer player = mc.player;
        float partialTicks = event.getPartialTicks();
        
        lastPlayerRayTraceResult = null;
        lastRayTraceResult = null;
        lastWorldRayTraceResult = null;
        
        RayTraceResult result = getRayTraceResult(player, event
            .getPartialTicks(), (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK) ? mc.objectMouseOver : null);
        
        if (result == null)
            return;
        
        GlStateManager.enableBlend();
        GlStateManager
            .tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.enableTexture2D();
        mc.renderEngine.bindTexture(PreviewRenderer.WHITE_TEXTURE);
        GlStateManager.depthMask(false);
        GlStateManager.enableRescaleNormal();
        
        BlockPos blockpos = result.getBlockPos();
        IBlockState iblockstate = lastWorldRayTraceResult.getBlockState(blockpos);
        
        if (iblockstate.getMaterial() != Material.AIR && lastWorldRayTraceResult.getWorldBorder().contains(blockpos)) {
            
            EntityAnimation entity = (EntityAnimation) lastWorldRayTraceResult.parent;
            GlStateManager.pushMatrix();
            
            entity.origin.setupRendering(entity, partialTicks);
            RenderGlobal.drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(lastWorldRayTraceResult, blockpos).grow(0.0020000000949949026D), 0.0F, 0.0F, 0.0F, 0.4F);
            GlStateManager.popMatrix();
        }
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    public void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks) {
        /*double d3 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double d4 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
        double d5 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;
        
        if (!this.damagedBlocks.isEmpty()) {
        	mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        	mc.renderGlobal.preRenderDamagedBlocks();
        	bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
        	bufferBuilderIn.setTranslation(-d3, -d4, -d5);
        	bufferBuilderIn.noColor();
        	Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();
        	
        	while (iterator.hasNext()) {
        		DestroyBlockProgress destroyblockprogress = iterator.next();
        		BlockPos blockpos = destroyblockprogress.getPosition();
        		double d6 = blockpos.getX() - d3;
        		double d7 = blockpos.getY() - d4;
        		double d8 = blockpos.getZ() - d5;
        		Block block = this.world.getBlockState(blockpos).getBlock();
        		TileEntity te = this.world.getTileEntity(blockpos);
        		boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;
        		if (!hasBreak)
        			hasBreak = te != null && te.canRenderBreaking();
        		
        		if (!hasBreak) {
        			if (d6 * d6 + d7 * d7 + d8 * d8 > 1024.0D) {
        				iterator.remove();
        			} else {
        				IBlockState iblockstate = this.world.getBlockState(blockpos);
        				
        				if (iblockstate.getMaterial() != Material.AIR) {
        					int k1 = destroyblockprogress.getPartialBlockDamage();
        					TextureAtlasSprite textureatlassprite = mc.renderGlobal.destroyBlockIcons[k1];
        					BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
        					blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.world);
        				}
        			}
        		}
        	}
        	
        	tessellatorIn.draw();
        	bufferBuilderIn.setTranslation(0.0D, 0.0D, 0.0D);
        	mc.renderGlobal.postRenderDamagedBlocks();
        }*/
    }
    
    public void drawHighlight(DrawBlockHighlightEvent event) {
        if (getRayTraceResult(lastPlayerRayTraceResult, event.getPartialTicks(), event.getTarget()) != null)
            event.setCanceled(true);
    }
    
    public static RayTraceResult getTarget(CreativeWorld world, Vec3d pos, Vec3d look, Vec3d originalPos, Vec3d originalLook) {
        RayTraceResult tempResult = world.rayTraceBlocks(pos, look);
        if (tempResult == null || tempResult.typeOfHit != RayTraceResult.Type.BLOCK)
            return null;
        tempResult.hitInfo = world;
        return tempResult;
    }
}
