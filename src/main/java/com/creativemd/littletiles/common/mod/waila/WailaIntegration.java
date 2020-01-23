package com.creativemd.littletiles.common.mod.waila;

import com.creativemd.littletiles.common.block.BlockTile;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@WailaPlugin
public class WailaIntegration implements IWailaPlugin {
	
	@Override
	public void register(IWailaRegistrar registrar) {
		registrar.registerStackProvider(new IWailaDataProvider() {
			
			@Override
			public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
				return ItemStack.EMPTY;
			}
			
			@Override
			public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
				return new NBTTagCompound();
			}
			
		}, BlockTile.class);
	}
	
}
