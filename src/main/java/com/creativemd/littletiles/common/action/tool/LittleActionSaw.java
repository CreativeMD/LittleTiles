package com.creativemd.littletiles.common.action.tool;

import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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

	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos) throws LittleActionException {
		if(tile.canSawResizeTile(moving.sideHit, player))
		{
			LittleTileBox box = null;
			if(player.isSneaking())
				box = tile.boundingBoxes.get(0).shrink(moving.sideHit, toLimit);
			else
				box = tile.boundingBoxes.get(0).expand(moving.sideHit, toLimit);
			
			if(box.isValidBox())
			{
				double amount = Math.abs(box.getSize().getPercentVolume()-tile.boundingBoxes.get(0).getSize().getPercentVolume());
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
				
				if(player.isSneaking())
					addIngredients(player, ingredients, unit);
				else
					drainIngredients(player, ingredients, unit);
				
				if(box.isBoxInsideBlock() && te.isSpaceForLittleTile(box.getBox(), tile))
				{
					tile.boundingBoxes.set(0, box);
					tile.updateCorner();
					te.updateBlock();
				}else if(!box.isBoxInsideBlock()){
					box = box.createOutsideBlockBox(moving.sideHit);
					BlockPos newPos = pos.offset(moving.sideHit);
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
						newTile.boundingBoxes.clear();
						newTile.boundingBoxes.add(box);
						newTile.te = littleTe;
						
						if(littleTe.isSpaceForLittleTile(box))
						{
							newTile.place();
							//littleTe.addTile(newTile);
							littleTe.updateBlock();
						}
					}
				}
				return true;
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
		// TODO Auto-generated method stub
		return null;
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
}
