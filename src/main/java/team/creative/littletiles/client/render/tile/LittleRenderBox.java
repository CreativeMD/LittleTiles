package team.creative.littletiles.client.render.tile;

import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleRenderBox extends RenderBox {
    
    public LittleBox box;
    
    public LittleRenderBox(LittleGrid grid, LittleBox box) {
        super((float) grid.toVanillaGrid(box.minX), (float) grid.toVanillaGrid(box.minY), (float) grid.toVanillaGrid(box.minZ), (float) grid.toVanillaGrid(box.maxX), (float) grid
                .toVanillaGrid(box.maxY), (float) grid.toVanillaGrid(box.maxZ), null);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, LittleElement element) {
        super((float) grid.toVanillaGrid(box.minX), (float) grid.toVanillaGrid(box.minY), (float) grid.toVanillaGrid(box.minZ), (float) grid.toVanillaGrid(box.maxX), (float) grid
                .toVanillaGrid(box.maxY), (float) grid.toVanillaGrid(box.maxZ), element.getState());
        this.color = element.color;
        this.box = box;
    }
    
}
