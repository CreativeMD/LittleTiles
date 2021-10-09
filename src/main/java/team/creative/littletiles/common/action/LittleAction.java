package team.creative.littletiles.common.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.creativemd.littletiles.common.action.BlockBreakable;
import com.creativemd.littletiles.common.action.BlockGlass;
import com.creativemd.littletiles.common.action.BlockSlab;
import com.creativemd.littletiles.common.action.BlockStainedGlass;
import com.creativemd.littletiles.common.action.CreativeWorld;
import com.creativemd.littletiles.common.action.EntityPlayer;
import com.creativemd.littletiles.common.action.EntityPlayerMP;
import com.creativemd.littletiles.common.action.IBlockState;
import com.creativemd.littletiles.common.action.IParentTileList;
import com.creativemd.littletiles.common.action.LittleGridContext;
import com.creativemd.littletiles.common.action.LittlePreview;
import com.creativemd.littletiles.common.action.LittlePreviews;
import com.creativemd.littletiles.common.action.NBTTagCompound;
import com.creativemd.littletiles.common.action.SPacketBlockChange;
import com.creativemd.littletiles.common.action.TileEntity;
import com.creativemd.littletiles.common.action.TileEntityLittleTiles;
import com.creativemd.littletiles.common.action.World;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.mod.coloredlights.ColoredLightsManager;
import com.creativemd.littletiles.common.tile.place.PlacePreview;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.config.LittleTilesConfig.AreaProtected;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToConvertBlockException;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToPlaceColorException;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.packet.LittleEntityRequestPacket;
import team.creative.littletiles.common.packet.update.LittleBlockUpdatePacket;
import team.creative.littletiles.common.packet.update.LittleBlocksUpdatePacket;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.group.LittleGroup;
import team.creative.littletiles.common.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.tile.parent.IParentCollection;
import team.creative.littletiles.common.tile.parent.ParentCollection;

public abstract class LittleAction extends CreativePacket {
    
    /** Must be implemented by every action **/
    public LittleAction() {
        
    }
    
    @OnlyIn(Dist.CLIENT)
    public abstract boolean canBeReverted();
    
    /** @return null if an revert action is not available */
    @OnlyIn(Dist.CLIENT)
    public abstract LittleAction revert(Player player) throws LittleActionException;
    
    public abstract boolean action(Player player) throws LittleActionException;
    
    @Override
    public final void executeClient(Player player) {}
    
    @Override
    public final void executeServer(ServerPlayer player) {
        try {
            action(player);
        } catch (LittleActionException e) {
            player.sendMessage(new TextComponent(e.getLocalizedMessage()), Util.NIL_UUID);
        }
    }
    
    public abstract LittleAction mirror(Axis axis, LittleBoxAbsolute box);
    
    public static boolean canConvertBlock(Player player, Level level, BlockPos pos, BlockState state, int affected) throws LittleActionException {
        if (LittleTiles.CONFIG.build.get(player).limitAffectedBlocks && LittleTiles.CONFIG.build.get(player).maxAffectedBlocks < affected)
            throw new NotAllowedToConvertBlockException(player);
        if (!LittleTiles.CONFIG.build.get(player).editUnbreakable)
            return state.getBlock().defaultDestroyTime() > 0;
        return LittleTiles.CONFIG.canEditBlock(player, state, pos);
    }
    
    public static boolean canPlace(Player player) {
        GameType type = PlayerUtils.getGameType(player);
        if (type == GameType.CREATIVE || type == GameType.SURVIVAL || type == GameType.ADVENTURE)
            return true;
        return false;
    }
    
    public static boolean canPlaceInside(LittleGroup previews, Level level, BlockPos pos, boolean placeInside) {
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
                    if (!world.setBlockState(pos, BlockTile.getState(attribute)))
                        return null;
                    tileEntity = world.getTileEntity(pos);
                }
            }
            
            if (tiles != null && !tiles.isEmpty()) {
                world.setBlockState(pos, BlockTile.getState(attribute));
                TileEntityLittleTiles te = (TileEntityLittleTiles) world.getTileEntity(pos);
                te.convertTo(context);
                te.updateTiles((x) -> x.noneStructureTiles().addAll(tiles));
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
    
    public static void sendEntityResetToClient(Player player, EntityAnimation animation) {
        if (!(player instanceof ServerPlayer))
            return;
        PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
    }
    
    public static void sendBlockResetToClient(Level level, Player player, PlacementPreview preview) {
        TODO
    }
    
    public static void sendBlockResetToClient(Level level, Player player, BlockPos pos) {
        if (!(player instanceof ServerPlayer))
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
    
    public static void sendBlockResetToClient(Level level, Player player, TileEntity te) {
        if (!(player instanceof ServerPlayer))
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
    
    public static void sendBlockResetToClient(Level level, Player player, Iterable<TileEntityLittleTiles> tileEntities) {
        if (!(player instanceof ServerPlayer))
            return;
        if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
            return;
        PacketHandler.sendPacketToPlayer(new LittleBlocksUpdatePacket(world, tileEntities), (EntityPlayerMP) player);
    }
    
    public static void sendBlockResetToClient(Level level, Player player, LittleStructure structure) {
        if (!(player instanceof ServerPlayer))
            return;
        try {
            if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
                return;
            sendBlockResetToClient(world, player, structure.blocks());
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isAllowedToInteract(Player player, EntityAnimation animation, boolean rightClick) {
        if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.isAllowEdit())))
            return false;
        
        return true;
    }
    
    public static boolean isAllowedToInteract(Level level, Player player, BlockPos pos, boolean rightClick, Facing facing) {
        if (player == null || player.level.isClientSide)
            return true;
        
        if (player.isSpectator() || (!rightClick && (PlayerUtils.getGameType(player) == GameType.ADVENTURE || !player.isAllowEdit())))
            return false;
        
        if (WorldEditEvent != null) {
            PlayerInteractEvent event = rightClick ? new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, facing, new Vec3d(pos)) : new PlayerInteractEvent.LeftClickBlock(player, pos, facing, new Vec3d(pos));
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
        
        return !player.getServer().isUnderSpawnProtection((ServerLevel) player.level, pos, player);
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
    
    public static boolean needIngredients(Player player) {
        return !player.isCreative();
    }
    
    public static LittleIngredients getIngredients(IParentCollection parent, LittleTile tile) {
        LittlePreviews previews = new LittlePreviews(parent.getContext());
        previews.addTile(parent, tile);
        return getIngredients(previews);
    }
    
    public static LittleIngredients getIngredients(IParentCollection parent, List<LittleTile> tiles) {
        LittlePreviews previews = new LittlePreviews(parent.getContext());
        for (LittleTile tile : tiles)
            previews.addTile(parent, tile);
        return getIngredients(previews);
    }
    
    public static LittleIngredients getIngredients(LittleGroup previews) {
        return LittleIngredient.extract(previews);
    }
    
    public static LittleIngredients getIngredients(LittleGroupAbsolute previews) {
        return LittleIngredient.extract(previews.group);
    }
    
    public static LittleIngredients getIngredients(LittleTile tile, double volume) {
        return LittleIngredient.extract(tile, volume);
    }
    
    public static boolean canTake(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
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
    
    public static boolean checkAndTake(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
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
    
    public static boolean take(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (needIngredients(player))
            inventory.take(ingredients.copy());
        return true;
    }
    
    public static boolean take(Player player, LittleInventory inventory, ItemStack toDrain) throws NotEnoughIngredientsException {
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
    
    public static boolean canGive(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
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
    
    public static boolean checkAndGive(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
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
    
    public static boolean give(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (needIngredients(player))
            inventory.give(ingredients.copy());
        return true;
    }
    
    public static boolean giveOrDrop(Player player, LittleInventory inventory, ParentCollection parent, List<LittleTile> tiles) {
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
    
    public static List<ItemStack> getInventories(Player player) {
        List<ItemStack> inventories = new ArrayList<>();
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ILittleIngredientInventory)
                inventories.add(stack);
        }
        return inventories;
    }
    
    public static boolean isBlockValid(BlockState state) {
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
