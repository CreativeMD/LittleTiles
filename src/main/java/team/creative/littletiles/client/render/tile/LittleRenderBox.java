package team.creative.littletiles.client.render.tile;

import net.minecraft.world.level.block.Block;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleRenderBox extends RenderBox {
    
    public LittleBox box;
    
    public LittleRenderBox(AlignedBox cube, LittleBox box, Block block, int color) {
        super(cube, block);
        this.color = color;
        this.box = box;
    }
    
}
