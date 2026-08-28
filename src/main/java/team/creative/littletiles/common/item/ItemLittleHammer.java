package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.ItemGuiCreator;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleShaper;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.shaper.LittleToolShaper;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiHammer;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PreviewMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.LittleShapeInstance;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class ItemLittleHammer extends Item implements ILittleShaper, IItemTooltip, ItemGuiCreator {
    
    public ItemLittleHammer() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LittleShapeInstance shape = getShape(stack);
        shape.appendInformation(stack, context, tooltip, flag);
    }
    
    @Override
    public boolean hasShape(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    public LittleShape defaultShape() {
        return ShapeRegistry.DRAG_BOX;
    }
    
    @Override
    public boolean selectLeftClick(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean previewInside(Player player, ItemStack stack) {
        return true;
    }
    
    @Override
    public PreviewMode previewMode(Player player, ItemStack stack) {
        return PreviewMode.LINES;
    }
    
    @Override
    public void shapeFinished(Level level, Player player, ItemStack stack, ShapeSelection selection, LittleBoxes boxes) {
        var filter = stack.get(LittleTilesRegistry.FILTER);
        if (filter != null && filter.hasFilter())
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionDestroyBoxesFiltered(level, boxes, filter.getFilter()));
        else
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionDestroyBoxes(level, boxes));
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
    public GuiLayer create(CompoundTag nbt, Player player) {
        return getConfigure(player, ContainerSlotView.mainHand(player), false);
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view, boolean secondary) {
        return new GuiHammer(view);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).translatable(), LittleTilesClient.KEY_MARK.getTranslatedKeyMessage(), LittleTilesClient
                .arrowKeysTooltip(), LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage(), LittleTilesClient.KEY_BUILDING_MODE.getTranslatedKeyMessage() };
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public LittleTool tool(ItemStack stack) {
        return new LittleToolShaper(stack);
    }
}
