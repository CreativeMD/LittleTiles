package com.creativemd.littletiles.common.items;

import java.util.List;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.container.SubContainerUtilityKnife;
import com.creativemd.littletiles.common.gui.SubGuiUtilityKnife;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockVanillaPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUtilityKnife extends Item implements ISpecialBlockSelector, IGuiCreator {
	
	public ItemUtilityKnife()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		list.add("can be used to chisel blocks");
		list.add("mode: " + getMode(stack).name());
		list.add("thickness: " + getThickness(stack));
	}

	@Override
	public LittleTileBox getBox(TileEntityLittleTiles te, LittleTile tile, BlockPos pos, EntityPlayer player, RayTraceResult result) {
		if(tile.isStructureBlock)
			return null;
		
		double posX = result.hitVec.xCoord - pos.getX();
		double posY = result.hitVec.yCoord - pos.getY();
		double posZ = result.hitVec.zCoord - pos.getZ();
		LittleTileVec vec = new LittleTileVec((int)PlacementHelper.round(posX*LittleTile.gridSize), (int)PlacementHelper.round(posY*LittleTile.gridSize), (int)PlacementHelper.round(posZ*LittleTile.gridSize));
		
		//LittleTileVec vec = PlacementHelper.getPosition(player.world, result).hit;
		
		if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
			vec.subVec(new LittleTileVec(result.sideHit));
		
		return getMode(player.getHeldItemMainhand()).getBox(vec, getThickness(player.getHeldItemMainhand()), result.sideHit);
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
    {
		World world = player.world;
		ItemStack stack = player.getHeldItemMainhand();
		IBlockState state = world.getBlockState(pos);
		if(SubContainerHammer.isBlockValid(state.getBlock()) && stack != null && stack.getItem() instanceof ItemUtilityKnife && (state.getBlockHardness(world, pos) >= 0 || player.isCreative()))
		{
			if(world.isRemote)
				destroyBlock(stack, player, world, pos, state);
			return true;
		}
        return false;
    }
	
	@SideOnly(Side.CLIENT)
	public void destroyBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState state)
	{
		LittleTileBox box = ((ISpecialBlockSelector) stack.getItem()).getBox(world, pos, state, player, Minecraft.getMinecraft().objectMouseOver);
		
		world.setBlockState(pos, LittleTiles.blockTile.getDefaultState());
		TileEntityLittleTiles te = (TileEntityLittleTiles) world.getTileEntity(pos);
		
		LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
		tile.boundingBoxes.add(new LittleTileBox(0,0,0,LittleTile.maxPos,LittleTile.maxPos,LittleTile.maxPos));
		tile.te = te;
		tile.place();
		te.removeBoxFromTiles(box);
		if(!player.capabilities.isCreativeMode)
		{
			tile.boundingBoxes.clear();
			tile.boundingBoxes.add(box.copy());
		}
		
		PacketHandler.sendPacketToServer(new LittleBlockVanillaPacket(pos, player));
		
	}

	@Override
	public LittleTileBox getBox(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			RayTraceResult result) {
		double posX = result.hitVec.xCoord - pos.getX();
		double posY = result.hitVec.yCoord - pos.getY();
		double posZ = result.hitVec.zCoord - pos.getZ();
		
		LittleTileVec vec = new LittleTileVec((int)PlacementHelper.round(posX*LittleTile.gridSize), (int)PlacementHelper.round(posY*LittleTile.gridSize), (int)PlacementHelper.round(posZ*LittleTile.gridSize));
		
		//LittleTileVec vec = PlacementHelper.getPosition(player.world, result).hit;
		if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE)
			vec.subVec(new LittleTileVec(result.sideHit));
		
		return getMode(player.getHeldItemMainhand()).getBox(vec, getThickness(player.getHeldItemMainhand()), result.sideHit);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		if(hand == EnumHand.OFF_HAND)
			return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand)); 
		if(!world.isRemote)
			GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiUtilityKnife(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos,
			IBlockState state) {
		return new SubContainerUtilityKnife(player, stack);
	}
	
	public static int getThickness(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		return Math.max(1, stack.getTagCompound().getInteger("thick"));
	}
	
	public static UtilityKnifeMode getMode(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		int mode = stack.getTagCompound().getInteger("mode");
		if(mode >= 0 && mode < UtilityKnifeMode.values().length)
			return UtilityKnifeMode.values()[mode];
		return UtilityKnifeMode.Cube;
	}
	
	public static enum UtilityKnifeMode {
		
		Cube {
			@Override
			public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side) {
				LittleTileVec offset = new LittleTileVec(side);
				offset.scale((int) (thickness-1)/2);
				vec.subVec(offset);
				if(side.getAxisDirection() == AxisDirection.NEGATIVE && (thickness & 1) == 0)
					vec.subVec(new LittleTileVec(side));
				LittleTileBox box = new LittleTileBox(vec, new LittleTileSize(thickness, thickness, thickness));
				box.makeItFitInsideBlock();
				return box;
			}
		},
		Bar {
			@Override
			public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side) {
				LittleTileBox box = UtilityKnifeMode.Cube.getBox(vec, thickness, side);
				
				switch(side.getAxis())
				{
				case X:
					box.minX = 0;
					box.maxX = LittleTile.gridSize;
					break;
				case Y:
					box.minY = 0;
					box.maxY = LittleTile.gridSize;
					break;
				case Z:
					box.minZ = 0;
					box.maxZ = LittleTile.gridSize;
					break;	
				}
				
				return box;
			}
		},
		Plane {
			@Override
			public LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side) {
				LittleTileBox box = UtilityKnifeMode.Cube.getBox(vec, thickness, side);
				
				switch(side.getAxis())
				{
				case X:
					box.minY = 0;
					box.maxY = LittleTile.gridSize;
					box.minZ = 0;
					box.maxZ = LittleTile.gridSize;
					break;
				case Y:
					box.minX = 0;
					box.maxX = LittleTile.gridSize;
					box.minZ = 0;
					box.maxZ = LittleTile.gridSize;
					break;
				case Z:
					box.minX = 0;
					box.maxX = LittleTile.gridSize;
					box.minY = 0;
					box.maxY = LittleTile.gridSize;
					break;	
				}
				
				return box;
			}
		};
		
		
		public abstract LittleTileBox getBox(LittleTileVec vec, int thickness, EnumFacing side);		
		
		public static String[] names()
		{
			String[] names = new String[values().length];
			for (int i = 0; i < values().length; i++) {
				names[i] = values()[i].name();
			}
			return names;
		}
		
	}
	
}
