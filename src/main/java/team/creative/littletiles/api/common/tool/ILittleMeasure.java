package team.creative.littletiles.api.common.tool;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.math.measure.LittleMeasurement;

public interface ILittleMeasure extends ILittleTool {
    
    public List<LittleMeasurement> getMeasurements(Level level, ItemStack stack);
    
    public void setMeasurements(ItemStack stack, List<LittleMeasurement> measurements);
    
}
