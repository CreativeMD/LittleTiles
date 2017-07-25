package com.creativemd.littletiles.common.items;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRubberMallet extends Item {
	
	public ItemRubberMallet()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add("rightclick moves tiles in faced direction");
		tooltip.add("shift+rightclick moves tiles in oposite faced direction");
		//list.add("limit: " + LittleTiles.maxNewTiles);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			if(world.isRemote)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("side", facing.getIndex());
				PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, player, BlockPacketAction.RUBBER_MALLET, nbt));
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
    }
	
	public static boolean reverseMoveTile(TileEntityLittleTiles te, EnumFacing facing, LittleTile tile, boolean simulate, LittleTileBox box)
	{
		if(box == null)
		{
			LittleTileVec vec = new LittleTileVec(facing.getOpposite());
			box = tile.boundingBoxes.get(0).copy();
			box.addOffset(vec);
		}
		
		
		if(tile.isStructureBlock)
			return moveTile(te, facing, tile, simulate, true);
		else if(box.isBoxInsideBlock()){
			LittleTile intersecting = te.getIntersectingTile(box, tile);
			
			if(intersecting != null && intersecting.canBeMoved(facing.getOpposite()) && !intersecting.isStructureBlock && intersecting.canBeCombined(tile) && tile.canBeCombined(intersecting) && intersecting.boundingBoxes.get(0).doesMatchTwoSides(box, facing.getOpposite()))
			{
				return reverseMoveTile(te, facing, intersecting, simulate, null);
			}else{
				 return moveTile(te, facing, tile, simulate, true);
			}
		}else{
			box = box.createOutsideBlockBox(facing.getOpposite());
			TileEntity tileEntity = te.getWorld().getTileEntity(te.getPos().offset(facing.getOpposite()));
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				return reverseMoveTile((TileEntityLittleTiles) tileEntity, facing, tile, simulate, box);
			}else
				return moveTile(te, facing, tile, simulate, true);
		}
	}
	
	public static boolean moveTile(TileEntityLittleTiles te, EnumFacing facing, LittleTile tile, boolean simulate, boolean push)
	{
		if(!push)
			return reverseMoveTile(te, facing, tile, simulate, null);
		LittleTileVec vec = new LittleTileVec(facing);
		LittleTileBox box = tile.boundingBoxes.get(0).copy();
		box.addOffset(vec);
		
		if(box.isBoxInsideBlock())
		{
			return tryMoveTile(te.getWorld(), te.getPos(), facing, box, tile, simulate);
		}else{
			if(tryMoveTile(te.getWorld(), te.getPos().offset(facing), facing, box.createOutsideBlockBox(facing), tile, simulate))
			{
				if(!simulate)
				{
					te.preventUpdate = true;
					box.makeItFitInsideBlock();
					if(box.isValidBox())
					{
						tile.boundingBoxes.clear();
						tile.boundingBoxes.add(box);
						te.updateBlock();
					}else{
						if(tile.isStructureBlock)
							tile.structure.removeTile(tile);;
						te.removeTile(tile);
					}
					te.preventUpdate = false;
					te.updateTiles();
				}
				return true;
			}
			return false;
		}
	}
	
	private static void placeMovingTile(LittleTile movingTile, TileEntityLittleTiles littleTE, LittleTileBox box)
	{
		
		LittleTile tile = movingTile;
		if(movingTile.te != littleTE)
			tile = movingTile.copy();
		tile.boundingBoxes.clear();
		tile.boundingBoxes.add(box);
		tile.updateCorner();
		
		if(movingTile.te != littleTE)
		{
			tile.te = littleTE;
			tile.place();
			
			if(movingTile.isStructureBlock)
			{
				tile.isStructureBlock = true;
				tile.structure.addTile(tile);
			}
		}
	}
	
	public static boolean tryMoveTile(World world, BlockPos pos, EnumFacing facing, LittleTileBox box, LittleTile movingTile, boolean simulate)
	{
		IBlockState state = world.getBlockState(pos);
		TileEntity te = world.getTileEntity(pos);
		TileEntityLittleTiles littleTE = null;
		if(state.getMaterial().isReplaceable())
		{ 
			littleTE = new TileEntityLittleTiles();
		}
		if(te instanceof TileEntityLittleTiles)
			littleTE = (TileEntityLittleTiles) te;
		
		if(littleTE != null)
		{
			if(littleTE.isSpaceForLittleTile(box, movingTile))
			{
				if(!simulate)
				{
					if(state.getBlock() != LittleTiles.blockTile)
					{
						world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
						world.setTileEntity(pos, littleTE);
					}
					placeMovingTile(movingTile, littleTE, box);
					
				}
				return true;
			}else{
				LittleTile tile = littleTE.getIntersectingTile(box, movingTile);
				if(!tile.canBeMoved(facing))
					return false;
				if(movingTile.isStructureBlock)
				{
					if(tile.isStructureBlock && tile.structure == movingTile.structure)
					{
						if(!simulate)
						{
							placeMovingTile(movingTile, littleTE, box);
							if(state.getBlock() != LittleTiles.blockTile)
							{
								world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
								world.setTileEntity(pos, littleTE);
							}
						}
						return true;
					}
				}else if(movingTile.canBeCombined(tile) && tile.canBeCombined(movingTile)){
					if(tile.boundingBoxes.get(0).doesMatchTwoSides(box, facing))
					{
						if(moveTile(littleTE, facing, tile, simulate, true))
						{
							if(!simulate)
							{
								littleTE.preventUpdate = true;
								LittleTileBox newBox = tile.boundingBoxes.get(0).copy();
								newBox.makeItFitInsideBlock();
								newBox = newBox.combineBoxes(box);
								if(newBox != null)
								{
									if(littleTE == movingTile.te)
									{
										movingTile.boundingBoxes.clear();
										movingTile.boundingBoxes.add(newBox);
										movingTile.updateCorner();
										littleTE.removeTile(tile);
									}else{
										tile.boundingBoxes.clear();
										tile.boundingBoxes.add(newBox);
										tile.updateCorner();
									}
								}
								littleTE.preventUpdate = false;
								littleTE.updateTiles();
							}
							return true;
						}
					}
				}
			}
		}
		return false;
		
	}
	
}
