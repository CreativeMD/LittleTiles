package team.creative.littletiles.client.render.tile;

import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleRenderBox extends RenderBox {
    
    public LittleBox box;
    
    public LittleRenderBox(AlignedBox box) {
        super(box);
    }
    
    public LittleRenderBox(AlignedBox box, BlockState state) {
        super(box, state);
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), (BlockState) null);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, BlockState state) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), state);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, LittleElement element) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), element.getState());
        this.color = element.color;
        this.box = box;
    }
    
    @Override
    public LittleRenderBox setColor(int color) {
        return (LittleRenderBox) super.setColor(color);
    }
    
}
