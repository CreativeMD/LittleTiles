package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleVanillaBlockPacket extends CreativeCorePacket {
	
	public static enum VanillaBlockAction {
		
		CHISEL{

			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state) {
				if(SubContainerHammer.isBlockValid(state.getBlock()))
				{
					ItemLittleChisel.setBlockState(player.getHeldItemMainhand(), state);
					ItemLittleChisel.setColor(player.getHeldItemMainhand(), ColorUtils.WHITE);
				}
			}
			
		};
		
		private VanillaBlockAction() {
			
		}
		
		public abstract void action(World world, EntityPlayer player, BlockPos pos, IBlockState state);
		
	}
	
	public BlockPos pos;
	public VanillaBlockAction action;
	
	public LittleVanillaBlockPacket(BlockPos pos, VanillaBlockAction action) {
		this.action = action;
		this.pos = pos;
	}
	
	public LittleVanillaBlockPacket() {
		
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		buf.writeInt(action.ordinal());
	}

	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		action = VanillaBlockAction.values()[buf.readInt()];
	}

	@Override
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		
		action.action(player.world, player, pos, player.world.getBlockState(pos));
		
		if(!player.world.isRemote)
		{
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
			playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
		}
	}

}
