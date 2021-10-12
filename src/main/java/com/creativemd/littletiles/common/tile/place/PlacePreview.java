package com.creativemd.littletiles.common.tile.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.tile.LittleTile;

public class PlacePreview {
    
    public LittleBox box;
    public LittlePreview preview;
    
    public PlacePreview(LittleBox box, LittlePreview preview) {
        this.box = box;
        this.preview = preview;
    }
    
    /** NEEDS TO BE OVERRIDEN! ALWAYS! **/
    public PlacePreview copy() {
        return new PlacePreview(box.copy(), preview.copy());
    }
    
    /** If false it will be placed after all regular tiles have been placed **/
    public boolean needsCollisionTest() {
        return true;
    }
    
    public List<LittleRenderBox> getPreviews(LittleGridContext context) {
        ArrayList<LittleRenderBox> previews = new ArrayList<>();
        previews.add(box.getRenderingCube(context, null, 0));
        return previews;
    }
    
    public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleStructure structure, boolean requiresCollisionTest) throws LittleActionException {
        LittleTile LT = preview.getLittleTile();
        
        if (LT == null)
            return Collections.EMPTY_LIST;
        
        LT.setBox(box.copy());
        return placement.mode.placeTile(placement, block, parent, structure, LT, requiresCollisionTest);
    }
    
    public PlacePreview copyWithBox(LittleBox box) {
        PlacePreview tile = this.copy();
        tile.box = box;
        return tile;
    }
    
    public boolean split(LittleGridContext context, HashMapList<BlockPos, PlacePreview> tiles, BlockPos pos, LittleBoxReturnedVolume volume) {
        if (!requiresSplit()) {
            tiles.add(pos, this);
            return true;
        }
        
        HashMapList<BlockPos, LittleBox> boxes = new HashMapList<>();
        this.box.split(context, pos, boxes, volume);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.entrySet()) {
            for (LittleBox box : entry.getValue()) {
                tiles.add(entry.getKey(), this.copyWithBox(box));
            }
        }
        
        return true;
    }
    
    public boolean requiresSplit() {
        return true;
    }
    
    public void add(LittleVec vec) {
        box.add(vec);
    }
    
    public void convertTo(LittleGrid context, LittleGrid to) {
        box.convertTo(context, to);
    }
    
    public int getSmallestContext(LittleGrid context) {
        return box.getSmallestContext(context);
    }
    
}
