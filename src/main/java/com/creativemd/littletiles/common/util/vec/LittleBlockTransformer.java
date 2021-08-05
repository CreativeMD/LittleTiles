package com.creativemd.littletiles.common.util.vec;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import team.creative.littletiles.common.tile.parent.StructureParentCollection;

public class LittleBlockTransformer {
    
    public static void flipTE(TileEntityLittleTiles te, Axis axis) {
        if (axis == null)
            return;
        
        LittleGridContext context = te.getContext();
        for (IParentTileList parent : te.groups()) {
            if (parent.isStructure()) {
                if (parent.isMain()) {
                    try {
                        parent.getStructure().flipForWarpDrive(context, axis);
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                } else
                    ((StructureParentCollection) parent).flipForWarpDrive(axis);
            }
            for (LittleTile tile : parent)
                flipTile(context, tile, axis);
        }
    }
    
    public static void rotateTE(TileEntityLittleTiles te, Rotation rotation, int steps) {
        if (rotation == null)
            return;
        
        LittleGridContext context = te.getContext();
        for (IParentTileList parent : te.groups()) {
            if (parent.isStructure()) {
                if (parent.isMain()) {
                    try {
                        parent.getStructure().rotateForWarpDrive(context, rotation, steps);
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                } else
                    ((StructureParentCollection) parent).rotateForWarpDrive(rotation, steps);
            }
            for (LittleTile tile : parent)
                for (int rotationStep = 0; rotationStep < steps; rotationStep++)
                    rotateTile(context, tile, rotation);
        }
    }
    
    public static void flipTile(LittleGridContext context, LittleTile tile, Axis axis) {
        tile.getBox().flipBox(axis, context.rotationCenter);
    }
    
    public static void rotateTile(LittleGridContext context, LittleTile tile, Rotation rotation) {
        tile.getBox().rotateBox(rotation, context.rotationCenter);
    }
}
