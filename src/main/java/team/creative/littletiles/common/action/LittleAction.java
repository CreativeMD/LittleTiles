package team.creative.littletiles.common.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.client.level.BlockStatePredictionHandlerExtender;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.common.action.cancel.ActionCancelContext;
import team.creative.littletiles.common.action.exception.GridTooHighException;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.exception.NotAllowedToConvertBlockException;
import team.creative.littletiles.common.action.exception.NotAllowedToPlaceColorException;
import team.creative.littletiles.common.action.exception.NotAllowedToPlaceTransformableException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.config.LittlePermissionBuild;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.packet.update.BlockUpdate;
import team.creative.littletiles.common.packet.update.BlocksUpdate;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public abstract class LittleAction<T> extends CreativePacket {
    
    /** Must be implemented by every action **/
    public LittleAction() {
        
    }
    
    @OnlyIn(Dist.CLIENT)
    public abstract boolean canBeReverted();
    
    /** @return null if an revert action is not available */
    @OnlyIn(Dist.CLIENT)
    public abstract LittleAction revert(LittleActionSource source) throws LittleActionException;
    
    public abstract T action(LittleActionSource source) throws LittleActionException;
    
    public abstract boolean wasSuccessful(T result);
    
    public abstract T failed();
    
    /** called when the action failed or in case of combined action, if one failed. This method should make sure everything returns to its default state.
     * Important: Only called on client side. Changes on server side cannot be cancelled. */
    public abstract void cancel(ActionCancelContext context) throws LittleActionException;
    
    @Override
    public final void executeClient(Player player) {}
    
    @Override
    public final void executeServer(ServerPlayer player) {
        try {
            action((LittleActionSource) player);
        } catch (LittleActionException e) {
            player.sendSystemMessage(Component.literal(e.getLocalizedMessage()));
        }
    }
    
    public abstract LittleAction mirror(Axis axis, LittleBoxAbsolute box);
    
    public abstract void include(LittleBoxes boxes);
    
    public abstract void exclude(LittleBoxes boxes);
    
    public static boolean canConvertBlock(LittleActionSource source, Level level, BlockPos pos, BlockState state, int affected) throws LittleActionException {
        if (!source.isPlayer())
            return true;
        
        var player = source.asPlayer();
        LittlePermissionBuild config = LittleTiles.CONFIG.build.get(player);
        if (config.affectedBlockLimit.isEnabled() && config.affectedBlockLimit.value < affected)
            throw new NotAllowedToConvertBlockException(config);
        if (!config.editUnbreakable && state.getBlock().defaultDestroyTime() < 0)
            throw new NotAllowedToConvertBlockException(config);
        return LittleTiles.CONFIG.canEditBlock(player, state, pos);
    }
    
    public static boolean canPlace(Player player) {
        GameType type = PlayerUtils.getGameType(player);
        if (type == GameType.CREATIVE || type == GameType.SURVIVAL || type == GameType.ADVENTURE)
            return true;
        return false;
    }
    
    public static boolean canPlaceInside(Level level, BlockPos pos, boolean placeInside) {
        BlockState state = level.getBlockState(pos);
        if (state.canBeReplaced(new DirectionalPlaceContext(level, pos, Direction.EAST, ItemStack.EMPTY, Direction.EAST)) || state.getBlock() instanceof BlockTile)
            return true;
        return false;
    }
    
    public static boolean setBlockPreventPredict(Level level, BlockPos pos, BlockState state, int notification) {
        if (level.isClientSide)
            return setBlockPreventPredictClient(level, pos, state, notification);
        return level.setBlock(pos, state, notification);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static boolean setBlockPreventPredictClient(Level level, BlockPos pos, BlockState state, int notification) {
        BlockStatePredictionHandlerExtender b = level instanceof ClientLevelExtender e && e.blockStatePredictionHandler().isPredicting() ? (BlockStatePredictionHandlerExtender) e
                .blockStatePredictionHandler() : null;
        if (b != null)
            b.setPredicting(false);
        boolean result = level.setBlock(pos, state, notification);
        if (b != null)
            b.setPredicting(true);
        return result;
    }
    
    public static BETiles loadBE(LittleActionSource source, Level level, BlockPos pos, MutableInt affected, boolean shouldConvert, int attribute) throws LittleActionException {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (!(blockEntity instanceof BETiles)) {
            LittleGroup tiles = null;
            LittleGroup chiselTiles = ChiselsAndBitsManager.getGroup(blockEntity);
            if (chiselTiles != null)
                tiles = chiselTiles;
            else if (blockEntity == null && shouldConvert) {
                BlockState state = level.getBlockState(pos);
                if (isBlockValid(state) && canConvertBlock(source, level, pos, state, affected == null ? 0 : affected.incrementAndGet())) {
                    tiles = new LittleGroup();
                    LittleBox box = new LittleBox(0, 0, 0, tiles.getGrid().count, tiles.getGrid().count, tiles.getGrid().count);
                    tiles.add(tiles.getGrid(), new LittleElement(state, ColorUtils.WHITE), box);
                } else if (state.is(BlockTags.REPLACEABLE)) {
                    if (!setBlockPreventPredict(level, pos, BlockTile.getStateByAttribute(level, pos, attribute), 3))
                        return null;
                    blockEntity = level.getBlockEntity(pos);
                }
            }
            
            if (tiles != null && !tiles.isEmpty()) {
                setBlockPreventPredict(level, pos, BlockTile.getStateByAttribute(level, pos, attribute), 3);
                BETiles te = (BETiles) level.getBlockEntity(pos);
                te.convertTo(tiles.getGrid());
                final LittleGroup toAdd = tiles;
                te.updateTilesSecretly((x) -> {
                    for (LittleTile tile : toAdd)
                        x.noneStructureTiles().add(tile);
                });
                te.convertToSmallest();
                blockEntity = te;
            }
        }
        
        if (blockEntity instanceof BETiles b)
            return b;
        return null;
    }
    
    public static boolean fireBlockBreakEvent(Level level, BlockPos pos, LittleActionSource source) {
        if (!source.isPlayer() || level.isClientSide)
            return true;
        BreakEvent event = new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), source.asPlayer());
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            sendBlockResetToClient(level, source, pos);
            return false;
        }
        return true;
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
    
    public static void sendBlockResetToClient(LevelAccessor level, LittleActionSource source, PlacementPreview preview) {
        if (!(source instanceof ServerPlayer))
            return;
        LittleTiles.NETWORK.sendToClient(new BlocksUpdate(level, preview.getPositions()), (ServerPlayer) source);
    }
    
    public static void sendBlockResetToClient(LevelAccessor level, LittleActionSource source, BlockPos pos) {
        if (!(source instanceof ServerPlayer))
            return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null)
            sendBlockResetToClient(level, source, be);
        else
            LittleTiles.NETWORK.sendToClient(new BlockUpdate(level, pos, be), (ServerPlayer) source);
    }
    
    public static void sendBlockResetToClient(LevelAccessor level, LittleActionSource source, BlockEntity be) {
        if (!(source instanceof ServerPlayer))
            return;
        LittleTiles.NETWORK.sendToClient(new BlockUpdate(level, be.getBlockPos(), be), (ServerPlayer) source);
    }
    
    public static void sendBlockResetToClient(LevelAccessor level, LittleActionSource source, Iterable<BETiles> blockEntities) {
        if (!(source instanceof ServerPlayer))
            return;
        LittleTiles.NETWORK.sendToClient(new BlocksUpdate(level, blockEntities), (ServerPlayer) source);
    }
    
    public static void sendBlockResetToClient(LevelAccessor level, LittleActionSource source, LittleStructure structure) {
        if (!(source instanceof ServerPlayer))
            return;
        try {
            sendBlockResetToClient(level, source, structure.blocks());
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isAllowedToInteract(LittleActionSource source, LittleEntity entity, boolean rightClick) {
        if (!source.isPlayer())
            return true;
        var player = source.asPlayer();
        if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.mayBuild())))
            return false;
        
        return true;
    }
    
    public static boolean isAllowedToInteract(LevelAccessor level, LittleActionSource source, BlockPos pos, boolean rightClick, Facing facing) {
        if (source == null || !source.isPlayer() || source.getActionLevel().isClientSide)
            return true;
        
        var player = source.asPlayer();
        if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.mayBuild())))
            return false;
        
        if (player.isSpectator())
            return false;
        
        if (!rightClick && PlayerUtils.isAdventure(player)) {
            ItemStack stack = player.getMainHandItem();
            BlockInWorld blockinworld = new BlockInWorld(level, pos, false);
            if (!stack.canPlaceOnBlockInAdventureMode(blockinworld))
                return false;
        } else if (!rightClick && !player.mayBuild())
            return false;
        
        if (WorldEditEvent != null) {
            PlayerInteractEvent event = rightClick ? new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, new BlockHitResult(Vec3.atBottomCenterOf(
                pos), facing.toVanilla(), pos, true)) : new PlayerInteractEvent.LeftClickBlock(player, pos, facing.toVanilla(), Action.START);
            try {
                if (worldEditInstance == null)
                    loadWorldEditEvent();
                WorldEditEvent.invoke(worldEditInstance, event);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (((ICancellableEvent) event).isCanceled())
                return false;
        }
        
        return !player.getServer().isUnderSpawnProtection((ServerLevel) player.level(), pos, player);
    }
    
    public static boolean isAllowedToPlacePreview(Player player, LittleTile tile) throws LittleActionException {
        if (tile == null)
            return true;
        
        if (tile.hasColor() && ColorUtils.alpha(tile.color) < LittleTiles.CONFIG.getMinimumTransparency(player))
            throw new NotAllowedToPlaceColorException(LittleTiles.CONFIG.build.get(player));
        
        if (!LittleTiles.CONFIG.build.get(player).placeTransformableBoxes)
            for (LittleBox box : tile)
                if (box instanceof LittleTransformableBox)
                    throw new NotAllowedToPlaceTransformableException();
                
        return true;
    }
    
    public static boolean isAllowedToUse(LittleActionSource source, IGridBased grid) throws LittleActionException {
        if (!source.isPlayer())
            return true;
        LittlePermissionBuild build = LittleTiles.CONFIG.build.get(source.asPlayer());
        if (build.gridLimit.isEnabled() && build.gridLimit.value < grid.getSmallest())
            throw new GridTooHighException(build, grid.getSmallest());
        return true;
    }
    
    public static LittleIngredients getIngredients(IParentCollection parent, LittleElement element, LittleBox box) {
        return LittleIngredient.extract(element, box.getPercentVolume(parent.getGrid()));
    }
    
    public static LittleIngredients getIngredients(IParentCollection parent, LittleTile tile) {
        return LittleIngredient.extract(tile, tile.getPercentVolume(parent.getGrid()));
    }
    
    public static LittleIngredients getIngredients(IParentCollection parent, List<LittleTile> tiles) {
        LittleIngredients ingredients = new LittleIngredients();
        for (LittleTile tile : tiles)
            ingredients.add(LittleIngredient.extract(tile, tile.getPercentVolume(parent.getGrid())));
        return ingredients;
    }
    
    public static LittleIngredients getIngredients(HolderLookup.Provider provider, LittleGroup previews) {
        return LittleIngredient.extract(provider, previews);
    }
    
    public static LittleIngredients getIngredients(HolderLookup.Provider provider, LittleGroupAbsolute previews) {
        return LittleIngredient.extract(provider, previews.group);
    }
    
    public static LittleIngredients getIngredients(LittleTile tile, double volume) {
        return LittleIngredient.extract(tile, volume);
    }
    
    public static boolean canTake(LittleActionSource source, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (source.needsIngredients()) {
            try {
                inventory.startSimulation();
                inventory.take(source.getActionRegistry(), ingredients.copy());
                return true;
            } finally {
                inventory.stopSimulation();
            }
        }
        return true;
    }
    
    public static boolean checkAndTake(LittleActionSource source, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (source.needsIngredients()) {
            try {
                inventory.startSimulation();
                inventory.take(source.getActionRegistry(), ingredients.copy());
            } finally {
                inventory.stopSimulation();
            }
            inventory.take(source.getActionRegistry(), ingredients.copy());
        }
        return true;
    }
    
    public static boolean take(LittleActionSource source, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (source.needsIngredients())
            inventory.take(source.getActionRegistry(), ingredients.copy());
        return true;
    }
    
    public static boolean take(LittleActionSource source, LittleInventory inventory, ItemStack toDrain) throws NotEnoughIngredientsException {
        if (!source.needsIngredients())
            return true;
        
        String id = ItemPremadeStructure.getPremadeId(toDrain);
        for (ItemStack stack : inventory) {
            if (stack.getItem() == LittleTilesRegistry.PREMADE.value() && ItemPremadeStructure.getPremadeId(stack).equals(id)) {
                stack.shrink(1);
                return true;
            }
        }
        throw new NotEnoughIngredientsException(toDrain);
    }
    
    public static boolean canGive(LittleActionSource source, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (source.needsIngredients()) {
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
    
    public static boolean checkAndGive(LittleActionSource source, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (source.needsIngredients()) {
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
    
    public static boolean give(LittleActionSource source, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (source.needsIngredients())
            inventory.give(ingredients.copy());
        return true;
    }
    
    public static boolean giveOrDrop(LittleActionSource source, LittleInventory inventory, ParentCollection parent, List<LittleTile> tiles) {
        if (source.needsIngredients() && !tiles.isEmpty()) {
            try {
                checkAndGive(source, inventory, getIngredients(parent, tiles));
            } catch (NotEnoughIngredientsException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    public static List<ItemStack> getInventories(Player player) {
        List<ItemStack> inventories = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ILittleIngredientInventory)
                inventories.add(stack);
        }
        return inventories;
    }
    
    private static boolean isBlockValid(Block block) {
        if (LittleBlockRegistry.isSpecialBlock(block))
            return true;
        return block instanceof TransparentBlock || block instanceof StainedGlassBlock || block instanceof HalfTransparentBlock || block instanceof LeavesBlock;
    }
    
    private static boolean isBlockInvalid(Block block) {
        if (block instanceof EntityBlock || block instanceof SlabBlock)
            return true;
        
        if (block.builtInRegistryHolder().is(LittleTilesRegistry.INCOMPATIBLE_TAG))
            return true;
        
        if (block.builtInRegistryHolder().unwrapKey().get().location().getNamespace().equals("framedblocks"))
            return true;
        
        return false;
    }
    
    public static boolean isBlockValid(BlockState state) {
        if (isBlockInvalid(state.getBlock()))
            return false;
        if (isBlockValid(state.getBlock()))
            return true;
        
        if (state.isSolid() && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
            return true;
        if (ChiselsAndBitsManager.isChiselsAndBitsStructure(state))
            return true;
        try {
            return state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        } catch (Exception e) {
            return false;
        }
    }
    
}
