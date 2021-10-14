package com.creativemd.littletiles.common.tile.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacePreviewFacing extends PlacePreview {
    
    public Facing facing;
    public int color;
    
    public PlacePreviewFacing(LittleBox box, Facing facing, int color) {
        super(box.copy(), null);
        this.facing = facing;
        this.color = color;
    }
    
    @Override
    public boolean needsCollisionTest() {
        return false;
    }
    
    @Override
    public boolean requiresSplit() {
        return false;
    }
    
    @Override
    public PlacePreview copy() {
        return new PlacePreviewFacing(box.copy(), facing, color);
    }
    
    @Override
    public List<LittleRenderBox> getPreviews(LittleGrid context) {
        List<LittleRenderBox> cubes = new ArrayList<>();
        LittleRenderBox cube = new LittleRenderBox(box.getCube(context), box, LittleTiles.dyeableBlock, 0);
        cube.setColor(color);
        float thickness = 1 / 32F;
        Axis axis = facing.getAxis();
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + thickness);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - thickness);
        }
        cubes.add(cube);
        return cubes;
    }
    
    @Override
    public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleStructure structure, boolean requiresCollisionTest) {
        return Collections.EMPTY_LIST;
    }
    
}
