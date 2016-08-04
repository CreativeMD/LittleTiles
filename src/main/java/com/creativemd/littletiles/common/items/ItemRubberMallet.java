package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.utils.TileList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.swing.TextComponent;

public class ItemRubberMallet extends Item {
	
	public ItemRubberMallet()
	{
		setCreativeTab(CreativeTabs.TOOLS);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("splits all tiles into");
		list.add("smallest pieces possible");
		list.add("limit: " + LittleTiles.maxNewTiles);
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			if(!world.isRemote)
			{
				if(player.isSneaking())
				{
					TileList<LittleTile> newTiles = TileEntityLittleTiles.createTileList();
					TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
					TileList<LittleTile> tiles = te.getTiles();
					for (int i = 0; i < tiles.size(); i++) {
						LittleTile oldTile = tiles.get(i);
						if((oldTile.getClass() == LittleTileBlock.class || oldTile instanceof LittleTileBlockColored) && oldTile.structure == null)
						{
							for (int j = 0; j < oldTile.boundingBoxes.size(); j++) {
								LittleTileBox box = oldTile.boundingBoxes.get(j);
								for (int littleX = box.minX; littleX < box.maxX; littleX++) {
									for (int littleY = box.minY; littleY < box.maxY; littleY++) {
										for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
											LittleTile tile = oldTile.copy();
											tile.boundingBoxes.clear();
											tile.boundingBoxes.add(new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1));
											tile.updateCorner();
											tile.te = te;
											newTiles.add(tile);
										}
									}
								}
							}
							
						}else
							newTiles.add(oldTile);
					}
					if(LittleTiles.maxNewTiles >= newTiles.size() - te.getTiles().size())
					{
						te.setTiles(newTiles);
						te.updateBlock();
					}else{
						player.addChatComponentMessage(new TextComponentTranslation("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
					}
				}
			}else{
				PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, player, 4, new NBTTagCompound()));
			}
			return EnumActionResult.SUCCESS;
		}else {
			IBlockState state = world.getBlockState(pos);
			
			if(tileEntity == null && SubContainerHammer.isBlockValid(state.getBlock()))
			{
				if(!world.isRemote)
				{
					if(LittleTiles.maxNewTiles < 4096)
					{
						player.addChatComponentMessage(new TextComponentTranslation("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
						return EnumActionResult.SUCCESS;
					}
					int meta = state.getBlock().getMetaFromState(state);
					TileEntityLittleTiles te = new TileEntityLittleTiles();
					for (int littleX = LittleTile.minPos; littleX < LittleTile.maxPos; littleX++) {
						for (int littleY = LittleTile.minPos; littleY < LittleTile.maxPos; littleY++) {
							for (int littleZ = LittleTile.minPos; littleZ < LittleTile.maxPos; littleZ++) {
								LittleTileBlock tile = new LittleTileBlock(state.getBlock(), meta);
								tile.boundingBoxes.add(new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1));
								tile.updateCorner();
								tile.te = te;
								te.getTiles().add(tile);
							}
						}
					}
					if(LittleTiles.maxNewTiles >= te.getTiles().size())
					{
						world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
						world.setTileEntity(pos, te);
					}else{
						player.addChatComponentMessage(new TextComponentTranslation("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
					}
					
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
    }
}
