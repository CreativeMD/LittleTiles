package com.creativemd.littletiles.common.events;

import java.util.Iterator;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.BlockTile.TEResult;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockVanillaPacket;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.structure.LittleBed;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.common.world.WorldInteractor;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleEvent {
	
	@SubscribeEvent
	public void trackEntity(StartTracking event)
	{
		if(event.getTarget() instanceof EntityDoorAnimation && ((EntityDoorAnimation) event.getTarget()).activator != event.getEntityPlayer())
		{
			EntityDoorAnimation animation = (EntityDoorAnimation) event.getTarget();
			PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), true), (EntityPlayerMP) event.getEntityPlayer());
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void speedEvent(BreakSpeed event)
	{
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		float hardness = event.getState().getBlockHardness(world, event.getPos());
		ItemStack stack = player.getHeldItemMainhand();
		if(SubContainerHammer.isBlockValid(event.getState().getBlock()) && stack != null && stack.getItem() instanceof ItemUtilityKnife)
		{
			float modifier = 2;
			if(!ForgeHooks.canHarvestBlock(event.getState().getBlock(), player, world, event.getPos()))
				modifier *= 3.333333333F;
			event.setNewSpeed(event.getOriginalSpeed()*hardness*modifier);
		}
	}
	
	public static boolean cancelNext = false;
	
	@SubscribeEvent
	public void onInteract(RightClickBlock event)
	{
		if(cancelNext)
		{
			cancelNext = false;
			event.setCanceled(true);
		}
		
		ItemStack stack = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
		if(stack.getItem() == Items.GLOWSTONE_DUST && event.getEntityPlayer().isSneaking())
		{
			BlockTile.TEResult te = BlockTile.loadTeAndTile(event.getEntityPlayer().world, event.getPos(), event.getEntityPlayer());
			if(te.isComplete())
			{
				if(event.getHand() == EnumHand.MAIN_HAND && event.getWorld().isRemote)
				{
					if(te.tile.glowing)
						event.getEntityPlayer().playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
					else
						event.getEntityPlayer().playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
					te.tile.glowing = !te.tile.glowing;
					te.te.updateLighting();
					PacketHandler.sendPacketToServer(new LittleBlockPacket(event.getPos(), event.getEntityPlayer(), 5));
				}
				event.setCanceled(true);
			}
		}
		
		if(PlacementHelper.isLittleBlock(stack))
		{
			if(event.getHand() == EnumHand.MAIN_HAND && event.getWorld().isRemote)
				onRightInteractClient(event.getEntityPlayer(), event.getHand(), event.getWorld(), stack, event.getPos(), event.getFace());
			event.setCanceled(true);		
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void onRightInteractClient(EntityPlayer player, EnumHand hand, World world, ItemStack stack, BlockPos pos, EnumFacing facing)
	{
		RayTraceResult moving = Minecraft.getMinecraft().objectMouseOver;
		((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).onItemUse(player, world, pos, hand, facing, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event)
	{
		if(event.getTarget().typeOfHit == Type.BLOCK)
		{
			EntityPlayer player = event.getPlayer();
			World world = event.getPlayer().getEntityWorld();
			BlockPos pos = event.getTarget().getBlockPos();
			IBlockState state = world.getBlockState(pos);
			ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
			if(SubContainerHammer.isBlockValid(state.getBlock()) && stack != null && stack.getItem() instanceof ISpecialBlockSelector)
			{
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
		        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
		        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();
		        LittleTileBox box = ((ISpecialBlockSelector) stack.getItem()).getBox(world, pos, state, player, event.getTarget());
		        box.addOffset(new LittleTileVec(pos));
		        
		        GlStateManager.enableBlend();
	            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	            GlStateManager.glLineWidth(2.0F);
	            GlStateManager.disableTexture2D();
	            GlStateManager.depthMask(false);
				RenderGlobal.drawSelectionBoundingBox(box.getBox().expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
				GlStateManager.depthMask(true);
	            GlStateManager.enableTexture2D();
	            GlStateManager.disableBlend();
	            
				event.setCanceled(true);
			}
			
		}
	}
	
	@SubscribeEvent
	public void isSleepingLocationAllowed(SleepingLocationCheckEvent event)
	{
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
			if(bed instanceof LittleBed && ((LittleBed) bed).sleepingPlayer == event.getEntityPlayer())
				event.setResult(Result.ALLOW);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event)
	{
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.player);
			if(bed instanceof LittleBed)
				((LittleBed) bed).sleepingPlayer = null;
			LittleBed.littleBed.set(event.player, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event)
	{
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
			if(bed instanceof LittleBed)
				((LittleBed) bed).sleepingPlayer = null;
			LittleBed.littleBed.set(event.getEntityPlayer(), null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerSleep(PlayerSleepInBedEvent event)
	{
		if(event.getEntityPlayer().world.getBlockState(event.getPos()).getBlock() instanceof BlockTile)
		{
			TileEntityLittleTiles te = BlockTile.loadTe(event.getEntityPlayer().world, event.getPos());
			if(te != null)
			{
				for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					if(tile.structure instanceof LittleBed && ((LittleBed) tile.structure).hasBeenActivated)
					{
						((LittleBed) tile.structure).trySleep(event.getEntityPlayer(), tile.structure.getHighestCenterPoint());
						event.setResult(SleepResult.OK);
						((LittleBed) tile.structure).hasBeenActivated = false;
						return ;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void worldCollision(GetCollisionBoxesEvent event)
	{
		for (int i = 0; i < event.getWorld().loadedEntityList.size(); i++) {
			Entity entity = event.getWorld().loadedEntityList.get(i);
			if(entity instanceof EntityDoorAnimation)
			{
				
			}
		}
		
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			ItemModelCache.tick();
		}
	}
	
	@SubscribeEvent
	public void ChunkUnload(ChunkEvent.Unload event)
	{
		WorldInteractor.addChunkToBeRemoved(event.getWorld(), event.getChunk());
	}
}
