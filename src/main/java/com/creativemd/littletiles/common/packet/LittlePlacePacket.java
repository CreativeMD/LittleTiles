package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.PlacementHelper.PositionResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittlePlacePacket extends CreativeCorePacket{
	
	public LittlePlacePacket()
	{
		
	}
	
	public LittlePlacePacket(PositionResult position, boolean centered, boolean fixed, boolean forced)
	{
		this.position = position;
		this.centered = centered;
		this.fixed = fixed;
		this.forced = forced;
	}
	
	public PositionResult position;
	public boolean centered;
	public boolean fixed;
	public boolean forced;
	
	@Override
	public void writeBytes(ByteBuf buf) {
		position.writeToBytes(buf);
		buf.writeBoolean(centered);
		buf.writeBoolean(fixed);
		buf.writeBoolean(forced);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		position = PositionResult.readFromBytes(buf);
		this.centered = buf.readBoolean();
		this.fixed = buf.readBoolean();
		this.forced = buf.readBoolean();
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		
		if(!LittleBlockPacket.isAllowedToInteract(player, position.pos, true, EnumFacing.EAST))
		{
			IBlockState state = player.world.getBlockState(position.pos);
			player.world.notifyBlockUpdate(position.pos, state, state, 3);
			return ;
		}
		
		if(PlacementHelper.isLittleBlock(stack))
		{
			((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).placeBlockAt(player, stack, player.world, position, centered, fixed, forced);
			
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
			playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
			
		}
	}

}
