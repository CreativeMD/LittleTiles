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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.GuiGlove;
import team.creative.littletiles.common.item.glove.GloveMode;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;

public class ItemLittleGlove extends Item implements ILittlePlacer, IItemTooltip {
    
    public static GloveMode getMode(ItemStack stack) {
        return GloveMode.REGISTRY.get(stack.getOrCreateTag().getString("mode"));
    }
    
    public static void setMode(ItemStack stack, GloveMode mode) {
        setMode(stack.getOrCreateTag(), mode);
    }
    
    public static void setMode(CompoundTag nbt, GloveMode mode) {
        nbt.putString("mode", GloveMode.REGISTRY.getId(mode));
    }
    
    public ItemLittleGlove() {
        super(new Item.Properties().stacksTo(1));
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        getMode(stack).addExtraInformation(stack.getTag(), tooltip);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return getMode(stack).hasTiles(stack);
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return getMode(stack).getTiles(stack);
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return getTiles(stack);
    }
    
    @Override
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return PlacementPreview.relative(level, stack, position, allowLowResolution);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup previews) {
        getMode(stack).setTiles(previews, stack);
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClickAir(Player player, ItemStack stack) {
        getMode(stack).leftClickAir(player.level(), player, stack);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        getMode(stack).leftClickBlock(level, player, stack, result);
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return getMode(stack).rightClickBlock(level, player, stack, result);
    }
    
    @Override
    public boolean onMouseWheelClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        return getMode(stack).wheelClickBlock(level, player, stack, result);
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        return new GuiGlove(getMode(view.get()), view, 140, 140, ((ILittlePlacer) view.get().getItem()).getPositionGrid(view.get()));
    }
    
    @Override
    public String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        return getMode(stack).tooltipTranslateKey(stack, defaultKey);
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return getMode(stack).tooltipData(stack);
    }
    
}
