package com.creativemd.littletiles.common.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.mod.coloredlights.ColoredLightsManager;
import com.creativemd.littletiles.common.packet.LittleBlockUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleBlocksUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.ingredient.NotEnoughIngredientsException;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.config.LittleTilesConfig.AreaProtected;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToPlaceColorException;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.tile.LittleTile;

public abstract class LittleAction2 extends CreativeCorePacket {
    
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
    
    public static void sendEntityResetToClient(EntityPlayer player, EntityAnimation animation) {
        if (!(player instanceof EntityPlayerMP))
            return;
        PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
    }
    
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
            throw new NotAllowedToPlaceColorException(player);
        
        return true;
    }
    
    public static double getVolume(LittleGridContext context, List<PlacePreview> tiles) {
        double volume = 0;
        for (PlacePreview preview : tiles)
            volume += preview.box.getPercentVolume(context);
        return volume;
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
