package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeRenderItem;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.api.tool.ILittlePlacer;

public class LittleRenderTool extends CreativeRenderItem {
    
    @Override
    public List<? extends RenderBox> getBoxes(ItemStack stack, RenderType layer) {
        ILittlePlacer tool = (ILittlePlacer) stack.getItem();
        if (tool.hasTiles(stack))
            return tool.getTiles(stack).getRenderingBoxes(layer == Sheets.translucentCullBlockSheet());
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public boolean hasTranslucentLayer(ItemStack stack) {
        return stack.getTag().getBoolean("translucent");
    }
    
    @Override
    public void saveCachedModel(Facing facing, RenderType layer, List<BakedQuad> cachedQuads, ItemStack stack, boolean threaded) {}
    
    @Override
    public List<BakedQuad> getCachedModel(Facing facing, RenderType layer, ItemStack stack, boolean threaded) {
        return LittleTilesClient.ITEM_RENDER_CACHE.requestCache(stack, layer, facing);
    }
    
}
