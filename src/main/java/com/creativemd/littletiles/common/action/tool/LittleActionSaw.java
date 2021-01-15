package com.creativemd.littletiles.common.action.tool;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.location.TileLocation;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredient;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.ColorIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LittleActionSaw extends LittleActionInteract {
    
    public boolean toLimit;
    public LittleGridContext context;
    
    public LittleActionSaw(World world, BlockPos blockPos, EntityPlayer player, boolean toLimit, LittleGridContext context) {
        super(world, blockPos, player);
        this.toLimit = toLimit;
        this.context = context;
    }
    
    public LittleActionSaw() {
        super();
    }
    
    @Override
    protected boolean isRightClick() {
        return true;
    }
    
    public LittleBox oldBox = null;
    public LittleBoxes newBoxes;
    public TileLocation location = null;
    public EnumFacing facing;
    
    @Override
    public RayTraceResult rayTrace(TileEntityLittleTiles te, LittleTile tile, Vec3d pos, Vec3d look) {
        return new LittleBox(tile.getBox()).calculateIntercept(te.getContext(), te.getPos(), pos, look);
    }
    
    @Override
    protected boolean action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
        
        facing = moving.sideHit;
        if (!parent.isStructure() && tile.canSawResizeTile(facing, player)) {
            LittleBox box;
            
            Axis axis = facing.getAxis();
            
            if (te.getContext() != context) {
                if (context.size > te.getContext().size)
                    te.convertTo(context);
                else
                    context = te.getContext();
            }
            
            oldBox = tile.getBox().copy();
            
            boolean outside = false;
            
            if (secondMode)
                box = tile.getBox().shrink(facing, toLimit);
            else {
                box = tile.getBox().grow(facing);
                if (tile.getBox().isFaceAtEdge(te.getContext(), facing)) {
                    BlockPos newPos = te.getPos().offset(facing);
                    
                    box = box.createOutsideBlockBox(te.getContext(), facing);
                    if (box == null)
                        return false;
                    
                    LittleGridContext context = LittleGridContext.get(box.getSmallestContext(te.getContext()));
                    te = loadTe(player, world, newPos, null, false, 0);
                    
                    if (te == null)
                        return false;
                    
                    if (context != te.getContext()) {
                        if (context.size > te.getContext().size)
                            te.convertTo(context);
                        else
                            box.convertTo(context, te.getContext());
                    }
                    
                    outside = true;
                }
                
                if (toLimit) {
                    LittleBox before = null;
                    while (!box.isFaceAtEdge(te.getContext(), facing) && te.isSpaceForLittleTile(box, (x, y) -> y != tile)) {
                        before = box;
                        box = box.grow(facing);
                    }
                    if (!te.isSpaceForLittleTile(box, (x, y) -> y != tile))
                        box = before;
                } else if (!te.isSpaceForLittleTile(box, (x, y) -> y != tile))
                    box = null;
            }
            
            if (box != null) {
                double amount = outside ? box.getPercentVolume(te.getContext()) : Math.abs(box.getPercentVolume(te.getContext()) - tile.getBox().getPercentVolume(te.getContext()));
                LittleIngredients ingredients = new LittleIngredients();
                LittleInventory inventory = new LittleInventory(player);
                BlockIngredient blocks = new BlockIngredient();
                LittlePreview preview = tile.getPreviewTile();
                BlockIngredientEntry block = preview.getBlockIngredient(te.getContext());
                if (block != null) {
                    block.value = amount;
                    blocks.add(block);
                    ingredients.set(blocks.getClass(), blocks);
                    
                    ColorIngredient unit = null;
                    if (preview.hasColor()) {
                        unit = ColorIngredient.getColors(preview.getColor());
                        unit.scaleLoose(amount);
                        ingredients.set(unit.getClass(), unit);
                    }
                    
                    if (secondMode)
                        give(player, inventory, ingredients);
                    else
                        take(player, inventory, ingredients);
                    
                }
                
                if (outside) {
                    LittleTile newTile = tile.copy();
                    newTile.setBox(box);
                    te.updateTiles((x) -> x.noneStructureTiles().add(newTile));
                    newBoxes = new LittleBoxes(te.getPos(), te.getContext());
                    newBoxes.addBox(parent, newTile);
                    te.convertToSmallest();
                    return true;
                } else {
                    tile.setBox(box);
                    te.updateTiles();
                    location = new TileLocation(parent, tile);
                    te.convertToSmallest();
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) {
        if (newBoxes != null)
            return new LittleActionDestroyBoxes(newBoxes);
        return new LittleActionSawRevert(context, location, oldBox, facing);
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        super.writeBytes(buf);
        buf.writeBoolean(toLimit);
        writeContext(context, buf);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        super.readBytes(buf);
        toLimit = buf.readBoolean();
        context = readContext(buf);
    }
    
    @Override
    public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
        return null;
    }
    
    public static class LittleActionSawRevert extends LittleAction {
        
        public LittleBox oldBox;
        public LittleBox replacedBox;
        public TileLocation location;
        public TileLocation newLocation;
        public EnumFacing facing;
        public LittleGridContext context;
        
        public LittleActionSawRevert(LittleGridContext context, TileLocation location, LittleBox oldBox, EnumFacing facing) {
            this.location = location;
            this.oldBox = oldBox;
            this.facing = facing;
            this.context = context;
        }
        
        public LittleActionSawRevert() {
            
        }
        
        @Override
        public boolean canBeReverted() {
            return true;
        }
        
        @Override
        public LittleAction revert(EntityPlayer player) throws LittleActionException {
            return new LittleActionSawRevert(context, newLocation, replacedBox, facing.getOpposite());
        }
        
        @Override
        protected boolean action(EntityPlayer player) throws LittleActionException {
            
            Pair<IParentTileList, LittleTile> pair = location.find(player.world);
            LittleTile tile = pair.value;
            if (tile.canSawResizeTile(facing, player)) {
                
                if (context != pair.key.getContext()) {
                    if (context.size > pair.key.getContext().size)
                        pair.key.getTe().convertTo(context);
                    else {
                        oldBox.convertTo(context, pair.key.getContext());
                        context = pair.key.getContext();
                    }
                }
                
                double amount = Math.abs(oldBox.getPercentVolume(context) - tile.getBox().getPercentVolume(pair.key.getContext()));
                
                LittlePreview preview = tile.getPreviewTile();
                LittleIngredients ingredients = new LittleIngredients();
                BlockIngredient blocks = new BlockIngredient();
                BlockIngredientEntry block = preview.getBlockIngredient(pair.key.getContext());
                if (block != null) {
                    LittleInventory inventory = new LittleInventory(player);
                    block.value = amount;
                    blocks.add(block);
                    ingredients.set(blocks.getClass(), blocks);
                    
                    ColorIngredient unit = null;
                    if (preview.hasColor()) {
                        unit = ColorIngredient.getColors(preview.getColor());
                        unit.scaleLoose(amount);
                        ingredients.set(unit.getClass(), unit);
                    }
                    
                    if (oldBox.getVolume() < tile.getBox().getVolume())
                        give(player, inventory, ingredients);
                    else
                        take(player, inventory, ingredients);
                }
                
                replacedBox = tile.getBox().copy();
                tile.setBox(oldBox.copy());
                
                pair.key.getTe().convertToSmallest();
                pair.key.getTe().updateTiles();
                
                newLocation = new TileLocation(pair.key, tile);
                return true;
            }
            
            return false;
        }
        
        @Override
        public void writeBytes(ByteBuf buf) {
            writeTileLocation(location, buf);
            writeLittleBox(oldBox, buf);
            writeFacing(buf, facing);
            writeContext(context, buf);
        }
        
        @Override
        public void readBytes(ByteBuf buf) {
            location = readTileLocation(buf);
            oldBox = readLittleBox(buf);
            facing = readFacing(buf);
            context = readContext(buf);
        }
        
        @Override
        public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
            return null;
        }
    }
}
