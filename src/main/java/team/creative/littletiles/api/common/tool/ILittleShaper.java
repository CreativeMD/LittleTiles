package team.creative.littletiles.api.common.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PreviewMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.LittleShapeInstance;

public interface ILittleShaper extends ILittleTool {
    
    public boolean hasShape(Player player, ItemStack stack);
    
    public default LittleShapeInstance getShape(ItemStack stack) {
        return LittleShapeInstance.getOrCreate(stack, defaultShape());
    }
    
    public LittleShape defaultShape();
    
    public boolean selectLeftClick(Player player, ItemStack stack);
    
    public PreviewMode previewMode(Player player, ItemStack stack);
    
    public boolean previewInside(Player player, ItemStack stack);
    
    public void shapeFinished(Level level, Player player, ItemStack stack, ShapeSelection selection, LittleBoxes boxes);
}
