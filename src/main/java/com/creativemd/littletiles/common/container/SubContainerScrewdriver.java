package com.creativemd.littletiles.common.container;

import java.util.Iterator;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class SubContainerScrewdriver extends SubContainer {

	public SubContainerScrewdriver(EntityPlayer player) {
		super(player);
	}

	@Override
	public void createControls() {
		
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}

}
