package com.creativemd.littletiles.common.action.tool;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.location.TileLocation;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.ingredient.NotEnoughIngredientsException.NotEnoughSpaceException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionGlowstone extends LittleActionInteract {
    
    public LittleActionGlowstone(World world, BlockPos blockPos, EntityPlayer player) {
        super(world, blockPos, player);
    }
    
    public LittleActionGlowstone() {
        super();
    }
    
    @Override
    protected boolean isRightClick() {
        return true;
    }
    
    public TileLocation changedTile;
    
    @Override
    protected boolean action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
        if (stack.getItem() == Items.GLOWSTONE_DUST && player.isSneaking()) {
            if (needIngredients(player)) {
                if (tile.glowing) {
                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLOWSTONE_DUST)))
                        player.dropItem(new ItemStack(Items.GLOWSTONE_DUST), true);
                } else
                    stack.shrink(1);
            }
            
            if (tile.glowing)
                player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
            else
                player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
            
            tile.glowing = !tile.glowing;
            te.updateTiles();
            te.updateLighting();
            
            this.changedTile = new TileLocation(parent, tile);
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) throws LittleActionException {
        if (changedTile.find(player.world) != null)
            return new LittleActionGlowstoneRevert(changedTile);
        throw new LittleActionException.TileNotThereException();
    }
    
    @Override
    public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
        return null;
    }
    
    public static class LittleActionGlowstoneRevert extends LittleAction {
        
        public TileLocation location;
        
        public TileLocation newLocation;
        
        public LittleActionGlowstoneRevert(TileLocation location) {
            this.location = location;
        }
        
        public LittleActionGlowstoneRevert() {
            
        }
        
        @Override
        public boolean canBeReverted() {
            return true;
        }
        
        @Override
        public LittleAction revert(EntityPlayer player) throws LittleActionException {
            if (location.find(player.world) != null)
                return new LittleActionGlowstoneRevert(newLocation);
            throw new LittleActionException.TileNotThereException();
        }
        
        @Override
        protected boolean action(EntityPlayer player) throws LittleActionException {
            
            Pair<IParentTileList, LittleTile> pair = newLocation.find(player.world);
            LittleTile tile = pair.value;
            if (needIngredients(player)) {
                ItemStack stack = new ItemStack(Items.GLOWSTONE_DUST);
                if (!InventoryUtils.consumeItemStack(player.inventory, stack))
                    throw new NotEnoughSpaceException(stack);
            }
            
            if (tile.glowing)
                player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
            else
                player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
            
            tile.glowing = !tile.glowing;
            pair.key.getTe().updateTiles();
            pair.key.getTe().updateLighting();
            
            this.newLocation = new TileLocation(pair.key, tile);
            
            return false;
        }
        
        @Override
        public void writeBytes(ByteBuf buf) {
            writeTileLocation(location, buf);
        }
        
        @Override
        public void readBytes(ByteBuf buf) {
            location = readTileLocation(buf);
        }
        
        @Override
        public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
            return null;
        }
    }
    
}
