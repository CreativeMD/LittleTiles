package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.packet.LittleDoorPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class LittleDoorActivator extends LittleStructure implements ILittleDoor {
	
	public LittleDoorActivator(LittleStructureType type) {
		super(type);
	}
	
	public PairList<Integer, Integer> childActivation;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		int[] array = nbt.getIntArray("activator");
		childActivation = new PairList<>();
		for (int i = 0; i < array.length; i += 2) {
			childActivation.add(array[i], array[i + 1]);
		}
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		int[] array = new int[childActivation.size() * 2];
		int i = 0;
		for (Pair<Integer, Integer> pair : childActivation) {
			array[i * 2] = pair.key;
			array[i * 2 + 1] = pair.value;
			i++;
		}
		nbt.setIntArray("activator", array);
	}
	
	public boolean doesActivateChild(int id) {
		return childActivation.containsKey(id);
	}
	
	public boolean activate(World world, @Nullable EntityPlayer player, BlockPos pos, @Nullable LittleTile tile) {
		if (!hasLoaded() || !loadChildren()) {
			player.sendStatusMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"), true);
			return false;
		}
		
		UUID uuid = UUID.randomUUID();
		if (world.isRemote)
			PacketHandler.sendPacketToServer(new LittleDoorPacket(tile != null ? tile : getMainTile(), uuid));
		
		openDoor(world, player, new UUIDSupplier(uuid));
		
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (world.isRemote) {
			activate(world, player, pos, tile);
			action.preventInteraction = true;
		}
		return true;
	}
	
	@Override
	public void openDoor(World world, @Nullable EntityPlayer player, UUIDSupplier uuid) {
		for (Pair<Integer, Integer> pair : childActivation) {
			IStructureChildConnector child = children.get(pair.key);
			if (child == null || child.isLinkToAnotherWorld())
				continue;
			LittleStructure childStructure = child.getStructure(world);
			if (childStructure == null || !(childStructure instanceof ILittleDoor))
				continue;
			
			((ILittleDoor) childStructure).openDoor(world, player, uuid);
		}
	}
	
	public static class LittleDoorActivatorParser extends LittleStructureGuiParser {
		
		public LittleDoorActivatorParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		public String getDisplayName(LittlePreviews previews, int childId) {
			String name = previews.getStructureName();
			if (name == null)
				if (previews.hasStructure())
					name = previews.getStructureId();
				else
					name = "none";
			return name + " " + childId;
		}
		
		public List<Integer> possibleChildren;
		
		@Override
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			LittleDoorActivator activator = structure instanceof LittleDoorActivator ? (LittleDoorActivator) structure : null;
			possibleChildren = new ArrayList<>();
			int i = 0;
			int added = 0;
			for (LittlePreviews child : previews.getChildren()) {
				if (ILittleDoor.class.isAssignableFrom(LittleStructureRegistry.getStructureClass(child.getStructureId()))) {
					parent.controls.add(new GuiCheckBox("" + i, getDisplayName(child, i), 0, added * 20, activator != null && activator.doesActivateChild(i)));
					possibleChildren.add(i);
					added++;
				}
				i++;
			}
		}
		
		@Override
		public LittleStructure parseStructure(LittlePreviews previews) {
			LittleDoorActivator activator = createStructure(LittleDoorActivator.class);
			activator.childActivation = new PairList<>();
			for (Integer integer : possibleChildren) {
				GuiCheckBox box = (GuiCheckBox) parent.get("" + integer);
				if (box != null && box.value)
					activator.childActivation.add(integer, 0);
			}
			return activator;
		}
		
	}
	
}
