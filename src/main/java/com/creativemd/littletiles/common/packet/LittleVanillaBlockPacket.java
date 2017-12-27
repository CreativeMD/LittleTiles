package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemLittleGrabber;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.GrabberMode;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.PlacePreviewMode;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import mod.flatcoloredblocks.block.BlockFlatColored;
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
		
		COLOR_TUBE {
			
			public boolean isFlatColoredBlocksInstalled()
			{
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
				
				if(flatColoredBlocks && state.getBlock() instanceof BlockFlatColored)
					color = ((BlockFlatColored) state.getBlock()).colorFromState(state);
				
				ItemColorTube.setColor(player.getHeldItemMainhand(), color);
			}
		},
		CHISEL{

			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state) {
				if(SubContainerGrabber.isBlockValid(state.getBlock()))
				{
					LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
					tile.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
					ItemLittleChisel.setPreview(player.getHeldItemMainhand(), tile.getPreviewTile());
				}
			}
			
		},
		GRABBER{

			@Override
			public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state) {
				if(SubContainerGrabber.isBlockValid(state.getBlock()))
				{
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
		
		if(!player.world.isRemote)
		{
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
			playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
		}
	}

}
