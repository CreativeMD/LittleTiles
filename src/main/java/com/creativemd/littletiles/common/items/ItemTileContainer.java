package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.container.SubContainerTileContainer;
import com.creativemd.littletiles.common.gui.SubGuiTileContainer;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTileContainer extends Item implements IGuiCreator{
	
	public ItemTileContainer()
	{
		setCreativeTab(LittleTiles.littleTab);
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("can store little pieces and");
		list.add("can be used to provide");
		list.add("needed materials");
	}
	
	public static void saveMap(ItemStack stack, ArrayList<BlockEntry> map)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		int i = 0;
		for (BlockEntry entry : map) {
			if(entry.block != null && !(entry.block instanceof BlockAir))
			{
				nbt.setString("b" + i, Block.REGISTRY.getNameForObject(entry.block).toString());
				nbt.setInteger("m" + i, entry.meta);
				nbt.setDouble("v" + i, entry.value);
				i++;
			}
		}
		nbt.setInteger("count", i);
		stack.setTagCompound(nbt);
	}
	
	public static boolean canStoreRemains(EntityPlayer player)
	{
		ArrayList<BlockEntry> mainList = new ArrayList<BlockEntry>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(stack.isEmpty() && stack.getItem() instanceof ItemTileContainer)
			{
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<BlockEntry> loadMap(EntityPlayer player)
	{
		ArrayList<BlockEntry> mainList = new ArrayList<BlockEntry>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(stack.isEmpty() && stack.getItem() instanceof ItemTileContainer)
			{
				mergeMap(mainList, loadMap(stack));
			}
		}
		return mainList;
	}
			
	public static void mergeMap(ArrayList<BlockEntry> mainMap, ArrayList<BlockEntry> newMap)
	{
		if(newMap == null)
			return ;
		for (BlockEntry entry : newMap) {
			if(mainMap.contains(entry))
			{
				BlockEntry mainEntry = mainMap.get(mainMap.indexOf(entry));
				mainEntry.value += entry.value;
			}
			else
				mainMap.add(entry);
		}
	}
	
	public static ArrayList<BlockEntry> loadMap(ItemStack stack)
	{
		ArrayList<BlockEntry> mainMap = new ArrayList<BlockEntry>();
		if(stack.hasTagCompound())
		{
			int count = stack.getTagCompound().getInteger("count");
			for (int i = 0; i < count; i++) {
				Block block = Block.getBlockFromName(stack.getTagCompound().getString("b" + i));
				int meta = stack.getTagCompound().getInteger("m" + i);
				if(block != null && !(block instanceof BlockAir))
				{
					double value = 0;
					if(stack.getTagCompound().getTag("v" + i) instanceof NBTTagFloat)
						value = stack.getTagCompound().getFloat("v" + i);
					else
						value = stack.getTagCompound().getDouble("v" + i);
					mainMap.add(new BlockEntry(block, meta, value));
				}
			}
		}
		return mainMap;
	}
	
	public static double getVolume(ItemStack stack, Block block, int meta)
	{
		BlockEntry entry = new BlockEntry(block, meta, 0);
		ArrayList<BlockEntry> stackMap = loadMap(stack);
		int index = stackMap.indexOf(entry);
		if(index != -1)
			return stackMap.get(index).value;
		return 0;
	}
	
	public static boolean drainBlock(EntityPlayer player, Block block, int meta, double ammount)
	{
		ArrayList<BlockEntry> mainList = loadMap(player);
		BlockEntry entry = new BlockEntry(block, meta, 0);
		if(mainList.contains(entry) && mainList.get(mainList.indexOf(entry)).value >= ammount)
		{
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if(stack != null && stack.getItem() instanceof ItemTileContainer)
				{
					ArrayList<BlockEntry> stackMap = loadMap(stack);
					if(stackMap.contains(entry))
					{
						double stored = stackMap.get(stackMap.indexOf(entry)).value;
						double drain = Math.min(ammount, stored);
						stored -= drain;
						ammount -= drain;
						stackMap.get(stackMap.indexOf(entry)).value = stored;
						if(stored <= 0)
							stackMap.remove(entry);
						saveMap(stack, stackMap);
					}
				}
			}
			return true;
		}
		return false;
	}
	
	public static boolean drainBlock(ItemStack stack, Block block, int meta, double ammount)
	{
		BlockEntry entry = new BlockEntry(block, meta, 0);
		ArrayList<BlockEntry> stackMap = loadMap(stack);
		if(stackMap.contains(entry) && stackMap.get(stackMap.indexOf(entry)).value >= ammount)
		{
			double stored = stackMap.get(stackMap.indexOf(entry)).value;
			double drain = Math.min(ammount, stored);
			stored -= drain;
			ammount -= drain;
			stackMap.get(stackMap.indexOf(entry)).value = stored;
			if(stored <= 0)
				stackMap.remove(entry);
			saveMap(stack, stackMap);
			return true;
		}
		return false;
	}
	
	public static boolean addBlock(EntityPlayer player, Block block, int meta, double ammount)
	{
		if(player.capabilities.isCreativeMode)
			return true;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(stack != null && stack.getItem() instanceof ItemTileContainer)
			{
				addBlock(stack, block, meta, ammount);
				return true;
			}
		}
		return false;
	}
	
	public static void addBlock(ItemStack stack, Block block, int meta, double ammount)
	{
		
		BlockEntry entry = new BlockEntry(block, meta, ammount);
		ArrayList<BlockEntry> stackMap = loadMap(stack);
		if(stackMap.contains(entry))
			stackMap.get(stackMap.indexOf(entry)).value += ammount;
		else
			stackMap.add(entry);
		saveMap(stack, stackMap);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiTileContainer(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerTileContainer(player, stack, player.inventory.currentItem);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
		return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
	
	public static class BlockEntry {
		public Block block;
		public int meta;
		public double value;
		
		public BlockEntry(Block block, int meta, double value)
		{
			this.block = block;
			this.meta = meta;
			this.value = value;
		}
		
		public ItemStack getItemStack()
		{
			return new ItemStack(block, 1, meta);
		}
		
		public ItemStack getTileItemStack()
		{
			ItemStack stack = new ItemStack(LittleTiles.blockTile);
			NBTTagCompound nbt = new NBTTagCompound();
			new LittleTileSize(1, 1, 1).writeToNBT("size", nbt);
			
			
			LittleTile tile = new LittleTileBlock(block, meta);
			tile.saveTileExtra(nbt);
			nbt.setString("tID", "BlockTileBlock");
			stack.setTagCompound(nbt);
			
			stack.setCount((int) (value/LittleTile.minimumTileSize));
			return stack;
		}
		
		@Override
		public boolean equals(Object object)
		{
			return object instanceof BlockEntry && ((BlockEntry)object).block == this.block && ((BlockEntry)object).meta == this.meta;
		}
	}

}
