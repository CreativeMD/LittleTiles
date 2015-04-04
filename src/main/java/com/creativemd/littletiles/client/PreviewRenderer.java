package com.creativemd.littletiles.client;

import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void tick(RenderTickEvent event)
	{
		if(event.phase == Phase.END && mc.thePlayer != null && mc.inGameHasFocus)
		{
			//TODO Add more Items for rendering
			if(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlockTiles && mc.objectMouseOver != null)
			{
				TileEntity tileEntity = mc.theWorld.getTileEntity(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
				if(tileEntity instanceof TileEntityLittleTiles)
				{
					//Render preview
					//TODO Add rendering for preview
				}
			}
		}
	}
}
