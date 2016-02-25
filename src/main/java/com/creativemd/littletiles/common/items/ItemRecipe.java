package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.gui.IGuiCreator;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.core.CreativeCore;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.gui.SubContainerStructure;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemRecipe extends Item implements ITilesRenderer, IGuiCreator{
	
	public ItemRecipe(){
		setCreativeTab(CreativeTabs.tabTools);
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    protected String getIconString()
    {
        return LittleTiles.modid + ":LTRecipe";
    }
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if(!world.isRemote && !player.isSneaking() && stack.stackTagCompound != null && !stack.stackTagCompound.hasKey("x"))
		{
			((EntityPlayerMP)player).openGui(CreativeCore.instance, 1, world, (int)player.posX, (int)player.posY, (int)player.posZ);
		}
        return stack;
    }
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if(player.isSneaking())
		{
			if(!world.isRemote)
				stack.stackTagCompound = null;
			return true;
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
		
		if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("x"))
		{
			if(!world.isRemote)
			{
				int firstX = stack.stackTagCompound.getInteger("x");
				int firstY = stack.stackTagCompound.getInteger("y");
				int firstZ = stack.stackTagCompound.getInteger("z");
				int minX = Math.min(firstX, x);
				int maxX = Math.max(firstX, x);
				int minY = Math.min(firstY, y);
				int maxY = Math.max(firstY, y);
				int minZ = Math.min(firstZ, z);
				int maxZ = Math.max(firstZ, z);
				
				ArrayList<LittleTile> tiles = new ArrayList<LittleTile>();
				
				stack.stackTagCompound.removeTag("x");
				stack.stackTagCompound.removeTag("y");
				stack.stackTagCompound.removeTag("z");
				
				for (int posX = minX; posX <= maxX; posX++) {
					for (int posY = minY; posY <= maxY; posY++) {
						for (int posZ = minZ; posZ <= maxZ; posZ++) {
							TileEntity tileEntity = world.getTileEntity(posX, posY, posZ);
							if(tileEntity instanceof TileEntityLittleTiles)
							{
								LittleTileVec offset = new LittleTileVec((posX-minX)*16, (posY-minY)*16, (posZ-minZ)*16);
								TileEntityLittleTiles littleEntity = (TileEntityLittleTiles) tileEntity;
								for (int i = 0; i < littleEntity.tiles.size(); i++) {
									LittleTile tile = littleEntity.tiles.get(i).copy();
									for (int j = 0; j < tile.boundingBoxes.size(); j++) {
										tile.boundingBoxes.get(j).addOffset(offset);
									}
									tiles.add(tile);
								}
							}
						}
					}
				}
				player.addChatMessage(new ChatComponentText("Second position: x=" + x + ",y=" + y + ",z=" + z));
				saveTiles(world, tiles, stack);
			}
			return true;
		}else if(stack.stackTagCompound == null){
			if(!world.isRemote)
			{
				stack.stackTagCompound = new NBTTagCompound();
				stack.stackTagCompound.setInteger("x", x);
				stack.stackTagCompound.setInteger("y", y);
				stack.stackTagCompound.setInteger("z", z);
				player.addChatMessage(new ChatComponentText("First position: x=" + x + ",y=" + y + ",z=" + z));
			}
			return true;
		}
        return false;
    }
	
	public static void flipPreview(ItemStack stack, ForgeDirection direction)
	{
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
			LittleTilePreview.flipPreview(nbt, direction);
			stack.stackTagCompound.setTag("tile" + i, nbt);
		}
	}
	
	public static void rotatePreview(ItemStack stack, ForgeDirection direction)
	{
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
			LittleTilePreview.rotatePreview(nbt, direction);
			stack.stackTagCompound.setTag("tile" + i, nbt);
		}
	}
	
	public static ArrayList<LittleTilePreview> getPreview(ItemStack stack)
	{
		ArrayList<LittleTilePreview> result = new ArrayList<LittleTilePreview>();
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
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
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(te, te.getWorldObj(), nbt);
			//if(tile != null && tile.isValid())
			result.add(tile);
		}
		return result;
	}
	
	public static void saveTiles(World world, ArrayList<LittleTile> tiles, ItemStack stack)
	{
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("tiles", tiles.size());
		for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			tiles.get(i).boundingBoxes.get(0).writeToNBT("bBox", nbt);
			tiles.get(i).saveTile(nbt);
			stack.stackTagCompound.setTag("tile" + i, nbt);
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
		if(stack.stackTagCompound != null)
		{
			if(stack.stackTagCompound.hasKey("x"))
				list.add("First pos: x=" + stack.stackTagCompound.getInteger("x") + ",y=" + stack.stackTagCompound.getInteger("y")+ ",z=" + stack.stackTagCompound.getInteger("z"));
			else{
				String id = "none";
				if(stack.stackTagCompound.hasKey("structure"))
					id = stack.stackTagCompound.getCompoundTag("structure").getString("id");
				list.add("structure: " + id);
				list.add("contains " + stack.stackTagCompound.getInteger("tiles") + " tiles");
			}
		}
	}

	@Override
	public boolean hasBackground(ItemStack stack) {
		return true;
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack) {
		if(stack.stackTagCompound != null && !stack.stackTagCompound.hasKey("x"))
			return getCubes(stack);
		return new ArrayList<CubeObject>();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
		return new SubGuiStructure(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
		return new SubContainerStructure(player, stack);
	}
}
