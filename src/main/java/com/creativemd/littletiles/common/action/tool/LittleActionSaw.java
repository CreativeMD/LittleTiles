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
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
	public LittleTileVec position = null;
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
			LittleTileBox box = null;
			oldBox = tile.box.copy();
			//oldBox.addOffset(te.getPos());
			
			if(secondMode)
				box = tile.box.shrink(facing, toLimit);
			else
				box = tile.box.expand(facing, toLimit);
			
			if(box.isValidBox())
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
				
				if(box.isBoxInsideBlock() && te.isSpaceForLittleTile(box, tile))
				{
					tile.box = box;
					te.updateBlock();
					position = tile.getAbsoluteCoordinates();
					return true;
				}else if(!box.isBoxInsideBlock()){
					box = box.createOutsideBlockBox(facing);
					if(box != null)
					{
						BlockPos newPos = te.getPos().offset(facing);
						IBlockState state = world.getBlockState(newPos);
						TileEntityLittleTiles littleTe = null;
						TileEntity newTE = world.getTileEntity(newPos);
						if(newTE instanceof TileEntityLittleTiles)
							littleTe = (TileEntityLittleTiles) newTE;
						if(state.getMaterial().isReplaceable())
						{
							//new TileEntityLittleTiles();
							world.setBlockState(newPos, LittleTiles.blockTile.getDefaultState());
							littleTe = (TileEntityLittleTiles) world.getTileEntity(newPos);
						}
						if(littleTe != null)
						{
							LittleTile newTile = tile.copy();
							newTile.box = box;
							newTile.te = littleTe;
							
							if(littleTe.isSpaceForLittleTile(box))
							{
								newTile.place();
								//littleTe.addTile(newTile);
								littleTe.updateBlock();
								position = newTile.getAbsoluteCoordinates();
								newBox = box.copy();
								newBox.addOffset(littleTe.getPos());
								return true;
							}
						}
					}
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
		return new LittleActionSawRevert(position, oldBox, facing);
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
		public LittleTileVec position;
		public LittleTileVec newPosition;
		public EnumFacing facing;
		
		public LittleActionSawRevert(LittleTileVec position, LittleTileBox oldBox, EnumFacing facing) {
			this.position = position;
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
			return new LittleActionSawRevert(newPosition, replacedBox, facing.getOpposite());
		}

		@Override
		protected boolean action(EntityPlayer player) throws LittleActionException {
			
			LittleTile tile = getTileAtPosition(player.world, position);
			if(tile == null)
				throw new LittleActionException.TileNotFoundException();
			
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
				
				newPosition = tile.getAbsoluteCoordinates();
				return true;
			}
			
			return false;
		}

		@Override
		public void writeBytes(ByteBuf buf) {
			writeLittleVec(position, buf);
			writeLittleBox(oldBox, buf);
			writeFacing(buf, facing);
		}

		@Override
		public void readBytes(ByteBuf buf) {
			position = readLittleVec(buf);
			oldBox = readLittleBox(buf);
			facing = readFacing(buf);
		}
	}
}
