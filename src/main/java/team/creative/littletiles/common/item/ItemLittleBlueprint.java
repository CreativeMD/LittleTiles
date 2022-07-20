package team.creative.littletiles.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.GuiModeSelector;
import team.creative.littletiles.common.gui.tool.GuiRecipe;
import team.creative.littletiles.common.gui.tool.GuiRecipeSelection;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.item.SelectionModePacket;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.selection.SelectionMode;

public class ItemLittleBlueprint extends Item implements ILittlePlacer, IItemTooltip {
    
    public ItemLittleBlueprint() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains("content") && stack.getOrCreateTagElement("content").contains("structure") && stack.getOrCreateTagElement("content")
                .getCompound("structure").contains("name"))
            return Component.literal(stack.getOrCreateTagElement("content").getCompound("structure").getString("name"));
        return super.getName(stack);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return stack.getOrCreateTag().contains("content");
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return LittleGroup.load(stack.getOrCreateTagElement("content"));
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return LittleGroup.loadLow(stack.getOrCreateTagElement("content"));
    }
    
    @Override
    public PlacementPreview getPlacement(Level level, ItemStack stack, PlacementPosition position, boolean allowLowResolution) {
        return PlacementPreview.relative(level, stack, position, allowLowResolution);
    }
    
    @Override
    public void saveTiles(ItemStack stack, LittleGroup group) {
        stack.getOrCreateTag().put("content", LittleGroup.save(group));
    }
    
    @Override
    public GuiConfigure getConfigure(Player player, ContainerSlotView view) {
        if (!((ItemLittleBlueprint) view.get().getItem()).hasTiles(view.get()))
            return new GuiRecipeSelection(view);
        return new GuiRecipe(view);
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
    @OnlyIn(Dist.CLIENT)
    public boolean onMouseWheelClickBlock(Level world, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        BlockState state = world.getBlockState(result.getBlockPos());
        if (state.getBlock() instanceof BlockTile) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
            LittleTiles.NETWORK.sendToServer(new BlockPacket(world, result.getBlockPos(), player, BlockPacketAction.RECIPE, nbt));
            return true;
        }
        return true;
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onRightClick(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        getSelectionMode(stack).rightClick(player, stack, result.getBlockPos());
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        getSelectionMode(stack).leftClick(player, stack, result.getBlockPos());
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ContainerSlotView view) {
        return new GuiModeSelector(view, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
            
            @Override
            public CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode) {
                ItemMultiTiles.currentContext = grid;
                ItemMultiTiles.currentMode = mode;
                return nbt;
            }
            
        };
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public LittleGrid getPositionGrid(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public LittleVec getCachedSize(ItemStack stack) {
        return LittleGroup.getSize(stack.getOrCreateTag());
    }
    
    @Override
    public LittleVec getCachedMin(ItemStack stack) {
        return LittleGroup.getMin(stack.getOrCreateTag());
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse
                .getTranslatedKeyMessage(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    public static SelectionMode getSelectionMode(ItemStack stack) {
        return SelectionMode.REGISTRY.get(stack.getOrCreateTag().getString("selmode"));
    }
    
    public static void setSelectionMode(ItemStack stack, SelectionMode mode) {
        stack.getOrCreateTag().putString("selmode", mode.getName());
    }
}
