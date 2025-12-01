package team.creative.littletiles.api.common.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.placement.PreviewMode;

public interface ILittleTransformer extends ILittleTool {
    
    public default PreviewMode previewMode(Player player, ItemStack stack) {
        return PreviewMode.LINES;
    }
    
    public default boolean previewInside(Player player, ItemStack stack) {
        return false;
    }
    
    public void boxFinished(Level level, Player player, ItemStack stack, LittleBoxAbsolute box);
    
}
