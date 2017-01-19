package com.creativemd.littletiles.common.events;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.packet.LittleBlockVanillaPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleEvent {
	
	@SubscribeEvent
	public void openContainer(PlayerContainerEvent event){
		if(event.getContainer() instanceof ContainerWorkbench)
			event.setResult(Result.ALLOW);
	}
	
	@SubscribeEvent
	public void onInteract(LeftClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		World world = player.getEntityWorld();
		BlockPos pos = event.getPos();
		IBlockState state = world.getBlockState(pos);
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if(event.getHand() == EnumHand.MAIN_HAND && SubContainerHammer.isBlockValid(state.getBlock()) && stack != null && stack.getItem() instanceof ISpecialBlockSelector)
		{
			if(world.isRemote)
				onLeftInteractClient(stack, player, world, pos, state);
			event.setCanceled(true);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void onLeftInteractClient(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState state)
	{
		LittleTileBox box = ((ISpecialBlockSelector) stack.getItem()).getBox(world, pos, state, player, Minecraft.getMinecraft().objectMouseOver);
		
		world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
		TileEntityLittleTiles te = (TileEntityLittleTiles) world.getTileEntity(pos);
		
		LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
		tile.boundingBoxes.add(new LittleTileBox(0,0,0,LittleTile.maxPos,LittleTile.maxPos,LittleTile.maxPos));
		tile.te = te;
		tile.place();
		te.removeBoxFromTiles(box);
		if(!player.capabilities.isCreativeMode)
		{
			tile.boundingBoxes.clear();
			tile.boundingBoxes.add(box.copy());
			//WorldUtils.dropItem(player, tile.getDrops());
		}
		
		PacketHandler.sendPacketToServer(new LittleBlockVanillaPacket(pos, player));
		
	}
	
	@SubscribeEvent
	public void onInteract(RightClickBlock event)
	{
		if(PlacementHelper.isLittleBlock(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND)))
		{
			if(event.getHand() == EnumHand.MAIN_HAND && event.getWorld().isRemote)
				onRightInteractClient(event.getEntityPlayer(), event.getHand(), event.getWorld(), event.getEntityPlayer().getHeldItem(event.getHand()), event.getPos(), event.getFace());
			event.setCanceled(true);		
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void onRightInteractClient(EntityPlayer player, EnumHand hand, World world, ItemStack stack, BlockPos pos, EnumFacing facing)
	{
		RayTraceResult moving = Minecraft.getMinecraft().objectMouseOver;
		((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).onItemUse(stack, player, world, pos, hand, facing, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord);
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
				RenderGlobal.func_189697_a(box.getBox().expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
				GlStateManager.depthMask(true);
	            GlStateManager.enableTexture2D();
	            GlStateManager.disableBlend();
	            
				event.setCanceled(true);
			}
			
		}
	}
	
}
