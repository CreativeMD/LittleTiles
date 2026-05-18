package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleMeasure;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.LittleToolMeasure;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiMesaurementTape;
import team.creative.littletiles.common.item.component.MeasurementsComponent;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.measure.LittleMeasurement;

public class ItemLittleMeasureTape extends Item implements ILittleMeasure, IItemTooltip {
    
    public ItemLittleMeasureTape() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public List<LittleMeasurement> getMeasurements(ItemStack stack) {
        return MeasurementsComponent.get(stack);
    }
    
    @Override
    public void setMeasurements(ItemStack stack, List<LittleMeasurement> measurements) {
        stack.set(LittleTilesRegistry.MEASUREMENTS, MeasurementsComponent.of(measurements));
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return new GuiMesaurementTape(view);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object[] tooltipData(ItemStack stack) {
        var MC = Minecraft.getInstance();
        return new Object[] { MC.options.keyUse.getTranslatedKeyMessage(), MC.options.keyAttack.getTranslatedKeyMessage(), MC.options.keyAttack
                .getTranslatedKeyMessage(), MC.options.keyAttack.getTranslatedKeyMessage() };
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleTool tool(ItemStack stack) {
        return new LittleToolMeasure(stack);
    }
    
}
