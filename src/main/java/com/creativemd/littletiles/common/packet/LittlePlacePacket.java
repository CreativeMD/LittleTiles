package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittlePlacePacket extends CreativeCorePacket{
	
	public LittlePlacePacket()
	{
		
	}
	
	public LittlePlacePacket(ItemStack stack, Vec3d playerPos, Vec3d hitVec, BlockPos pos, EnumFacing side, boolean customPlacement) //, int direction, int direction2)
	{
		this.stack = stack;
		this.playerPos = playerPos;
		this.hitVec = hitVec;
		this.pos = pos;
		this.side = side;
		this.customPlacement = customPlacement;
		//this.direction = direction;
		//this.direction2 = direction2;
	}
	
	public ItemStack stack;
	public Vec3d hitVec;
	public Vec3d playerPos;
	public BlockPos pos;
	public EnumFacing side;
	public boolean customPlacement;
	//public int direction;
	//public int direction2;
	
	@Override
	public void writeBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, stack);
		writeVec3(playerPos, buf);
		writeVec3(hitVec, buf);
		writePos(buf, pos);
		writeFacing(buf, side);
		buf.writeBoolean(customPlacement);
		//buf.writeInt(direction);
		//buf.writeInt(direction2);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		stack = ByteBufUtils.readItemStack(buf);
		playerPos = readVec3(buf);
		hitVec = readVec3(buf);
		pos = readPos(buf);
		this.side = readFacing(buf);
		this.customPlacement = buf.readBoolean();
		//this.direction = buf.readInt();
		//this.direction2 = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		if(PlacementHelper.isLittleBlock(stack))
		{
			PlacementHelper helper = PlacementHelper.getInstance(player); //new PlacementHelper(player, x, y, z);
			//helper.side = side;
			
			((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).placeBlockAt(player, stack, player.worldObj, playerPos, hitVec, helper, pos, side, customPlacement); //, ForgeDirection.getOrientation(direction), ForgeDirection.getOrientation(direction2));
			
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
			playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
			
		}
	}

}
