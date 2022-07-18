package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.ItemGuiCreator;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import team.creative.littletiles.common.api.tool.ILittleEditor;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.GuiGridSelector;
import team.creative.littletiles.common.gui.tool.GuiHammer;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class ItemLittleHammer extends Item implements ILittleEditor, IItemTooltip, ItemGuiCreator {
    
    private static boolean activeFilter = false;
    private static BiFilter<IParentCollection, LittleTile> currentFilter = null;
    public static ShapeSelection selection;
    
    public static boolean isFiltered() {
        return activeFilter;
    }
    
    public static void setFilter(boolean active, BiFilter<IParentCollection, LittleTile> filter) {
        activeFilter = active;
        currentFilter = filter;
    }
    
    public static BiFilter<IParentCollection, LittleTile> getFilter() {
        return currentFilter;
    }
    
    public ItemLittleHammer() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LittleShape shape = getShape(stack);
        tooltip.add(Component.translatable("gui.shape").append(": ").append(Component.translatable(shape.getKey())));
        shape.addExtraInformation(stack.getTag(), tooltip);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND)
            return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
        if (!level.isClientSide)
            GuiCreator.ITEM_OPENER.open(player, hand);
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
        if (LittleActionHandlerClient.isUsingSecondMode()) {
            selection = null;
            LittleTilesClient.PREVIEW_RENDERER.removeMarked();
        } else if (selection != null)
            if (selection.addAndCheckIfPlace(player, position, result)) {
                if (isFiltered())
                    LittleTilesClient.ACTION_HANDLER.execute(new LittleActionDestroyBoxesFiltered(level, selection.getBoxes(false), getFilter()));
                else
                    LittleTilesClient.ACTION_HANDLER.execute(new LittleActionDestroyBoxes(level, selection.getBoxes(false)));
                selection = null;
                LittleTilesClient.PREVIEW_RENDERER.removeMarked();
            }
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (selection != null)
            selection.click(player);
        return true;
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
    public void onDeselect(Level level, ItemStack stack, Player player) {
        selection = null;
    }
    
    @Override
    public boolean hasCustomBoxes(Level level, ItemStack stack, Player player, BlockState state, PlacementPosition pos, BlockHitResult result) {
        return LittleAction.isBlockValid(state) || level.getBlockEntity(result.getBlockPos()) instanceof BETiles;
    }
    
    @Override
    public GuiLayer create(CompoundTag nbt, Player player) {
        return getConfigure(player, ContainerSlotView.mainHand(player));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiHammer(view);
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
    public GuiConfigure getConfigureAdvanced(Player player, ContainerSlotView view) {
        return new GuiGridSelector(view, ItemMultiTiles.currentContext, isFiltered(), getFilter()) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, boolean activeFilter, BiFilter<IParentCollection, LittleTile> filter) {
                setFilter(activeFilter, selector);
                if (selection != null)
                    selection.convertTo(grid);
                ItemMultiTiles.currentContext = grid;
                return nbt;
            }
        };
    }
    
    public static LittleShape getShape(ItemStack stack) {
        return ShapeRegistry.REGISTRY.get(stack.getOrCreateTag().getString("shape"));
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { getShape(stack).getTranslatable(), LittleTilesClient.mark.getTranslatedKeyMessage(), LittleTilesClient.configure
                .getTranslatedKeyMessage(), LittleTilesClient.configureAdvanced.getTranslatedKeyMessage() };
    }
}
