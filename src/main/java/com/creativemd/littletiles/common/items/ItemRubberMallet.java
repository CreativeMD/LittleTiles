package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
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
import scala.tools.nsc.transform.patmat.Solving.Solver.Lit;

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
		list.add("rightclick moves tiles in faced direction");
		list.add("shift+rightclick moves tiles in oposite faced direction");
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
				PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, player, 4, nbt));
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
				//if(mo)
				//tile.isMainBlock = movingTile.isMainBlock;
				//tile.structure.mainTile = tile;
			}
			
			if(movingTile.isStructureBlock)
			{
				tile.structure.addTile(tile);
				//littleTE.combineTiles(tile.structure);
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
					placeMovingTile(movingTile, littleTE, box);
					if(state.getBlock() != LittleTiles.blockTile)
					{
						world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
						world.setTileEntity(pos, littleTE);
					}
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
								
								littleTE.updateBlock();
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
