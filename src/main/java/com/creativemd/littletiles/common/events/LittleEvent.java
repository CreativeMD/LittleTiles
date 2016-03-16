package com.creativemd.littletiles.common.events;

import scala.collection.parallel.ParIterableLike.Min;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.world.ChunkEvent.Unload;

public class LittleEvent {
	
	@SideOnly(Side.CLIENT)
	public static int renderPass;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPreRenderWorld(RenderWorldEvent.Pre event)
	{
		renderPass = event.pass;
	}
	
	@SubscribeEvent
	public void openContainer(PlayerOpenContainerEvent event){
		if(event.entityPlayer.openContainer instanceof ContainerWorkbench)
			event.setResult(Result.ALLOW);
	}
	
	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event)
	{
		if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			if(PlacementHelper.isLittleBlock(event.entityPlayer.getHeldItem()))
			{
				if(FMLCommonHandler.instance().getEffectiveSide().isClient())
				{
					MovingObjectPosition moving = Minecraft.getMinecraft().objectMouseOver;
					((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).onItemUse(event.entityPlayer.getHeldItem(), event.entityPlayer, event.world, event.x, event.y, event.z, event.face, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord);
				}
				event.setCanceled(true);		
			}
		}
	}
}
