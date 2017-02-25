package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleBlockVanillaPacket extends CreativeCorePacket {
	
	public BlockPos blockPos;
	public Vec3d pos;
	public Vec3d look;
	
	public LittleBlockVanillaPacket()
	{
		
	}
	
	public LittleBlockVanillaPacket(BlockPos blockPos, EntityPlayer player)
	{
		this.blockPos = blockPos;
		this.pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		this.look = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, blockPos);
		writeVec3d(pos, buf);
		writeVec3d(look, buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
		pos = readVec3d(buf);
		look = readVec3d(buf);		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		World world = player.worldObj;
		IBlockState state = world.getBlockState(blockPos);
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if(SubContainerHammer.isBlockValid(state.getBlock()) && stack != null && stack.getItem() instanceof ISpecialBlockSelector)
		{
			RayTraceResult trace = new AxisAlignedBB(blockPos).calculateIntercept(pos, look);
			LittleTileBox box = ((ISpecialBlockSelector) stack.getItem()).getBox(world, blockPos, state, player, trace);
			
			world.setBlockState(blockPos, LittleTiles.blockTile.getDefaultState());
			TileEntityLittleTiles te = (TileEntityLittleTiles) world.getTileEntity(blockPos);
			
			LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
			tile.boundingBoxes.add(new LittleTileBox(0,0,0,LittleTile.maxPos,LittleTile.maxPos,LittleTile.maxPos));

			tile.te = te;
			tile.place();
			
			world.playSound((EntityPlayer)null, blockPos, tile.getSound().getPlaceSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
			
			te.removeBoxFromTiles(box);
			if(!player.capabilities.isCreativeMode)
			{
				tile.boundingBoxes.clear();
				tile.boundingBoxes.add(box.copy());
				WorldUtils.dropItem(player, tile.getDrops());
			}
		}
	}

}
