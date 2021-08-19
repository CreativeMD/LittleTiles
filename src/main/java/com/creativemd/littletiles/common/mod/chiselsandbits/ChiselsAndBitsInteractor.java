package com.creativemd.littletiles.common.mod.chiselsandbits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import team.creative.littletiles.common.math.box.LittleBoxCombiner;

public class ChiselsAndBitsInteractor {
    
    public static boolean isChiselsAndBitsStructure(IBlockState state) {
        Block block = state.getBlock();
        Map blocks = ChiselsAndBits.getBlocks().getConversions();
        for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
            Block block2 = (Block) iterator.next();
            if (block == block2)
                return true;
        }
        return false;
    }
    
    public static boolean isChiselsAndBitsStructure(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        Map blocks = ChiselsAndBits.getBlocks().getConversions();
        for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
            Block block2 = (Block) iterator.next();
            if (block == block2)
                return true;
        }
        return false;
    }
    
    public static List<LittleTile> getTiles(VoxelBlob blob) {
        List<LittleTile> tiles = new ArrayList<>();
        for (int x = 0; x < ChiselsAndBitsManager.convertingFrom; x++) {
            for (int y = 0; y < ChiselsAndBitsManager.convertingFrom; y++) {
                for (int z = 0; z < ChiselsAndBitsManager.convertingFrom; z++) {
                    IBlockState state = ModUtil.getStateById(blob.get(x, y, z));
                    if (state.getBlock() == Blocks.WATER)
                        state = LittleTiles.dyeableBlockTransparent.getDefaultState()
                            .withProperty(BlockLittleDyeableTransparent.VARIANT, BlockLittleDyeableTransparent.LittleDyeableTransparent.WATER);
                    if (state.getBlock() != Blocks.AIR) {
                        LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
                        tile.setBox(new LittleBox(new LittleVec(x, y, z)));
                        tiles.add(tile);
                    }
                }
            }
        }
        LittleBoxCombiner.combine(tiles);
        return tiles;
    }
    
    public static LittlePreviews getPreviews(VoxelBlob blob) {
        List<LittleTile> tiles = getTiles(blob);
        LittlePreviews previews = new LittlePreviews(LittleGridContext.get(ChiselsAndBitsManager.convertingFrom));
        for (LittleTile tile : tiles) {
            previews.addWithoutCheckingPreview(tile.getPreviewTile());
        }
        return previews;
    }
    
    public static LittlePreviews getPreviews(ItemStack stack) {
        if (isChiselsAndBitsStructure(stack))
            return getPreviews(ModUtil.getBlobFromStack(stack, null));
        return null;
    }
    
    public static LittlePreviews getPreviews(TileEntity te) {
        if (te instanceof TileEntityBlockChiseled)
            return getPreviews(((TileEntityBlockChiseled) te).getBlob());
        return null;
    }
    
    public static boolean isChiselsAndBitsStructure(TileEntity te) {
        return te instanceof TileEntityBlockChiseled;
    }
    
    public static List<LittleTile> getTiles(TileEntity te) {
        if (te instanceof TileEntityBlockChiseled)
            return getTiles(((TileEntityBlockChiseled) te).getBlob());
        return null;
    }
    
    public static VoxelBlob getVoxelBlob(TileEntityLittleTiles te, boolean force) throws Exception {
        if (te.getContext().size > ChiselsAndBitsManager.convertingFrom)
            throw new Exception("Invalid grid size of " + te.getContext() + "!");
        
        LittleGridContext context = null;
        try {
            context = LittleGridContext.get(ChiselsAndBitsManager.convertingFrom);
            if (context == null)
                throw new Exception();
        } catch (Exception e) {
            throw new Exception("The grid-size 16 is not supported! Base=" + LittleGridContext.minSize + ", Multiplier=" + LittleGridContext.multiplier + ", Scale=" + LittleGridContext.gridSizes.length);
        }
        
        te.convertTo(context);
        
        LittleVec vec = new LittleVec(0, 0, 0);
        VoxelBlob blob = new VoxelBlob();
        for (Pair<IParentTileList, LittleTile> pair : te.allTiles()) {
            LittleTile tile = pair.value;
            boolean convert;
            if (tile.getClass() == LittleTile.class)
                convert = true;
            else if (force) {
                if (tile.getClass() == LittleTileColored.class)
                    convert = true;
                else
                    continue;
            } else
                throw new Exception("Cannot convert " + tile.getClass() + " tile!");
            
            if (convert) {
                if (!force && tile.getBox().getClass() != LittleBox.class)
                    throw new Exception("Cannot convert " + tile.getBox().getClass() + " box!");
                
                LittleBox box = new LittleBox(0, 0, 0, 0, 0, 0);
                for (int x = tile.getBox().minX; x < tile.getBox().maxX; x++)
                    for (int y = tile.getBox().minY; y < tile.getBox().maxY; y++)
                        for (int z = tile.getBox().minZ; z < tile.getBox().maxZ; z++) {
                            box.set(x, y, z, x + 1, y + 1, z + 1);
                            if (tile.getBox().isSolid() || tile.intersectsWith(box))
                                blob.set(x, y, z, Block.getStateId(tile.getBlockState()));
                        }
            }
        }
        
        te.convertToSmallest();
        
        return blob;
    }
}
