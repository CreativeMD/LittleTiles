package com.creativemd.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.old.LittleSize;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTiles extends ItemBlock implements ILittleTile, ICreativeRendered {
	
	public ItemBlockTiles(Block block, ResourceLocation location) {
		super(block);
		setUnlocalizedName(location.getResourcePath());
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		String result = super.getItemStackDisplayName(stack);
		if (stack.hasTagCompound()) {
			LittleVec size = stack.getTagCompound().hasKey("size") ? LittleSize.loadSize("size", stack.getTagCompound()) : LittleBox.loadBox("bBox", stack.getTagCompound()).getSize();
			result += " (x=" + size.x + ",y=" + size.y + ",z=" + size.z + ")";
		}
		return result;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.hasTagCompound()) {
			Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
			if (block != null && !(block instanceof BlockAir))
				return new ItemStack(block, 1, stack.getTagCompound().getInteger("meta")).getUnlocalizedName();
		}
		return super.getUnlocalizedName(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		LittlePreviews previews = new LittlePreviews(getPreviewsContext(stack));
		previews.addWithoutCheckingPreview(LittleTileRegistry.loadPreview(stack.getTagCompound()));
		return previews;
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		if (previews.size() > 0) {
			LittlePreview preview = previews.get(0);
			NBTTagCompound nbt = preview.getTileData().copy();
			previews.getContext().set(nbt);
			LittleBox tempBox = preview.box;
			preview.box = preview.box.copy();
			preview.box.sub(preview.box.getMinVec());
			preview.writeToNBT(nbt);
			preview.box = tempBox;
			stack.setTagCompound(nbt);
		} else
			stack.setTagCompound(new NBTTagCompound());
	}
	
	public static ItemStack getStackFromPreview(LittleGridContext context, LittlePreview preview) {
		ItemStack stack = new ItemStack(LittleTiles.blockTileNoTicking);
		NBTTagCompound nbt = preview.getTileData().copy();
		
		// preview.size.writeToNBT("size", nbt);
		preview.writeToNBT(nbt);
		
		// if(preview.isCustomPreview() && !preview.getTypeID().equals(""))
		// nbt.setString("type", preview.getTypeID());
		context.set(nbt);
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public static List<LittleRenderingCube> getItemRenderingCubes(ItemStack stack) {
		ArrayList<LittleRenderingCube> cubes = new ArrayList<LittleRenderingCube>();
		if (stack != null && stack.hasTagCompound()) {
			if (stack.getTagCompound().hasKey("size")) {
				Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
				int meta = stack.getTagCompound().getInteger("meta");
				LittleVec size = new LittleVec("size", stack.getTagCompound());
				if (!(block instanceof BlockAir)) {
					LittleRenderingCube cube = new LittleBox(0, 0, 0, size.x, size.y, size.z).getRenderingCube(LittleGridContext.get(), block, meta);
					if (stack.getTagCompound().hasKey("color"))
						cube.color = stack.getTagCompound().getInteger("color");
					cubes.add(cube);
				}
			} else {
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				LittlePreview preview = LittleTileRegistry.loadPreview(stack.getTagCompound());
				cubes.add((LittleRenderingCube) preview.getCubeBlock(iTile.getPreviewsContext(stack)));
			}
		}
		return cubes;
	}
	
	@Override
	public List<? extends RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if (stack != null)
			return getItemRenderingCubes(stack);
		return new ArrayList<>();
	}
	
	@Override
	public PlacementMode getPlacementMode(ItemStack stack) {
		return ItemMultiTiles.currentMode;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
		return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
			
			@Override
			public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
				ItemMultiTiles.currentContext = context;
				ItemMultiTiles.currentMode = mode;
			}
			
		};
	}
	
	@Override
	public LittleGridContext getPositionContext(ItemStack stack) {
		return ItemMultiTiles.currentContext;
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack) {
		return true;
	}
	
}
