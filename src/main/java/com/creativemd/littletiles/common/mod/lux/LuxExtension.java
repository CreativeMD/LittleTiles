package com.creativemd.littletiles.common.mod.lux;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.zeitheron.hammercore.api.lighting.ColoredLight;
import com.zeitheron.hammercore.api.lighting.impl.IGlowingBlock;
import com.zeitheron.lux.api.LuxManager;
import com.zeitheron.lux.api.light.ILightBlockHandler;

import net.minecraft.tileentity.TileEntity;

public class LuxExtension {
    
    public static void init() {
        ILightBlockHandler handler = (world, pos, state, e) -> {
            
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityLittleTiles) {
                TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
                
                for (Pair<IParentTileList, LittleTile> pair : te.allTiles()) {
                    LittleTile tile = pair.value;
                    if (tile.getBlock() instanceof IGlowingBlock) {
                        ColoredLight light = ((IGlowingBlock) tile.getBlock()).produceColoredLight(world, pos, state, e.getPartialTicks());
                        
                        int color = ColorUtils.RGBAToInt(light.r, light.g, light.b, light.a);
                        if (tile instanceof LittleTileColored)
                            color = ColorUtils.blend(color, ((LittleTileColored) tile).color);
                        e.add(ColoredLight.builder().pos(tile.getCompleteBox().getBox(te.getContext(), te.getPos()).getCenter())
                            .color(ColorUtils.getRedDecimal(color), ColorUtils.getGreenDecimal(color), ColorUtils.getBlueDecimal(color), ColorUtils.getAlphaDecimal(color))
                            .radius((float) (light.radius * tile.getPercentVolume(te.getContext()))));
                    }
                }
            }
        };
        LuxManager.registerBlockLight(LittleTiles.blockTileNoTicking, handler);
        LuxManager.registerBlockLight(LittleTiles.blockTileNoTickingRendered, handler);
        LuxManager.registerBlockLight(LittleTiles.blockTileTicking, handler);
        LuxManager.registerBlockLight(LittleTiles.blockTileTickingRendered, handler);
    }
    
}
