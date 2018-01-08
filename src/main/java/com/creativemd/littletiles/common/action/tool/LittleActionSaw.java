package com.creativemd.littletiles.common.action.tool;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileAbsoluteCoord;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionSaw extends LittleActionInteract {
	
	public boolean toLimit;
	
	public LittleActionSaw(BlockPos blockPos, EntityPlayer player, boolean toLimit) {
		super(blockPos, player);
		this.toLimit = toLimit;
	}
	
	public LittleActionSaw() {
		super();
	}

	@Override
	protected boolean isRightClick() {
		return true;
	}
	
	public LittleTileBox oldBox = null;
	public LittleTileBox newBox = null;
	public LittleTileAbsoluteCoord coord = null;
	public EnumFacing facing;
	
	@Override
	public RayTraceResult rayTrace(TileEntityLittleTiles te, LittleTile tile)
	{
		return new LittleTileBox(tile.box).calculateIntercept(te.getPos(), pos, look);
	}
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
		
		facing = moving.sideHit;
		if(tile.canSawResizeTile(facing, player))
		{
			LittleTileBox box;
			oldBox = tile.box.copy();
			Axis axis = facing.getAxis();
			
			boolean outside = false;
			
			if(secondMode)
				box = tile.box.shrink(facing, toLimit);
			else
			{	
				box = tile.box.grow(facing);
				if(tile.box.isFaceAtEdge(facing))
				{
					BlockPos newPos = te.getPos().offset(facing);
					te = loadTe(world, newPos, true);
					box = box.createOutsideBlockBox(facing);
					outside = true;
					if(te == null)
						return false;
				}
				
				if(toLimit)
				{
					LittleTileBox before = null;
					while(!box.isFaceAtEdge(facing) && te.isSpaceForLittleTile(box, tile))
					{
						before = box;
						box = box.grow(facing);
					}
					if(!te.isSpaceForLittleTile(box, tile))
						box = before;
				}
				else if(!te.isSpaceForLittleTile(box, tile))
					box = null;
			}
			
			if(box != null)
			{
				double amount = Math.abs(box.getPercentVolume()-tile.box.getPercentVolume());
				BlockIngredients ingredients = new BlockIngredients();
				LittleTilePreview preview = tile.getPreviewTile();
				BlockIngredient ingredient = preview.getBlockIngredient();
				ingredient.value = amount;
				ingredients.addIngredient(ingredient);
				
				ColorUnit unit = null;
				if(preview.hasColor())
				{
					unit = ColorUnit.getRequiredColors(preview.getColor());
					unit.BLACK *= amount;
					unit.RED *= amount;
					unit.GREEN *= amount;
					unit.BLUE *= amount;
				}
				
				if(secondMode)
					addIngredients(player, ingredients, unit);
				else
					drainIngredients(player, ingredients, unit);
				
				if(outside)
				{
					LittleTile newTile = tile.copy();
					newTile.box = box;
					newTile.te = te;
					newTile.place();
					//littleTe.addTile(newTile);
					te.updateBlock();
					coord = new LittleTileAbsoluteCoord(newTile);
					newBox = box.copy();
					newBox.addOffset(te.getPos());
					return true;
				}
				else
				{
					tile.box = box;
					te.updateBlock();
					coord = new LittleTileAbsoluteCoord(tile);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		if(newBox != null)
		{
			List<LittleTileBox> boxes = new ArrayList<>();
			boxes.add(newBox);
			return new LittleActionDestroyBoxes(boxes);
		}
		return new LittleActionSawRevert(coord, oldBox, facing);
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeBoolean(toLimit);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		toLimit = buf.readBoolean();
	}
	
	public static class LittleActionSawRevert extends LittleAction {
		
		public LittleTileBox oldBox;
		public LittleTileBox replacedBox;
		public LittleTileAbsoluteCoord coord;
		public LittleTileAbsoluteCoord newCoord;
		public EnumFacing facing;
		
		public LittleActionSawRevert(LittleTileAbsoluteCoord coord, LittleTileBox oldBox, EnumFacing facing) {
			this.coord = coord;
			this.oldBox = oldBox;
			this.facing = facing;
		}
		
		public LittleActionSawRevert() {
			
		}

		@Override
		public boolean canBeReverted() {
			return true;
		}

		@Override
		public LittleAction revert() throws LittleActionException {
			return new LittleActionSawRevert(newCoord, replacedBox, facing.getOpposite());
		}

		@Override
		protected boolean action(EntityPlayer player) throws LittleActionException {
			
			LittleTile tile = getTile(player.world, coord);
			
			if(tile.canSawResizeTile(facing, player))
			{
				double amount = Math.abs(oldBox.getPercentVolume()-tile.box.getPercentVolume());
				BlockIngredients ingredients = new BlockIngredients();
				LittleTilePreview preview = tile.getPreviewTile();
				BlockIngredient ingredient = preview.getBlockIngredient();
				ingredient.value = amount;
				ingredients.addIngredient(ingredient);
				
				ColorUnit unit = null;
				if(preview.hasColor())
				{
					unit = ColorUnit.getRequiredColors(preview.getColor());
					unit.BLACK *= amount;
					unit.RED *= amount;
					unit.GREEN *= amount;
					unit.BLUE *= amount;
				}
				
				if(oldBox.getVolume() < tile.box.getVolume())
					addIngredients(player, ingredients, unit);
				else
					drainIngredients(player, ingredients, unit);
				
				replacedBox = tile.box.copy();
				//replacedBox.addOffset(tile.te.getPos());
				tile.box = oldBox;
				
				tile.te.updateBlock();
				
				newCoord = new LittleTileAbsoluteCoord(tile);
				return true;
			}
			
			return false;
		}

		@Override
		public void writeBytes(ByteBuf buf) {
			writeAbsoluteCoord(coord, buf);
			writeLittleBox(oldBox, buf);
			writeFacing(buf, facing);
		}

		@Override
		public void readBytes(ByteBuf buf) {
			coord = readAbsoluteCoord(buf);
			oldBox = readLittleBox(buf);
			facing = readFacing(buf);
		}
	}
}
