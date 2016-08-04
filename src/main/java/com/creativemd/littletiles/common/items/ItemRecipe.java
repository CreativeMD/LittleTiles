package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.gui.SubContainerStructure;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRecipe extends Item implements ITilesRenderer, IGuiCreator{
	
	public ItemRecipe(){
		setCreativeTab(CreativeTabs.TOOLS);
		hasSubtypes = true;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
		if(!world.isRemote && !player.isSneaking() && stack.hasTagCompound() && !stack.getTagCompound().hasKey("x"))
			GuiHandler.openGuiItem(player, world);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
    }
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(player.isSneaking())
		{
			if(!world.isRemote)
				stack.setTagCompound(null);
			return EnumActionResult.SUCCESS;
		}
		
		//onItemRightClick(stack, world, player);
		/*if(stack.stackTagCompound != null && !stack.stackTagCompound.hasKey("x"))
		{
			if(player.capabilities.isCreativeMode)
			{
				ItemStack multiTiles = new ItemStack(LittleTiles.multiTiles);
				multiTiles.stackTagCompound = stack.stackTagCompound;
				WorldUtils.dropItem(player, multiTiles);
				return true;
			}
			return false;
		}*/
		
		//if(stack.stackTagCompound == null)
			//stack.stackTagCompound = new NBTTagCompound();
		
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("x"))
		{
			if(!world.isRemote)
			{
				int firstX = stack.getTagCompound().getInteger("x");
				int firstY = stack.getTagCompound().getInteger("y");
				int firstZ = stack.getTagCompound().getInteger("z");
				int minX = Math.min(firstX, pos.getX());
				int maxX = Math.max(firstX, pos.getX());
				int minY = Math.min(firstY, pos.getY());
				int maxY = Math.max(firstY, pos.getY());
				int minZ = Math.min(firstZ, pos.getZ());
				int maxZ = Math.max(firstZ, pos.getZ());
				
				ArrayList<LittleTile> tiles = new ArrayList<LittleTile>();
				
				stack.getTagCompound().removeTag("x");
				stack.getTagCompound().removeTag("y");
				stack.getTagCompound().removeTag("z");
				
				for (int posX = minX; posX <= maxX; posX++) {
					for (int posY = minY; posY <= maxY; posY++) {
						for (int posZ = minZ; posZ <= maxZ; posZ++) {
							TileEntity tileEntity = world.getTileEntity(new BlockPos(posX, posY, posZ));
							if(tileEntity instanceof TileEntityLittleTiles)
							{
								LittleTileVec offset = new LittleTileVec((posX-minX)*16, (posY-minY)*16, (posZ-minZ)*16);
								TileEntityLittleTiles littleEntity = (TileEntityLittleTiles) tileEntity;
								TileList<LittleTile> teTiles = littleEntity.getTiles();
								for (int i = 0; i < teTiles.size(); i++) {
									LittleTile tile = teTiles.get(i).copy();
									for (int j = 0; j < tile.boundingBoxes.size(); j++) {
										tile.boundingBoxes.get(j).addOffset(offset);
									}
									tiles.add(tile);
								}
							}
						}
					}
				}
				player.addChatMessage(new TextComponentTranslation("Second position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
				saveTiles(world, tiles, stack);
			}
			return EnumActionResult.SUCCESS;
		}else if(stack.hasTagCompound()){
			if(!world.isRemote)
			{
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setInteger("x", pos.getX());
				stack.getTagCompound().setInteger("y", pos.getZ());
				stack.getTagCompound().setInteger("z", pos.getZ());
				player.addChatMessage(new TextComponentTranslation("First position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
			}
			return EnumActionResult.SUCCESS;
		}
        return EnumActionResult.PASS;
    }
	
	public static void flipPreview(ItemStack stack, EnumFacing direction)
	{
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTilePreview.flipPreview(nbt, direction);
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
	}
	
	public static void rotatePreview(ItemStack stack, EnumFacing direction)
	{
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTilePreview.rotatePreview(nbt, direction);
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
	}
	
	public static ArrayList<LittleTilePreview> getPreview(ItemStack stack)
	{
		ArrayList<LittleTilePreview> result = new ArrayList<LittleTilePreview>();
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTilePreview preview = LittleTilePreview.getPreviewFromNBT(nbt);
			if(preview != null)
				result.add(preview);
		}
		return result;
	}
	
	public static LittleTileSize getSize(ItemStack stack)
	{
		ArrayList<LittleTilePreview> tiles = getPreview(stack);
		byte minX = LittleTile.maxPos;
		byte minY = LittleTile.maxPos;
		byte minZ = LittleTile.maxPos;
		byte maxX = LittleTile.minPos;
		byte maxY = LittleTile.minPos;
		byte maxZ = LittleTile.minPos;
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			minX = (byte) Math.min(minX, tile.box.minX);
			minY = (byte) Math.min(minY, tile.box.minY);
			minZ = (byte) Math.min(minZ, tile.box.minZ);
			maxX = (byte) Math.max(maxX, tile.box.maxX);
			maxY = (byte) Math.max(maxY, tile.box.maxY);
			maxZ = (byte) Math.max(maxZ, tile.box.maxZ);
		}
		return new LittleTileSize(maxX-minX, maxY-minY, maxZ-minZ);
	}
	
	public static ArrayList<LittleTile> loadTiles(TileEntityLittleTiles te, ItemStack stack)
	{
		ArrayList<LittleTile> result = new ArrayList<LittleTile>();
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(te, te.getWorld(), nbt);
			//if(tile != null && tile.isValid())
			result.add(tile);
		}
		return result;
	}
	
	public static void saveTiles(World world, ArrayList<LittleTile> tiles, ItemStack stack)
	{
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("tiles", tiles.size());
		for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			tiles.get(i).boundingBoxes.get(0).writeToNBT("bBox", nbt);
			tiles.get(i).saveTile(nbt);
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
	}
	
	public static ArrayList<CubeObject> getCubes(ItemStack stack)
	{
		ArrayList<LittleTilePreview> preview = getPreview(stack);
		ArrayList<CubeObject> cubes = new ArrayList<CubeObject>();
		for (int i = 0; i < preview.size(); i++) {
			cubes.add(preview.get(i).getCubeBlock());
		}
		return cubes;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(stack.hasTagCompound())
		{
			if(stack.getTagCompound().hasKey("x"))
				list.add("First pos: x=" + stack.getTagCompound().getInteger("x") + ",y=" + stack.getTagCompound().getInteger("y")+ ",z=" + stack.getTagCompound().getInteger("z"));
			else{
				String id = "none";
				if(stack.getTagCompound().hasKey("structure"))
					id = stack.getTagCompound().getCompoundTag("structure").getString("id");
				list.add("structure: " + id);
				list.add("contains " + stack.getTagCompound().getInteger("tiles") + " tiles");
			}
		}
	}

	@Override
	public boolean hasBackground(ItemStack stack) {
		return true;
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack) {
		if(stack.hasTagCompound() && !stack.getTagCompound().hasKey("x"))
			return getCubes(stack);
		return new ArrayList<CubeObject>();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiStructure(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerStructure(player, stack);
	}
}
