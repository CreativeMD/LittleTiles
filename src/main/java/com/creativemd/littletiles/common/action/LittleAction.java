package com.creativemd.littletiles.common.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.mc.PlayerUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig.NotAllowedToPlaceColorException;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.event.ActionEvent;
import com.creativemd.littletiles.common.event.ActionEvent.ActionType;
import com.creativemd.littletiles.common.item.ItemPremadeStructure;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.mod.coloredlights.ColoredLightsManager;
import com.creativemd.littletiles.common.packet.LittleBlockUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleBlocksUpdatePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierAbsolute;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.ingredient.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage.ActionMessageObjectType;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleAction extends CreativeCorePacket {
	
	private static List<LittleAction> lastActions = new ArrayList<>();
	
	private static int index = 0;
	
	@SideOnly(Side.CLIENT)
	public static boolean isUsingSecondMode(EntityPlayer player) {
		if (LittleTiles.CONFIG.building.useALTForEverything)
			return GuiScreen.isAltKeyDown();
		if (LittleTiles.CONFIG.building.useAltWhenFlying)
			return player.capabilities.isFlying ? GuiScreen.isAltKeyDown() : player.isSneaking();
		return player.isSneaking();
	}
	
	public static void rememberAction(LittleAction action) {
		if (!action.canBeReverted())
			return;
		
		if (index > 0) {
			if (index < lastActions.size())
				lastActions = lastActions.subList(index, lastActions.size() - 1);
			else
				lastActions = new ArrayList<>();
		}
		
		index = 0;
		
		if (lastActions.size() == LittleTiles.CONFIG.building.maxSavedActions)
			lastActions.remove(LittleTiles.CONFIG.building.maxSavedActions - 1);
		
		lastActions.add(0, action);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean undo() throws LittleActionException {
		if (lastActions.size() > index) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			
			LittleAction reverted = lastActions.get(index).revert();
			
			if (reverted == null)
				throw new LittleActionException("action.revert.notavailable");
			
			reverted.furtherActions = lastActions.get(index).revertFurtherActions();
			
			if (reverted.action(player)) {
				MinecraftForge.EVENT_BUS.post(new ActionEvent(reverted, ActionType.undo, player));
				if (reverted.sendToServer())
					PacketHandler.sendPacketToServer(reverted);
				
				if (reverted.furtherActions != null && !reverted.furtherActions.isEmpty()) {
					for (int i = 0; i < reverted.furtherActions.size(); i++) {
						LittleAction subAction = reverted.furtherActions.get(i);
						
						if (subAction == null)
							continue;
						
						try {
							subAction.action(player);
							
							if (subAction.sendToServer())
								PacketHandler.sendPacketToServer(subAction);
							
						} catch (LittleActionException e) {
							handleExceptionClient(e);
						}
					}
				}
				lastActions.set(index, reverted);
				index++;
				return true;
			}
		}
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean redo() throws LittleActionException {
		if (index > 0 && index <= lastActions.size()) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			
			index--;
			
			LittleAction reverted = lastActions.get(index).revert();
			
			if (reverted == null)
				throw new LittleActionException("action.revert.notavailable");
			
			reverted.furtherActions = lastActions.get(index).revertFurtherActions();
			
			if (reverted.action(player)) {
				MinecraftForge.EVENT_BUS.post(new ActionEvent(reverted, ActionType.redo, player));
				if (reverted.sendToServer())
					PacketHandler.sendPacketToServer(reverted);
				
				if (reverted.furtherActions != null && !reverted.furtherActions.isEmpty()) {
					for (int i = 0; i < reverted.furtherActions.size(); i++) {
						LittleAction subAction = reverted.furtherActions.get(i);
						
						if (subAction == null)
							continue;
						
						try {
							subAction.action(player);
							
							if (subAction.sendToServer())
								PacketHandler.sendPacketToServer(subAction);
							
						} catch (LittleActionException e) {
							handleExceptionClient(e);
						}
					}
				}
				lastActions.set(index, reverted);
				
				return true;
			}
		}
		return false;
	}
	
	public static void registerLittleAction(String id, Class<? extends LittleAction>... classTypes) {
		for (int i = 0; i < classTypes.length; i++)
			CreativeCorePacket.registerPacket(classTypes[i]);
	}
	
	public List<LittleAction> furtherActions = null;
	
	/** Must be implemented by every action **/
	public LittleAction() {
		
	}
	
	@SideOnly(Side.CLIENT)
	public abstract boolean canBeReverted();
	
	/** @return null if an revert action is not available */
	@SideOnly(Side.CLIENT)
	public abstract LittleAction revert() throws LittleActionException;
	
	private List<LittleAction> revertFurtherActions() {
		if (furtherActions == null || furtherActions.isEmpty())
			return null;
		
		List<LittleAction> result = new ArrayList<>(furtherActions.size());
		for (int i = furtherActions.size() - 1; i >= 0; i--)
			result.add(furtherActions.get(i));
		return result;
	}
	
	public boolean sendToServer() {
		return true;
	}
	
	protected abstract boolean action(EntityPlayer player) throws LittleActionException;
	
	@SideOnly(Side.CLIENT)
	public boolean execute() {
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		try {
			if (action(player)) {
				rememberAction(this);
				MinecraftForge.EVENT_BUS.post(new ActionEvent(this, ActionType.normal, player));
				
				if (sendToServer())
					PacketHandler.sendPacketToServer(this);
				
				if (furtherActions != null && !furtherActions.isEmpty()) {
					for (int i = 0; i < furtherActions.size(); i++) {
						LittleAction subAction = furtherActions.get(i);
						
						if (subAction == null)
							continue;
						
						try {
							subAction.action(player);
							
							if (subAction.sendToServer())
								PacketHandler.sendPacketToServer(subAction);
							
						} catch (LittleActionException e) {
							handleExceptionClient(e);
						}
					}
				}
				
				return true;
			}
		} catch (LittleActionException e) {
			handleExceptionClient(e);
		}
		
		return false;
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		try {
			action(player);
		} catch (LittleActionException e) {
			player.sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
		}
	}
	
	public boolean activateServer(EntityPlayer player) {
		try {
			return action(player);
		} catch (LittleActionException e) {
			return false;
		}
	}
	
	public abstract LittleAction flip(Axis axis, LittleAbsoluteBox box);
	
	@SideOnly(Side.CLIENT)
	public static void handleExceptionClient(LittleActionException e) {
		if (e.isHidden())
			return;
		
		ActionMessage message = e.getActionMessage();
		if (message != null)
			LittleTilesClient.displayActionMessage(message);
		else
			Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
	}
	
	public static boolean canConvertBlock(EntityPlayer player, World world, BlockPos pos, IBlockState state) {
		if (player.isCreative())
			return true;
		if (LittleTiles.CONFIG.survival.strictMining)
			return false;
		if (!LittleTiles.CONFIG.survival.editUnbreakable)
			return state.getBlock().getBlockHardness(state, world, pos) > 0;
		return LittleTiles.CONFIG.canEditBlock(player, state, pos);
	}
	
	public static boolean canUseUndoOrRedo(EntityPlayer player) {
		GameType type = PlayerUtils.getGameType(player);
		return type == GameType.CREATIVE || type == GameType.SURVIVAL;
	}
	
	public static boolean canPlace(EntityPlayer player) {
		GameType type = PlayerUtils.getGameType(player);
		if (type == GameType.CREATIVE || type == GameType.SURVIVAL || type == GameType.ADVENTURE)
			return true;
		return false;
	}
	
	public static boolean canPlaceInside(LittlePreviews previews, World world, BlockPos pos, boolean placeInside) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.isReplaceable(world, pos) || block instanceof BlockTile) {
			if (!placeInside) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityLittleTiles) {
					TileEntityLittleTiles teTiles = (TileEntityLittleTiles) te;
					for (LittlePreview preview : previews.allPreviews())
						if (!teTiles.isSpaceForLittleTile(preview.box))
							return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static TileEntityLittleTiles loadTe(EntityPlayer player, World world, BlockPos pos, boolean shouldConvert) {
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if (!(tileEntity instanceof TileEntityLittleTiles)) {
			List<LittleTile> tiles = new ArrayList<>();
			List<LittleTile> chiselTiles = ChiselsAndBitsManager.getTiles(tileEntity);
			LittleGridContext context = chiselTiles != null ? LittleGridContext.get(ChiselsAndBitsManager.convertingFrom) : LittleGridContext.get();
			if (chiselTiles != null)
				tiles.addAll(chiselTiles);
			else if (tileEntity == null && shouldConvert) {
				IBlockState state = world.getBlockState(pos);
				if (isBlockValid(state) && canConvertBlock(player, world, pos, state)) {
					
					context = LittleGridContext.get(LittleGridContext.minSize);
					
					LittleBox box = new LittleBox(0, 0, 0, context.maxPos, context.maxPos, context.maxPos);
					
					LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
					tile.box = box;
					tiles.add(tile);
				} else if (state.getMaterial().isReplaceable()) {
					if (!world.setBlockState(pos, BlockTile.getState(false, false)))
						return null;
					tileEntity = world.getTileEntity(pos);
				}
			}
			
			if (tiles != null && !tiles.isEmpty()) {
				world.setBlockState(pos, BlockTile.getState(tiles));
				TileEntityLittleTiles te = (TileEntityLittleTiles) world.getTileEntity(pos);
				te.convertTo(context);
				te.updateTiles((x) -> {
					for (LittleTile tile : tiles) {
						tile.te = te;
						tile.place(x);
					}
				});
				te.convertToSmallest();
				tileEntity = te;
			}
		}
		
		if (tileEntity instanceof TileEntityLittleTiles)
			return (TileEntityLittleTiles) tileEntity;
		return null;
	}
	
	private static Method loadWorldEditEvent() {
		try {
			Class clazz = Class.forName("com.sk89q.worldedit.forge.ForgeWorldEdit");
			worldEditInstance = clazz.getField("inst").get(null);
			return clazz.getMethod("onPlayerInteract", PlayerInteractEvent.class);
		} catch (Exception e) {
			
		}
		return null;
	}
	
	private static Method WorldEditEvent = loadWorldEditEvent();
	private static Object worldEditInstance = null;
	
	public static void sendEntityResetToClient(EntityPlayerMP player, EntityAnimation animation) {
		//tobedone
	}
	
	public static void sendBlockResetToClient(World world, EntityPlayerMP player, BlockPos pos) {
		if (world instanceof CreativeWorld)
			PacketHandler.sendPacketToPlayer(new LittleBlockUpdatePacket(world, pos, null), player);
		else
			player.connection.sendPacket(new SPacketBlockChange(player.world, pos));
	}
	
	public static void sendBlockResetToClient(World world, EntityPlayerMP player, TileEntityLittleTiles te) {
		if (world instanceof CreativeWorld)
			PacketHandler.sendPacketToPlayer(new LittleBlockUpdatePacket(world, te.getPos(), te), player);
		else {
			player.connection.sendPacket(new SPacketBlockChange(player.world, te.getPos()));
			if (te != null)
				player.connection.sendPacket(te.getUpdatePacket());
		}
	}
	
	public static void sendBlockResetToClient(World world, EntityPlayerMP player, Collection<TileEntityLittleTiles> tileEntities) {
		PacketHandler.sendPacketToPlayer(new LittleBlocksUpdatePacket(world, tileEntities), player);
	}
	
	public static void sendBlockResetToClient(World world, EntityPlayerMP player, LittleStructure structure) {
		sendBlockResetToClient(world, player, structure.collectBlocks());
	}
	
	public static boolean isAllowedToInteract(EntityPlayer player, EntityAnimation animation, boolean rightClick) {
		if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.isAllowEdit())))
			return false;
		
		return true;
	}
	
	public static boolean isAllowedToInteract(World world, EntityPlayer player, BlockPos pos, boolean rightClick, EnumFacing facing) {
		if (player == null || player.world.isRemote)
			return true;
		
		if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.isAllowEdit())))
			return false;
		
		if (WorldEditEvent != null) {
			PlayerInteractEvent event = rightClick ? new PlayerInteractEvent.RightClickBlock(player, EnumHand.MAIN_HAND, pos, facing, new Vec3d(pos)) : new PlayerInteractEvent.LeftClickBlock(player, pos, facing, new Vec3d(pos));
			try {
				if (worldEditInstance == null)
					loadWorldEditEvent();
				WorldEditEvent.invoke(worldEditInstance, event);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			if (event.isCanceled())
				return false;
		}
		
		return !player.getServer().isBlockProtected(player.world, pos, player);
	}
	
	public static boolean isAllowedToPlacePreview(EntityPlayer player, LittlePreview preview) throws LittleActionException {
		if (preview == null)
			return true;
		
		if (preview.hasColor() && ColorUtils.getAlpha(preview.getColor()) < LittleTiles.CONFIG.getMinimumTransparency(player))
			throw new NotAllowedToPlaceColorException();
		
		return true;
	}
	
	public static boolean isTileStillInPlace(LittleTile tile) {
		return tile.te.contains(tile);
	}
	
	public static LittleTile getTile(World world, LittleIdentifierAbsolute coord) throws LittleActionException {
		TileEntity te = world.getTileEntity(coord.pos);
		if (te instanceof TileEntityLittleTiles) {
			LittleTile tile = ((TileEntityLittleTiles) te).getTile(coord.context, coord.identifier);
			if (tile != null)
				return tile;
			throw new LittleActionException.TileNotFoundException();
		} else
			throw new LittleActionException.TileEntityNotFoundException();
	}
	
	public static double getVolume(LittleGridContext context, List<PlacePreview> tiles) {
		double volume = 0;
		for (PlacePreview preview : tiles)
			volume += preview.box.getPercentVolume(context);
		return volume;
	}
	
	public static void writeAbsoluteCoord(LittleIdentifierAbsolute coord, ByteBuf buf) {
		writePos(buf, coord.pos);
		buf.writeInt(coord.identifier.length);
		for (int i = 0; i < coord.identifier.length; i++) {
			buf.writeInt(coord.identifier[i]);
		}
		writeContext(coord.context, buf);
	}
	
	public static LittleIdentifierAbsolute readAbsoluteCoord(ByteBuf buf) {
		BlockPos pos = readPos(buf);
		int[] identifier = new int[buf.readInt()];
		for (int i = 0; i < identifier.length; i++) {
			identifier[i] = buf.readInt();
		}
		return new LittleIdentifierAbsolute(pos, readContext(buf), identifier);
	}
	
	public static void writePreviews(LittlePreviews previews, ByteBuf buf) {
		buf.writeBoolean(previews.isAbsolute());
		buf.writeBoolean(previews.hasStructure());
		if (previews.hasStructure())
			writeNBT(buf, previews.structure);
		if (previews.isAbsolute())
			writePos(buf, ((LittleAbsolutePreviews) previews).pos);
		
		writeContext(previews.getContext(), buf);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("list", LittleNBTCompressionTools.writePreviews(previews));
		
		NBTTagList children = new NBTTagList();
		for (LittlePreviews child : previews.getChildren())
			children.appendTag(LittlePreview.saveChildPreviews(child));
		nbt.setTag("children", children);
		
		writeNBT(buf, nbt);
	}
	
	public static LittlePreviews readPreviews(ByteBuf buf) {
		boolean absolute = buf.readBoolean();
		boolean structure = buf.readBoolean();
		
		NBTTagCompound nbt;
		LittlePreviews previews;
		if (absolute) {
			if (structure)
				previews = LittleNBTCompressionTools.readPreviews(new LittleAbsolutePreviews(readNBT(buf), readPos(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
			else
				previews = LittleNBTCompressionTools.readPreviews(new LittleAbsolutePreviews(readPos(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
		} else {
			if (structure)
				previews = LittleNBTCompressionTools.readPreviews(new LittlePreviews(readNBT(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
			else
				previews = LittleNBTCompressionTools.readPreviews(new LittlePreviews(readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
		}
		
		NBTTagList list = nbt.getTagList("children", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound child = list.getCompoundTagAt(i);
			previews.addChild(LittlePreviews.getChild(previews.getContext(), child));
		}
		return previews;
	}
	
	public static void writePlacementMode(PlacementMode mode, ByteBuf buf) {
		writeString(buf, mode.name);
	}
	
	public static PlacementMode readPlacementMode(ByteBuf buf) {
		return PlacementMode.getModeOrDefault(readString(buf));
	}
	
	public static void writeContext(LittleGridContext context, ByteBuf buf) {
		buf.writeInt(context.size);
	}
	
	public static LittleGridContext readContext(ByteBuf buf) {
		return LittleGridContext.get(buf.readInt());
	}
	
	public static void writeLittleVecContext(LittleVecContext vec, ByteBuf buf) {
		writeLittleVec(vec.getVec(), buf);
		writeContext(vec.getContext(), buf);
	}
	
	public static LittleVecContext readLittleVecContext(ByteBuf buf) {
		return new LittleVecContext(readLittleVec(buf), readContext(buf));
	}
	
	public static void writeBoxes(LittleBoxes boxes, ByteBuf buf) {
		writePos(buf, boxes.pos);
		writeContext(boxes.context, buf);
		buf.writeInt(boxes.size());
		for (LittleBox box : boxes) {
			writeLittleBox(box, buf);
		}
	}
	
	public static LittleBoxes readBoxes(ByteBuf buf) {
		BlockPos pos = readPos(buf);
		LittleGridContext context = readContext(buf);
		LittleBoxes boxes = new LittleBoxes(pos, context);
		int length = buf.readInt();
		for (int i = 0; i < length; i++) {
			boxes.add(readLittleBox(buf));
		}
		return boxes;
	}
	
	public static void writeLittlePos(LittleAbsoluteVec pos, ByteBuf buf) {
		writePos(buf, pos.getPos());
		writeLittleVecContext(pos.getVecContext(), buf);
	}
	
	public static LittleAbsoluteVec readLittlePos(ByteBuf buf) {
		return new LittleAbsoluteVec(readPos(buf), readLittleVecContext(buf));
	}
	
	public static void writeLittleVec(LittleVec vec, ByteBuf buf) {
		buf.writeInt(vec.x);
		buf.writeInt(vec.y);
		buf.writeInt(vec.z);
	}
	
	public static LittleVec readLittleVec(ByteBuf buf) {
		return new LittleVec(buf.readInt(), buf.readInt(), buf.readInt());
	}
	
	public static void writeSelector(TileSelector selector, ByteBuf buf) {
		writeNBT(buf, selector.writeNBT(new NBTTagCompound()));
	}
	
	public static TileSelector readSelector(ByteBuf buf) {
		return TileSelector.loadSelector(readNBT(buf));
	}
	
	public static void writeLittleBox(LittleBox box, ByteBuf buf) {
		int[] array = box.getArray();
		buf.writeInt(array.length);
		for (int i = 0; i < array.length; i++) {
			buf.writeInt(array[i]);
		}
	}
	
	public static LittleBox readLittleBox(ByteBuf buf) {
		int[] array = new int[buf.readInt()];
		for (int i = 0; i < array.length; i++) {
			array[i] = buf.readInt();
		}
		return LittleBox.createBox(array);
	}
	
	public static void writeActionMessage(ActionMessage message, ByteBuf buf) {
		writeString(buf, message.text);
		buf.writeInt(message.objects.length);
		for (int i = 0; i < message.objects.length; i++) {
			ActionMessageObjectType type = ActionMessage.getType(message.objects[i]);
			buf.writeInt(type.index());
			type.write(message.objects[i], buf);
		}
	}
	
	public static ActionMessage readActionMessage(ByteBuf buf) {
		String text = readString(buf);
		Object[] objects = new Object[buf.readInt()];
		for (int i = 0; i < objects.length; i++) {
			ActionMessageObjectType type = ActionMessage.getType(buf.readInt());
			objects[i] = type.read(buf);
		}
		return new ActionMessage(text, objects);
	}
	
	public static boolean needIngredients(EntityPlayer player) {
		return !player.isCreative();
	}
	
	public static LittleIngredients getIngredients(LittleTile tile) {
		LittlePreviews previews = new LittlePreviews(tile.getContext());
		previews.addTile(tile);
		return getIngredients(previews);
	}
	
	public static LittleIngredients getIngredients(List<LittleTile> tiles) {
		LittlePreviews previews = new LittlePreviews(tiles.get(0).getContext());
		previews.addTiles(tiles);
		return getIngredients(previews);
	}
	
	public static LittleIngredients getIngredients(LittlePreviews previews) {
		return LittleIngredient.extract(previews);
	}
	
	public static LittleIngredients getIngredients(LittlePreview preview, double volume) {
		return LittleIngredient.extract(preview, volume);
	}
	
	public static boolean canTake(EntityPlayer player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
		if (needIngredients(player)) {
			try {
				inventory.startSimulation();
				inventory.take(ingredients.copy());
				return true;
			} finally {
				inventory.stopSimulation();
			}
		}
		return true;
	}
	
	public static boolean checkAndTake(EntityPlayer player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
		if (needIngredients(player)) {
			try {
				inventory.startSimulation();
				inventory.take(ingredients.copy());
			} finally {
				inventory.stopSimulation();
			}
			inventory.take(ingredients.copy());
		}
		return true;
	}
	
	public static boolean take(EntityPlayer player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
		if (needIngredients(player))
			inventory.take(ingredients.copy());
		return true;
	}
	
	public static boolean take(EntityPlayer player, LittleInventory inventory, ItemStack toDrain) throws NotEnoughIngredientsException {
		if (!needIngredients(player))
			return true;
		
		String id = ItemPremadeStructure.getPremadeId(toDrain);
		for (ItemStack stack : inventory) {
			if (stack.getItem() == LittleTiles.premade && ItemPremadeStructure.getPremadeId(stack).equals(id)) {
				stack.shrink(1);
				return true;
			}
		}
		throw new NotEnoughIngredientsException(toDrain);
	}
	
	public static boolean canGive(EntityPlayer player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
		if (needIngredients(player)) {
			try {
				inventory.startSimulation();
				inventory.give(ingredients.copy());
				return true;
			} finally {
				inventory.stopSimulation();
			}
		}
		return true;
	}
	
	public static boolean checkAndGive(EntityPlayer player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
		if (needIngredients(player)) {
			try {
				inventory.startSimulation();
				inventory.give(ingredients.copy());
			} finally {
				inventory.stopSimulation();
			}
			inventory.give(ingredients.copy());
		}
		return true;
	}
	
	public static boolean give(EntityPlayer player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
		if (needIngredients(player))
			inventory.give(ingredients.copy());
		return true;
	}
	
	public static boolean giveOrDrop(EntityPlayer player, LittleInventory inventory, List<LittleTile> tiles) {
		if (needIngredients(player) && !tiles.isEmpty()) {
			LittlePreviews previews = new LittlePreviews(tiles.get(0).getContext());
			previews.addTiles(tiles);
			try {
				checkAndGive(player, inventory, getIngredients(previews));
			} catch (NotEnoughIngredientsException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public static List<ItemStack> getInventories(EntityPlayer player) {
		List<ItemStack> inventories = new ArrayList<>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack.getItem() instanceof ILittleIngredientInventory)
				inventories.add(stack);
		}
		return inventories;
	}
	
	public static boolean isBlockValid(IBlockState state) {
		Block block = state.getBlock();
		if (ChiselsAndBitsManager.isChiselsAndBitsStructure(state))
			return true;
		if (ColoredLightsManager.isBlockFromColoredBlocks(block))
			return true;
		if (block.hasTileEntity(state) || block instanceof BlockSlab)
			return false;
		return state.isNormalCube() || state.isFullCube() || state.isFullBlock() || block instanceof BlockGlass || block instanceof BlockStainedGlass || block instanceof BlockBreakable;
	}
	
}
