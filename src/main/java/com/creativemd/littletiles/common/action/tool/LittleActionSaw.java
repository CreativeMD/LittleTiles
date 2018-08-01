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
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

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
	public LittleGridContext context;
	
	public LittleActionSaw(BlockPos blockPos, EntityPlayer player, boolean toLimit, LittleGridContext context) {
		super(blockPos, player);
		this.toLimit = toLimit;
		this.context = context;
	}
	
	public LittleActionSaw() {
		super();
	}

	@Override
	protected boolean isRightClick() {
		return true;
	}
	
	public LittleTileBox oldBox = null;
	public LittleBoxes newBoxes;
	public LittleTileIdentifierAbsolute coord = null;
	public EnumFacing facing;
	
	@Override
	public RayTraceResult rayTrace(TileEntityLittleTiles te, LittleTile tile)
	{
		return new LittleTileBox(tile.box).calculateIntercept(te.getContext(), te.getPos(), pos, look);
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
			
			te.ensureMinContext(context);
			if(context != te.getContext())
				context = te.getContext();
			
			boolean outside = false;
			
			if(secondMode)
				box = tile.box.shrink(facing, toLimit);
			else
			{
				box = tile.box.grow(facing);
				if(tile.box.isFaceAtEdge(te.getContext(), facing))
				{
					BlockPos newPos = te.getPos().offset(facing);
					
					box = box.createOutsideBlockBox(te.getContext(), facing);
					LittleGridContext context = LittleGridContext.get(box.getSmallestContext(te.getContext()));
					te = loadTe(player, world, newPos, false);
					
					if(te == null)
						return false;
					
					if(context != te.getContext())
					{
						if(context.size > te.getContext().size)
							te.ensureMinContext(context);
						else
							box.convertTo(context, te.getContext());
					}
					
					outside = true;
				}
				
				if(toLimit)
				{
					LittleTileBox before = null;
					while(!box.isFaceAtEdge(te.getContext(), facing) && te.isSpaceForLittleTile(box, tile))
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
				double amount = Math.abs(box.getPercentVolume(te.getContext())-tile.box.getPercentVolume(te.getContext()));
				BlockIngredients ingredients = new BlockIngredients();
				LittleTilePreview preview = tile.getPreviewTile();
				BlockIngredient ingredient = preview.getBlockIngredient(te.getContext());
				if(ingredient != null)
				{
					ingredient.value = amount;
					ingredients.addIngredient(ingredient);
					
					ColorUnit unit = null;
					if(preview.hasColor())
					{
						unit = ColorUnit.getColors(preview.getColor());
						unit.scaleLoose(amount);
					}
					
					if(secondMode)
						addIngredients(player, ingredients, unit);
					else
						drainIngredients(player, ingredients, unit);
				
				}
				
				if(outside)
				{
					LittleTile newTile = tile.copy();
					newTile.box = box;
					newTile.te = te;
					newTile.place();
					te.updateBlock();
					newBoxes = new LittleBoxes(te.getPos(), te.getContext());
					newBoxes.addBox(newTile);
					te.convertToSmallest();
					return true;
				}
				else
				{
					tile.box = box;
					te.updateBlock();
					coord = new LittleTileIdentifierAbsolute(tile);
					te.convertToSmallest();
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
		if(newBoxes != null)
			return new LittleActionDestroyBoxes(newBoxes);
		return new LittleActionSawRevert(context, coord, oldBox, facing);
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeBoolean(toLimit);
		writeContext(context, buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		toLimit = buf.readBoolean();
		context = readContext(buf);
	}
	
	public static class LittleActionSawRevert extends LittleAction {
		
		public LittleTileBox oldBox;
		public LittleTileBox replacedBox;
		public LittleTileIdentifierAbsolute coord;
		public LittleTileIdentifierAbsolute newCoord;
		public EnumFacing facing;
		public LittleGridContext context;
		
		public LittleActionSawRevert(LittleGridContext context, LittleTileIdentifierAbsolute coord, LittleTileBox oldBox, EnumFacing facing) {
			this.coord = coord;
			this.oldBox = oldBox;
			this.facing = facing;
			this.context = context;
		}
		
		public LittleActionSawRevert() {
			
		}

		@Override
		public boolean canBeReverted() {
			return true;
		}

		@Override
		public LittleAction revert() throws LittleActionException {
			return new LittleActionSawRevert(context, newCoord, replacedBox, facing.getOpposite());
		}

		@Override
		protected boolean action(EntityPlayer player) throws LittleActionException {
			
			LittleTile tile = getTile(player.world, coord);
			
			if(tile.canSawResizeTile(facing, player))
			{
				tile.te.ensureMinContext(context);
				if(tile.getContext() != context)
				{
					oldBox.convertTo(context, tile.getContext());
					context = tile.getContext();
				}
				
				double amount = Math.abs(oldBox.getPercentVolume(context)-tile.box.getPercentVolume(tile.getContext()));
				BlockIngredients ingredients = new BlockIngredients();
				LittleTilePreview preview = tile.getPreviewTile();
				BlockIngredient ingredient = preview.getBlockIngredient(tile.getContext());
				if(ingredient != null)
				{
					ingredient.value = amount;
					ingredients.addIngredient(ingredient);
					
					ColorUnit unit = null;
					if(preview.hasColor())
					{
						unit = ColorUnit.getColors(preview.getColor());
						unit.scaleLoose(amount);
					}
					
					if(oldBox.getVolume() < tile.box.getVolume())
						addIngredients(player, ingredients, unit);
					else
						drainIngredients(player, ingredients, unit);
				}
				
				replacedBox = tile.box.copy();
				//replacedBox.addOffset(tile.te.getPos());
				tile.box = oldBox.copy();
				
				tile.te.convertToSmallest();
				tile.te.updateBlock();
				
				newCoord = new LittleTileIdentifierAbsolute(tile);
				return true;
			}
			
			return false;
		}

		@Override
		public void writeBytes(ByteBuf buf) {
			writeAbsoluteCoord(coord, buf);
			writeLittleBox(oldBox, buf);
			writeFacing(buf, facing);
			writeContext(context, buf);
		}

		@Override
		public void readBytes(ByteBuf buf) {
			coord = readAbsoluteCoord(buf);
			oldBox = readLittleBox(buf);
			facing = readFacing(buf);
			context = readContext(buf);
		}
	}
}
