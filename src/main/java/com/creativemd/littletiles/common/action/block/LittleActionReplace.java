package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxesSimple;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionReplace extends LittleActionInteract {
    
    public LittlePreview toReplace;
    
    public LittleActionReplace(World world, BlockPos blockPos, EntityPlayer player, LittlePreview toReplace) {
        super(world, blockPos, player);
        this.toReplace = toReplace;
    }
    
    public LittleActionReplace() {
        
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        super.writeBytes(buf);
        NBTTagCompound nbt = new NBTTagCompound();
        toReplace.writeToNBT(nbt);
        writeNBT(buf, nbt);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        super.readBytes(buf);
        toReplace = LittleTileRegistry.loadPreview(readNBT(buf));
    }
    
    @Override
    protected boolean isRightClick() {
        return false;
    }
    
    @Override
    protected boolean requiresBreakEvent() {
        return true;
    }
    
    public LittleAbsolutePreviews replacedTiles;
    public LittleBoxes boxes;
    
    @Override
    protected boolean action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
        
        replacedTiles = new LittleAbsolutePreviews(pos, te.getContext());
        boxes = new LittleBoxesSimple(pos, te.getContext());
        
        if (LittleTiles.CONFIG.isTransparencyRestricted(player))
            isAllowedToPlacePreview(player, toReplace);
        
        if (BlockTile.selectEntireBlock(player, secondMode)) {
            List<LittleTile> toRemove = new ArrayList<>();
            LittlePreviews toBePlaced = new LittlePreviews(te.getContext());
            LittlePreviews previews = new LittlePreviews(te.getContext());
            
            parent = te.noneStructureTiles();
            for (LittleTile toDestroy : parent) {
                if (tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile)) {
                    replacedTiles.addTile(parent, toDestroy);
                    boxes.addBox(parent, toDestroy);
                    
                    toBePlaced.addTile(parent, toDestroy);
                    LittlePreview preview = toReplace.copy();
                    preview.box = toDestroy.getBox();
                    previews.addWithoutCheckingPreview(preview);
                    
                    toRemove.add(toDestroy);
                }
            }
            
            if (toRemove.isEmpty())
                return false;
            
            LittleInventory inventory = new LittleInventory(player);
            
            try {
                inventory.startSimulation();
                take(player, inventory, getIngredients(toBePlaced));
                give(player, inventory, getIngredients(replacedTiles));
            } finally {
                inventory.stopSimulation();
            }
            
            te.updateTilesSecretly(x -> x.noneStructureTiles().removeAll(toRemove));
            
            Placement placement = new Placement(player, PlacementHelper.getAbsolutePreviews(world, previews, pos, PlacementMode.normal));
            take(player, inventory, getIngredients(placement.place().placedPreviews));
            checkAndGive(player, inventory, placement.overflow());
            checkAndGive(player, inventory, getIngredients(replacedTiles));
        } else {
            if (parent.isStructure())
                return false;
            
            LittleInventory inventory = new LittleInventory(player);
            
            replacedTiles.addTile(parent, tile);
            boxes.addBox(parent, tile);
            
            LittlePreviews toBePlaced = new LittlePreviews(te.getContext());
            toReplace.box = tile.getBox().copy();
            toBePlaced.addPreview(null, toReplace, te.getContext());
            
            try {
                inventory.startSimulation();
                take(player, inventory, getIngredients(toBePlaced));
                give(player, inventory, getIngredients(replacedTiles));
            } finally {
                inventory.stopSimulation();
            }
            
            te.updateTilesSecretly((x) -> x.noneStructureTiles().remove(tile));
            
            LittlePreviews previews = new LittlePreviews(te.getContext());
            previews.addWithoutCheckingPreview(toReplace);
            
            Placement placement = new Placement(player, PlacementHelper.getAbsolutePreviews(world, previews, pos, PlacementMode.normal));
            take(player, inventory, getIngredients(placement.place().placedPreviews));
            checkAndGive(player, inventory, placement.overflow());
            checkAndGive(player, inventory, getIngredients(replacedTiles));
        }
        
        world.playSound((EntityPlayer) null, pos, tile.getSound()
            .getBreakSound(), SoundCategory.BLOCKS, (tile.getSound().getVolume() + 1.0F) / 2.0F, tile.getSound().getPitch() * 0.8F);
        
        return true;
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) throws LittleActionException {
        return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(replacedTiles, PlacementMode.normal));
    }
    
    @Override
    public LittleAction flip(Axis axis, LittleAbsoluteBox absoluteBox) {
        LittleBoxes boxes = this.boxes.copy();
        boxes.flip(axis, absoluteBox);
        
        LittleAbsolutePreviews previews = new LittleAbsolutePreviews(boxes.pos, boxes.context);
        for (LittleBox box : boxes.all()) {
            LittlePreview preview = toReplace.copy();
            preview.box = box;
            previews.addWithoutCheckingPreview(preview);
        }
        return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(previews, PlacementMode.normal));
    }
}
