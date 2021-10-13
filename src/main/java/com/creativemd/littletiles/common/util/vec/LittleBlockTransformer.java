package com.creativemd.littletiles.common.util.vec;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;
import team.creative.littletiles.common.tile.parent.StructureParentCollection;

public class LittleBlockTransformer {
    
    @SuppressWarnings("deprecation")
    public static void mirror(BETiles be, Axis axis) {
        if (axis == null)
            return;
        
        LittleGrid grid = be.getGrid();
        for (IParentCollection parent : be.groups()) {
            if (parent.isStructure()) {
                if (parent.isMain()) {
                    try {
                        parent.getStructure().flipForWarpDrive(grid, axis);
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                } else
                    ((StructureParentCollection) parent).flipForWarpDrive(axis);
            }
            for (LittleTile tile : parent)
                mirrorTile(grid, tile, axis);
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void rotate(BETiles be, Rotation rotation, int steps) {
        if (rotation == null)
            return;
        
        LittleGrid grid = be.getGrid();
        for (IParentCollection parent : be.groups()) {
            if (parent.isStructure()) {
                if (parent.isMain()) {
                    try {
                        parent.getStructure().rotateForWarpDrive(grid, rotation, steps);
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                } else
                    ((StructureParentCollection) parent).rotateForWarpDrive(rotation, steps);
            }
            for (LittleTile tile : parent)
                for (int rotationStep = 0; rotationStep < steps; rotationStep++)
                    rotateTile(grid, tile, rotation);
        }
    }
    
    public static void mirrorTile(LittleGrid grid, LittleTile tile, Axis axis) {
        tile.mirror(axis, grid.rotationCenter);
    }
    
    public static void rotateTile(LittleGrid grid, LittleTile tile, Rotation rotation) {
        tile.rotate(rotation, grid.rotationCenter);
    }
}
