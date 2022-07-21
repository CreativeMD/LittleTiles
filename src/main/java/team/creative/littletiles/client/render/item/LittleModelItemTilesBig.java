package team.creative.littletiles.client.render.item;

import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

public class LittleModelItemTilesBig extends LittleModelItemTiles {
    
    @Override
    public List<? extends RenderBox> getBoxes(ItemStack stack, RenderType layer) {
        List<? extends RenderBox> boxes = super.getBoxes(stack, layer);
        LittleGroup.shrinkCubesToOneBlock(boxes);
        return boxes;
    }
    
}
