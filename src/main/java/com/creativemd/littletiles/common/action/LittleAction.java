package com.creativemd.littletiles.common.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.mc.PlayerUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig.AreaProtected;
import com.creativemd.littletiles.LittleTilesConfig.NotAllowedToConvertBlockException;
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
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxesNoOverlap;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxesSimple;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;
import com.creativemd.littletiles.common.tile.math.location.TileLocation;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
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
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleAction extends CreativeCorePacket {
    
    private static List<LittleAction> lastActions = new ArrayList<>();
    
    private static int index = 0;
    
    @SideOnly(Side.CLIENT)
    public static boolean isUsingSecondMode(EntityPlayer player) {
        if (player == null)
            return false;
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
                lastActions = lastActions.subList(index, lastActions.size());
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
            
            LittleAction reverted = lastActions.get(index).revert(player);
            
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
    public static void unloadWorld() {
        lastActions.clear();
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean redo() throws LittleActionException {
        if (index > 0 && index <= lastActions.size()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            
            index--;
            
            LittleAction reverted = lastActions.get(index).revert(player);
            
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
    public abstract LittleAction revert(EntityPlayer player) throws LittleActionException;
    
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
    
    public static boolean canConvertBlock(EntityPlayer player, World world, BlockPos pos, IBlockState state, int affected) throws LittleActionException {
        if (LittleTiles.CONFIG.build.get(player).limitAffectedBlocks && LittleTiles.CONFIG.build.get(player).maxAffectedBlocks < affected)
            throw new NotAllowedToConvertBlockException(player);
        if (!LittleTiles.CONFIG.build.get(player).editUnbreakable && state.getBlock().getBlockHardness(state, world, pos) < 0)
            throw new NotAllowedToConvertBlockException(player);
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
    
    public static TileEntityLittleTiles loadTe(EntityPlayer player, World world, BlockPos pos, MutableInt affected, boolean shouldConvert, int attribute) throws LittleActionException {
        TileEntity tileEntity = world.getTileEntity(pos);
        
        if (!(tileEntity instanceof TileEntityLittleTiles)) {
            List<LittleTile> tiles = new ArrayList<>();
            List<LittleTile> chiselTiles = ChiselsAndBitsManager.getTiles(tileEntity);
            LittleGridContext context = chiselTiles != null ? LittleGridContext.get(ChiselsAndBitsManager.convertingFrom) : LittleGridContext.get();
            if (chiselTiles != null)
                tiles.addAll(chiselTiles);
            else if (tileEntity == null && shouldConvert) {
                IBlockState state = world.getBlockState(pos);
                if (isBlockValid(state) && canConvertBlock(player, world, pos, state, affected == null ? 0 : affected.incrementAndGet())) {
                    
                    context = LittleGridContext.get(LittleGridContext.minSize);
                    
                    LittleBox box = new LittleBox(0, 0, 0, context.maxPos, context.maxPos, context.maxPos);
                    
                    LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
                    tile.setBox(box);
                    tiles.add(tile);
                } else if (state.getMaterial().isReplaceable()) {
                    if (!world.setBlockState(pos, BlockTile.getStateByAttribute(attribute)))
                        return null;
                    tileEntity = world.getTileEntity(pos);
                }
            }
            
            if (tiles != null && !tiles.isEmpty()) {
                world.setBlockState(pos, BlockTile.getStateByAttribute(attribute));
                TileEntityLittleTiles te = (TileEntityLittleTiles) world.getTileEntity(pos);
                te.convertTo(context);
                te.updateTilesSecretly((x) -> x.noneStructureTiles().addAll(tiles));
                te.convertToSmallest();
                tileEntity = te;
            }
        }
        
        if (tileEntity instanceof TileEntityLittleTiles)
            return (TileEntityLittleTiles) tileEntity;
        return null;
    }
    
    public static void fireBlockBreakEvent(World world, BlockPos pos, EntityPlayer player) throws AreaProtected {
        if (world.isRemote)
            return;
        BreakEvent event = new BreakEvent(world, pos, world.getBlockState(pos), player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            sendBlockResetToClient(world, player, pos);
            throw new AreaProtected();
        }
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
    
    public static void sendBlockResetToClient(World world, EntityPlayer player, BlockPos pos) {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
            return;
        TileEntity te = world.getTileEntity(pos);
        if (te != null)
            sendBlockResetToClient(world, player, te);
        else {
            if (world instanceof CreativeWorld)
                PacketHandler.sendPacketToPlayer(new LittleBlockUpdatePacket(world, pos, null), (EntityPlayerMP) player);
            else
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(player.world, pos));
        }
    }
    
    public static void sendBlockResetToClient(World world, EntityPlayer player, TileEntity te) {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
            return;
        if (world instanceof CreativeWorld)
            PacketHandler.sendPacketToPlayer(new LittleBlockUpdatePacket(world, te.getPos(), te), (EntityPlayerMP) player);
        else {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(player.world, te.getPos()));
            if (te != null)
                ((EntityPlayerMP) player).connection.sendPacket(te.getUpdatePacket());
        }
    }
    
    public static void sendBlockResetToClient(World world, EntityPlayer player, Iterable<TileEntityLittleTiles> tileEntities) {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
            return;
        PacketHandler.sendPacketToPlayer(new LittleBlocksUpdatePacket(world, tileEntities), (EntityPlayerMP) player);
    }
    
    public static void sendBlockResetToClient(World world, EntityPlayer player, LittleStructure structure) {
        if (!(player instanceof EntityPlayerMP))
            return;
        try {
            if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
                return;
            sendBlockResetToClient(world, player, structure.blocks());
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isAllowedToInteract(EntityPlayer player, EntityAnimation animation, boolean rightClick) {
        if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.isAllowEdit())))
            return false;
        
        return true;
    }
    
    public static boolean isAllowedToInteract(World world, EntityPlayer player, BlockPos pos, boolean rightClick, EnumFacing facing) {
        if (player == null || player.world.isRemote)
            return true;
        
        if (player.isSpectator())
            return false;
        
        if (!rightClick && PlayerUtils.isAdventure(player)) {
            ItemStack stack = player.getHeldItemMainhand();
            if (!stack.canDestroy(world.getBlockState(pos).getBlock()))
                return false;
        } else if (!rightClick && !player.isAllowEdit())
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
            throw new NotAllowedToPlaceColorException(player);
        
        return true;
    }
    
    public static double getVolume(LittleGridContext context, List<PlacePreview> tiles) {
        double volume = 0;
        for (PlacePreview preview : tiles)
            volume += preview.box.getPercentVolume(context);
        return volume;
    }
    
    public static void writeTileLocation(TileLocation location, ByteBuf buf) {
        writePos(buf, location.pos);
        buf.writeBoolean(location.isStructure);
        buf.writeInt(location.index);
        int[] boxArray = location.box.getArray();
        buf.writeInt(boxArray.length);
        for (int i = 0; i < boxArray.length; i++)
            buf.writeInt(boxArray[i]);
        if (location.worldUUID != null) {
            buf.writeBoolean(true);
            writeString(buf, location.worldUUID.toString());
        } else
            buf.writeBoolean(false);
    }
    
    public static TileLocation readTileLocation(ByteBuf buf) {
        BlockPos pos = readPos(buf);
        boolean isStructure = buf.readBoolean();
        int index = buf.readInt();
        int[] boxArray = new int[buf.readInt()];
        for (int i = 0; i < boxArray.length; i++)
            boxArray[i] = buf.readInt();
        UUID world = null;
        if (buf.readBoolean())
            world = UUID.fromString(readString(buf));
        return new TileLocation(pos, isStructure, index, LittleBox.createBox(boxArray), world);
    }
    
    public static void writeStructureLocation(StructureLocation location, ByteBuf buf) {
        writePos(buf, location.pos);
        buf.writeInt(location.index);
        if (location.worldUUID != null) {
            buf.writeBoolean(true);
            writeString(buf, location.worldUUID.toString());
        } else
            buf.writeBoolean(false);
    }
    
    public static StructureLocation readStructureLocation(ByteBuf buf) {
        BlockPos pos = readPos(buf);
        int index = buf.readInt();
        UUID world = null;
        if (buf.readBoolean())
            world = UUID.fromString(readString(buf));
        return new StructureLocation(pos, index, world);
    }
    
    public static void writePreviews(LittlePreviews previews, ByteBuf buf) {
        buf.writeBoolean(previews.isAbsolute());
        buf.writeBoolean(previews.hasStructure());
        if (previews.hasStructure())
            writeNBT(buf, previews.structureNBT);
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
                previews = LittleNBTCompressionTools
                    .readPreviews(new LittleAbsolutePreviews(readNBT(buf), readPos(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
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
            previews.addChild(LittlePreviews.getChild(previews.getContext(), child), child.getBoolean("dynamic"));
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
        if (boxes instanceof LittleBoxesSimple) {
            buf.writeBoolean(true);
            buf.writeInt(boxes.size());
            for (LittleBox box : boxes.all())
                writeLittleBox(box, buf);
        } else {
            buf.writeBoolean(false);
            HashMapList<BlockPos, LittleBox> map = boxes.generateBlockWise();
            buf.writeInt(map.size());
            for (Entry<BlockPos, ArrayList<LittleBox>> entry : map.entrySet()) {
                writePos(buf, entry.getKey());
                buf.writeInt(entry.getValue().size());
                for (LittleBox box : entry.getValue())
                    writeLittleBox(box, buf);
            }
        }
    }
    
    public static LittleBoxes readBoxes(ByteBuf buf) {
        BlockPos pos = readPos(buf);
        LittleGridContext context = readContext(buf);
        if (buf.readBoolean()) {
            LittleBoxes boxes = new LittleBoxesSimple(pos, context);
            int length = buf.readInt();
            for (int i = 0; i < length; i++)
                boxes.add(readLittleBox(buf));
            return boxes;
        } else {
            int posCount = buf.readInt();
            HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
            for (int i = 0; i < posCount; i++) {
                BlockPos posList = readPos(buf);
                int boxCount = buf.readInt();
                List<LittleBox> blockBoxes = new ArrayList<>();
                for (int j = 0; j < boxCount; j++)
                    blockBoxes.add(readLittleBox(buf));
                map.add(posList, blockBoxes);
            }
            return new LittleBoxesNoOverlap(pos, context, map);
        }
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
    
    public static LittleIngredients getIngredients(IParentTileList parent, LittleTile tile) {
        LittlePreviews previews = new LittlePreviews(parent.getContext());
        previews.addTile(parent, tile);
        return getIngredients(previews);
    }
    
    public static LittleIngredients getIngredients(IParentTileList parent, List<LittleTile> tiles) {
        LittlePreviews previews = new LittlePreviews(parent.getContext());
        for (LittleTile tile : tiles)
            previews.addTile(parent, tile);
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
    
    public static boolean giveOrDrop(EntityPlayer player, LittleInventory inventory, IParentTileList parent, List<LittleTile> tiles) {
        if (needIngredients(player) && !tiles.isEmpty()) {
            LittlePreviews previews = new LittlePreviews(parent.getContext());
            for (LittleTile tile : tiles)
                previews.addTile(parent, tile);
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
        return state.isNormalCube() || state.isFullCube() || state
            .isFullBlock() || block instanceof BlockGlass || block instanceof BlockStainedGlass || block instanceof BlockBreakable;
    }
    
}
