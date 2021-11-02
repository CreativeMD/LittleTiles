package team.creative.littletiles.common.item;

import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.SubGuiRecipe;
import team.creative.littletiles.common.gui.SubGuiRecipeAdvancedSelection;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.gui.configure.SubGuiModeSelector;
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
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains("content") && stack.getOrCreateTagElement("content").contains("structure") && stack.getOrCreateTagElement("content")
                .getCompound("structure").contains("name"))
            return new TextComponent(stack.getOrCreateTagElement("content").getCompound("structure").getString("name"));
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
    public GuiConfigure getConfigure(Player player, ItemStack stack) {
        if (!((ItemLittleBlueprint) stack.getItem()).hasLittlePreview(stack))
            return new SubGuiRecipeAdvancedSelection(stack);
        return new SubGuiRecipe(stack);
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
        getSelectionMode(stack).onRightClick(player, stack, result.getBlockPos());
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClickBlock(Level level, Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        if (hasTiles(stack))
            return true;
        getSelectionMode(stack).onLeftClick(player, stack, result.getBlockPos());
        LittleTiles.NETWORK.sendToServer(new SelectionModePacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    public GuiConfigure getConfigureAdvanced(Player player, ItemStack stack) {
        return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
                ItemMultiTiles.currentContext = context;
                ItemMultiTiles.currentMode = mode;
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
        return LittleGroup.getSize(stack);
    }
    
    @Override
    public LittleVec getCachedOffset(ItemStack stack) {
        return LittleGroup.getOffset(stack);
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse
                .getTranslatedKeyMessage(), Minecraft.getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
    public static SelectionMode getSelectionMode(ItemStack stack) {
        return SelectionMode.getOrDefault(stack.getOrCreateTag().getString("selmode"));
    }
    
    public static void setSelectionMode(ItemStack stack, SelectionMode mode) {
        stack.getOrCreateTag().putString("selmode", mode.name);
    }
}
