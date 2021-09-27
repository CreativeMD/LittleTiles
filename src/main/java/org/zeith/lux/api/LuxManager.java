package org.zeith.lux.api;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.zeith.lux.api.light.ILightBlockHandler;

import com.zeitheron.hammercore.api.lighting.ColoredLight;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//Modified to not cause compiler errors

public class LuxManager {
    
    public static void registerBlockLight(Block blk, ILightBlockHandler handler) {}
    
    public static ArrayList<ColoredLight> getLights(World world, BlockPos pos, IBlockState state, @Nullable TileEntity tile, float partialTicks) {
        return null;
    }
    
}
