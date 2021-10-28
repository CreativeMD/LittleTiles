package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import team.creative.littletiles.common.api.tool.ILittleEditor;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.SubGuiHammer;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiGridSelector;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittleHammer extends Item implements ILittleEditor, IItemTooltip, GuiHandler {
    
    private static boolean activeFilter = false;
    private static Filter<Block> currentFilter = null;
    public static ShapeSelection selection;
    
    public static boolean isFiltered() {
        return activeFilter;
    }
    
    public static void setFilter(boolean active, Filter<Block> filter) {
        activeFilter = active;
        currentFilter = filter;
    }
    
    public static Filter<Block> getFilter() {
        return currentFilter;
    }
    
    public ItemLittleHammer() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LittleShape shape = getShape(stack);
        tooltip.add(new TranslatableComponent("gui.shape").append(": ").append(new TranslatableComponent(shape.getKey())));
        shape.addExtraInformation(stack.getTag(), tooltip);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND)
            return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
        if (!level.isClientSide)
            GuiHandler.openItemGui(player, hand);
        return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
    
    @Override
    public LittleBoxes getBoxes(Level world, ItemStack stack, Player player, PlacementPosition pos, BlockHitResult result) {
        if (selection == null)
            selection = new ShapeSelection(stack, true);
        selection.setLast(player, stack, pos, result);
        return selection.getBoxes(true);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode(player)) {
            selection = null;
            PreviewRenderer.marked = null;
        } else if (selection != null)
            if (selection.addAndCheckIfPlace(player, position, result)) {
                if (isFiltered())
                    new LittleActionDestroyBoxesFiltered(selection.getBoxes(false), getFilter()).execute();
                else
                    new LittleActionDestroyBoxes(selection.getBoxes(false)).execute();
                selection = null;
            }
        return false;
    }
    
    @Override
    public boolean canDestroyBlockInCreative(Level level, BlockPos pos, ItemStack stack, Player player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0F;
    }
    
    @Override
    public void onDeselect(Level level, ItemStack stack, Player player) {
        if (selection != null)
            selection = null;
    }
    
    @Override
    public boolean hasCustomBoxes(Level level, ItemStack stack, Player player, BlockState state, PlacementPosition pos, BlockHitResult result) {
        return LittleAction.isBlockValid(state) || level.getBlockEntity(result.getBlockPos()) instanceof BETiles;
    }
    
    @Override
    public GuiLayer create(Player player, CompoundTag nbt) {
        return getConfigure(player, player.getMainHandItem());
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ItemStack stack) {
        return new SubGuiHammer(stack);
    }
    
    @Override
    public void rotate(Player player, ItemStack stack, Rotation rotation, boolean client) {
        if (client && selection != null)
            selection.rotate(player, stack, rotation);
        else
            new ShapeSelection(stack, false).rotate(player, stack, rotation);
    }
    
    @Override
    public void mirror(Player player, ItemStack stack, Axis axis, boolean client) {
        if (client && selection != null)
            selection.mirror(player, stack, axis);
        else
            new ShapeSelection(stack, false).mirror(player, stack, axis);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public IMarkMode onMark(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result, PlacementPreview previews) {
        if (selection != null)
            selection.toggleMark();
        return selection;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiConfigure getConfigureAdvanced(Player player, ItemStack stack) {
        return new SubGuiGridSelector(stack, ItemMultiTiles.currentContext, isFiltered(), getFilter()) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, boolean activeFilter, TileSelector selector) {
                setFilter(activeFilter, selector);
                if (selection != null)
                    selection.convertTo(context);
                ItemMultiTiles.currentContext = context;
            }
        };
    }
    
    public static LittleShape getShape(ItemStack stack) {
        return ShapeRegistry.getShape(stack.getOrCreateTag().getString("shape"));
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getLocalizedName(), LittleTilesClient.mark.getTranslatedKeyMessage(), LittleTilesClient.configure
                .getTranslatedKeyMessage(), LittleTilesClient.configureAdvanced.getTranslatedKeyMessage() };
    }
}
