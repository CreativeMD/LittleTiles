package com.creativemd.littletiles.common.events;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
	public void onInteract(RightClickBlock event)
	{
		if(PlacementHelper.isLittleBlock(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND)))
		{
			if(event.getHand() == EnumHand.MAIN_HAND && FMLCommonHandler.instance().getEffectiveSide().isClient())
			{
				RayTraceResult moving = Minecraft.getMinecraft().objectMouseOver;
				((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).onItemUse(event.getEntityPlayer().getHeldItem(event.getHand()), event.getEntityPlayer(), event.getWorld(), event.getPos(), event.getHand(), event.getFace(), (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord);
			}
			event.setCanceled(true);		
		}
	}
}
