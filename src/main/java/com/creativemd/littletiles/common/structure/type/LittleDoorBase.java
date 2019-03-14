package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.packet.LittleDoorPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.type.LittleAdvancedDoor.LittleAdvancedDoorParser;
import com.creativemd.littletiles.common.structure.type.LittleAxisDoor.LittleAxisDoorParser;
import com.creativemd.littletiles.common.structure.type.LittleSlidingDoor.LittleSlidingDoorParser;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleDoorBase extends LittleStructure {
	
	public LittleDoorBase(LittleStructureType type) {
		super(type);
	}
	
	public int duration = 50;
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		if (nbt.hasKey("duration"))
			duration = nbt.getInteger("duration");
		else
			duration = 50;
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setInteger("duration", duration);
	}
	
	public boolean place(World world, EntityPlayer player, LittleAbsolutePreviewsStructure previews, DoorController controller, UUID uuid, StructureAbsolute absolute) {
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		previews.getPlacePreviews(placePreviews, null, true, LittleTileVec.ZERO);
		
		HashMap<BlockPos, PlacePreviews> splitted = LittleActionPlaceStack.getSplittedTiles(previews.context, placePreviews, previews.pos);
		if (LittleActionPlaceStack.canPlaceTiles(player, world, splitted, PlacementMode.all.getCoordsToCheck(splitted, previews.pos), PlacementMode.all)) {
			ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
			SubWorld fakeWorld = SubWorld.createFakeWorld(world);
			LittleActionPlaceStack.placeTilesWithoutPlayer(fakeWorld, previews.context, splitted, previews.getStructure(), PlacementMode.all, previews.pos, null, null, null, null);
			
			controller.activator = player;
			
			if (world.isRemote) {
				controller.markWaitingForApprove();
				
				for (TileEntityLittleTiles te : tiles.keySet())
					if (te.waitingAnimation != null)
						te.clearWaitingAnimations();
			}
			
			LittleStructure newDoor = previews.getStructure();
			
			if (parent != null) {
				LittleStructure parentStructure = parent.getStructure(world);
				parentStructure.updateChildConnection(parent.getChildID(), newDoor);
				newDoor.updateParentConnection(parent.getChildID(), parentStructure);
			}
			
			EntityAnimation animation = new EntityAnimation(world, fakeWorld, controller, previews.pos, uuid, absolute);
			world.spawnEntity(animation);
			return true;
		}
		
		return false;
	}
	
	public boolean activate(World world, @Nullable EntityPlayer player, BlockPos pos, @Nullable LittleTile tile) {
		if (!hasLoaded() || !loadChildren()) {
			player.sendStatusMessage(new TextComponentTranslation("Cannot interact with door! Not all tiles are loaded!"), true);
			return false;
		}
		
		if (isChildMoving()) {
			player.sendStatusMessage(new TextComponentTranslation("A child is still in motion!"), true);
			return false;
		}
		
		UUID uuid = UUID.randomUUID();
		if (world.isRemote)
			PacketHandler.sendPacketToServer(new LittleDoorPacket(tile != null ? tile : getMainTile(), uuid));
		
		openDoor(world, player, uuid);
		
		return true;
	}
	
	public void openDoor(World world, @Nullable EntityPlayer player, UUID uuid) {
		HashMapList<TileEntityLittleTiles, LittleTile> tempTiles = getAllTiles(new HashMapList<>());
		HashMap<TileEntityLittleTiles, LittleGridContext> tempContext = new HashMap<>();
		
		StructureAbsolute absolute = getAbsoluteAxis();
		
		for (TileEntityLittleTiles te : tempTiles.keySet()) {
			tempContext.put(te, te.getContext());
		}
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
			entry.getKey().preventUpdate = true;
			entry.getKey().removeTiles(entry.getValue());
			entry.getKey().preventUpdate = false;
		}
		
		if (tryToPlacePreviews(world, player, uuid, absolute)) {
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
				entry.getKey().updateTiles();
			}
			return;
		}
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tempTiles.entrySet()) {
			entry.getKey().convertTo(tempContext.get(entry.getKey()));
			entry.getKey().addTiles(entry.getValue());
		}
	}
	
	public abstract boolean tryToPlacePreviews(World world, EntityPlayer player, UUID uuid, StructureAbsolute absolute);
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (world.isRemote) {
			activate(world, player, pos, tile);
			action.preventInteraction = true;
		}
		return true;
	}
	
	public abstract StructureAbsolute getAbsoluteAxis();
	
	public static void initDoors() {
		LittleStructureRegistry.registerStructureType("door", "door", LittleAxisDoor.class, LittleStructureAttribute.NONE, LittleAxisDoorParser.class);
		LittleStructureRegistry.registerStructureType("slidingDoor", "door", LittleSlidingDoor.class, LittleStructureAttribute.NONE, LittleSlidingDoorParser.class);
		LittleStructureRegistry.registerStructureType("advancedDoor", "door", LittleAdvancedDoor.class, LittleStructureAttribute.NONE, LittleAdvancedDoorParser.class);
	}
	
	public static abstract class LittleDoorBaseParser extends LittleStructureGuiParser {
		
		public LittleDoorBaseParser(GuiParent parent) {
			super(parent);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(ItemStack stack, LittleStructure structure) {
			
			parent.controls.add(new GuiLabel("Duration:", 90, 122));
			parent.controls.add(new GuiSteppedSlider("duration_s", 140, 122, 50, 6, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 50, 1, 500));
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleDoorBase parseStructure(ItemStack stack) {
			GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
			return parseStructure((int) slider.value);
		}
		
		@SideOnly(Side.CLIENT)
		public abstract LittleDoorBase parseStructure(int duration);
		
	}
}
