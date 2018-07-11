package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.combine.BasicCombiner;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class ItemRecipeAdvanced extends ItemRecipe {
	
	public ItemRecipeAdvanced(){
		
	}
	
	@Override
	public void saveRecipe(World world, EntityPlayer player, ItemStack stack, BlockPos second)
	{
		if(stack.getTagCompound().hasKey("scale"))
		{
			super.saveRecipe(world, player, stack, second);
			stack.getTagCompound().removeTag("scale");			
		}else{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("posX", second.getX());
			nbt.setInteger("posY", second.getY());
			nbt.setInteger("posZ", second.getZ());
			GuiHandler.openGui("recipeadvanced", nbt, player);
		}
	}
	
	@Override
	public LittlePreviews saveBlocks(World world, ItemStack stack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		int scale = stack.getTagCompound().getInteger("scale");
		
		LittleGridContext context = LittleGridContext.get(scale);
		
		LittlePreviews previews = new LittlePreviews(context);
		HashMapList<BlockPos, LittleTile> tiles = new HashMapList<>();
		
		MutableBlockPos newPos = new MutableBlockPos();
		
		for (int posX = minX; posX <= maxX; posX++) {
			for (int posY = minY; posY <= maxY; posY++) {
				for (int posZ = minZ; posZ <= maxZ; posZ++) {
					newPos.setPos(posX, posY, posZ);
					IBlockState state = world.getBlockState(newPos);
					if(LittleAction.isBlockValid(state.getBlock()))
					{
						LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
						tile.box = new LittleTileBox(0, 0, 0, 1, 1, 1);
						LittleTileVec offset = new LittleTileVec((posX-minX), (posY-minY), (posZ-minZ));
						tile.box.addOffset(offset);
						tiles.add(offset.getBlockPos(context), tile);
						
					}
				}
			}
		}
		
		for (Entry<BlockPos, ArrayList<LittleTile>> entry : tiles.entrySet()) {
			BasicCombiner.combineTiles(entry.getValue());
			for (LittleTile tile : entry.getValue()) {
				previews.addPreview(null, tile.getPreviewTile(), context);
			}
		}
		
		return previews;
	}
	
	@Override
	public ModelResourceLocation getBackgroundLocation()
	{
		return new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced_background", "inventory");
	}
}
