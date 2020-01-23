package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemLittleGrabber;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.GrabberMode;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import io.netty.buffer.ByteBuf;
import mod.flatcoloredblocks.block.BlockFlatColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleVanillaBlockPacket extends CreativeCorePacket {
	
	public static enum VanillaBlockAction {
		
		COLOR_TUBE {
			
			public boolean isFlatColoredBlocksInstalled() {
				try {
					return Class.forName("mod.flatcoloredblocks.block.BlockFlatColored") != null;
				} catch (ClassNotFoundException e) {
					
				}
				return false;
			}
			
			boolean flatColoredBlocks = isFlatColoredBlocksInstalled();
			
			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state) {
				int color = ColorUtils.WHITE;
				
				if (flatColoredBlocks && state.getBlock() instanceof BlockFlatColored)
					color = ((BlockFlatColored) state.getBlock()).colorFromState(state);
				
				ItemColorTube.setColor(player.getHeldItemMainhand(), color);
			}
		},
		CHISEL {
			
			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state) {
				if (LittleAction.isBlockValid(state)) {
					LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
					tile.box = new LittleBox(0, 0, 0, LittleGridContext.get().size, LittleGridContext.get().size, LittleGridContext.get().size);
					ItemLittleChisel.setPreview(player.getHeldItemMainhand(), tile.getPreviewTile());
				}
			}
			
		},
		GRABBER {
			
			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state) {
				if (LittleAction.isBlockValid(state)) {
					ItemStack stack = player.getHeldItemMainhand();
					GrabberMode mode = ItemLittleGrabber.getMode(stack);
					mode.vanillaBlockAction(world, stack, pos, state);
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
		
		if (!player.world.isRemote) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
			playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
		}
	}
	
}
