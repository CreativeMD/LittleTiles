package com.creativemd.littletiles.client.render.tile;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;

import net.minecraft.block.Block;

public class LittleRenderBox extends RenderBox {
    
    public LittleBox box;
    
    public LittleRenderBox(AlignedBox cube, LittleBox box, Block block, int meta) {
        super(cube, block, meta);
        this.box = box;
    }
    
    @Override
    public LittleRenderBox setColor(int color) {
        return (LittleRenderBox) super.setColor(color);
    }
}
