package com.creativemd.littletiles.common.tile.place;

import java.util.Collections;
import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class PlacePreviewRelative extends PlacePreview {
    
    public StructureDirectionalField relativeType;
    public StructureRelative relative;
    
    public PlacePreviewRelative(LittleBox box, StructureRelative relative, StructureDirectionalField relativeType) {
        super(box.copy(), null);
        this.relative = relative;
        this.relativeType = relativeType;
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
        return new PlacePreviewRelative(box.copy(), relative, relativeType);
    }
    
    @Override
    public List<LittleRenderBox> getPreviews(LittleGrid context) {
        List<LittleRenderBox> cubes = super.getPreviews(context);
        for (LittleRenderBox cube : cubes)
            cube.color = relativeType.annotation.color();
        return cubes;
    }
    
    @Override
    public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleStructure structure, boolean requiresCollisionTest) {
        relative.setBox(BlockPos.ORIGIN, box.copy(), block.getContext());
        relative.add(block.pos.subtract(structure.getPos()));
        relativeType.set(structure, relative);
        return Collections.EMPTY_LIST;
    }
    
}
