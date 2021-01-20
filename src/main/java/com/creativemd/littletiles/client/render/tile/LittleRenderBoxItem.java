package com.creativemd.littletiles.client.render.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import com.creativemd.creativecore.client.rendering.model.CreativeBakedQuad;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.littletiles.common.structure.type.LittleItemHolder;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class LittleRenderBoxItem extends LittleRenderBox {
    
    private static final Vector3f defaultDirection = new Vector3f(1, 1, 0);
    private static final Vector3f center = new Vector3f(0.5F, 0.5F, 0.5F);
    
    public final LittleItemHolder structure;
    
    public LittleRenderBoxItem(LittleItemHolder structure, AlignedBox cube, LittleBox box) {
        super(cube, box, Blocks.AIR, 0);
        this.structure = structure;
        this.allowOverlap = true;
        this.keepVU = true;
    }
    
    @Override
    public List<BakedQuad> getBakedQuad(IBlockAccess world, @Nullable BlockPos pos, BlockPos offset, IBlockState state, IBakedModel blockModel, EnumFacing facing, BlockRenderLayer layer, long rand, boolean overrideTint, int defaultColor) {
        if (facing != structure.facing)
            return Collections.EMPTY_LIST;
        IBakedModel bakedmodel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(structure.stack, null, null);
        List<BakedQuad> blockQuads = new ArrayList<>(bakedmodel.getQuads(null, null, 0L));
        
        for (EnumFacing face : EnumFacing.values()) {
            List<BakedQuad> newQuads = bakedmodel.getQuads(null, face, 0L);
            blockQuads.addAll(newQuads);
        }
        
        Vector3f topRight = new Vector3f(defaultDirection);
        Rotation rotation;
        int rotationSteps = 1;
        boolean flipX = false;
        boolean flipY = false;
        boolean flipZ = false;
        switch (structure.facing) {
        case EAST:
            rotation = Rotation.Y_COUNTER_CLOCKWISE;
            flipX = true;
            break;
        case WEST:
            rotation = Rotation.Y_CLOCKWISE;
            flipX = true;
            break;
        case UP:
            flipY = false;
            rotation = Rotation.X_COUNTER_CLOCKWISE;
            break;
        case DOWN:
            rotation = Rotation.X_CLOCKWISE;
            break;
        case SOUTH:
            rotation = null;
            break;
        case NORTH:
            rotation = Rotation.Y_COUNTER_CLOCKWISE;
            rotationSteps = 2;
            flipZ = true;
            break;
        default:
            rotation = null;
            break;
        }
        
        if (rotation != null)
            for (int i = 0; i < rotationSteps; i++)
                RotationUtils.rotate(topRight, rotation);
        if (structure.topRight.x != 0 && structure.topRight.x == topRight.x)
            flipX = true;
        if (structure.topRight.y != 0 && structure.topRight.y != topRight.y)
            flipY = true;
        if (structure.topRight.z != 0 && structure.topRight.z == topRight.z)
            flipZ = true;
        
        float scale;
        switch (structure.facing.getAxis()) {
        case X:
            scale = Math.min(getSize(Axis.Y), getSize(Axis.Z));
            break;
        case Y:
            scale = Math.min(getSize(Axis.X), getSize(Axis.Z));
            break;
        case Z:
            scale = Math.min(getSize(Axis.X), getSize(Axis.Y));
            break;
        default:
            scale = 1;
            break;
        }
        
        float offsetX = (minX + maxX) * 0.5F - 0.5F;
        float offsetY = (minY + maxY) * 0.5F - 0.5F;
        float offsetZ = (minZ + maxZ) * 0.5F - 0.5F;
        
        boolean reverse = ((flipX ? 1 : 0) + (flipY ? 1 : 0) + (flipZ ? 1 : 0)) % 2 == 1;
        
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < blockQuads.size(); i++) {
            int[] originalData = blockQuads.get(i).getVertexData();
            CreativeBakedQuad quad = new CreativeBakedQuad(blockQuads.get(i), this, defaultColor, overrideTint, null);
            
            for (int k = 0; k < 4; k++) {
                int index = k * quad.getFormat().getIntegerSize();
                Vector3f vec = new Vector3f(Float.intBitsToFloat(originalData[index]), Float.intBitsToFloat(originalData[index + 1]), Float
                    .intBitsToFloat(originalData[index + 2]));
                
                vec.sub(center);
                
                if (rotation != null)
                    for (int j = 0; j < rotationSteps; j++)
                        RotationUtils.rotate(vec, rotation);
                if (flipX)
                    vec.x = -vec.x;
                if (flipY)
                    vec.y = -vec.y;
                if (flipZ)
                    vec.z = -vec.z;
                
                vec.x *= scale;
                vec.y *= scale;
                vec.z *= scale;
                vec.x += offsetX;
                vec.y += offsetY;
                vec.z += offsetZ;
                
                vec.add(center);
                
                int newIndex = index;
                if (reverse)
                    newIndex = (3 - k) * quad.getFormat().getIntegerSize();
                quad.getVertexData()[newIndex] = Float.floatToIntBits(vec.x + offset.getX());
                quad.getVertexData()[newIndex + 1] = Float.floatToIntBits(vec.y + offset.getY());
                quad.getVertexData()[newIndex + 2] = Float.floatToIntBits(vec.z + offset.getZ());
                if (reverse)
                    for (int j = 3; j < quad.getFormat().getIntegerSize(); j++)
                        quad.getVertexData()[newIndex + j] = originalData[index + j];
            }
            quads.add(quad);
        }
        return quads;
        
    }
    
    @Override
    public boolean intersectsWithFace(EnumFacing facing, RenderInformationHolder holder, BlockPos offset) {
        return true;
    }
    
}
