package team.creative.littletiles.common.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.LittleToolPlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprint;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprintSelection;
import team.creative.littletiles.common.item.tooltip.IItemTooltip;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.item.SelectionModePacket;

public class ItemLittleBlueprint extends Item implements ILittlePlacer, IItemTooltip {
    
    public static final String CONTENT_KEY = "c";
    
    public static CompoundTag getContent(ItemStack stack) {
        var nbt = ILittleTool.getData(stack);
        if (nbt.contains(CONTENT_KEY) && !nbt.contains(LittleGroup.BOXES_COUNT_KEY))
            return nbt.getCompound(CONTENT_KEY);
        return nbt;
    }
    
    public ItemLittleBlueprint() {
        super(new Item.Properties());
    }
    
    @Override
    public Component getName(ItemStack stack) {
        var content = getContent(stack);
        if (content.contains(LittleGroup.STRUCTURE_KEY) && content.getCompound(LittleGroup.STRUCTURE_KEY).contains("n"))
            return Component.literal(content.getCompound(LittleGroup.STRUCTURE_KEY).getString("n"));
        return super.getName(stack);
    }
    
    @Override
    public boolean hasTiles(ItemStack stack) {
        return ILittleTool.hasData(stack);
    }
    
    @Override
    public LittleGroup getTiles(ItemStack stack) {
        return LittleGroup.load(getContent(stack));
    }
    
    @Override
    public LittleGroup getLow(ItemStack stack) {
        return LittleGroup.loadLow(getContent(stack));
    }
    
    public void saveTiles(ItemStack stack, LittleGroup group) {
        CompoundTag stackTag = ILittleTool.getData(stack);
        stackTag.put(ItemLittleBlueprint.CONTENT_KEY, LittleGroup.save(group));
        ILittleTool.setData(stack, stackTag);
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
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(LittleGroup.printTooltip(getContent(stack)));
    }
    
    @Override
    public LittleVecGrid getCachedSize(ItemStack stack) {
        return LittleGroup.getSize(getContent(stack));
    }
    
    @Override
    public LittleVecGrid getCachedMin(ItemStack stack) {
        return LittleGroup.getMin(getContent(stack));
    }
    
    @Override
    public String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        if (hasTiles(stack))
            return "littletiles.tiles.tooltip";
        return "littletiles.blueprint.selection.tooltip";
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        if (hasTiles(stack))
            return new Object[] { LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage(), LittleTilesClient.arrowKeysTooltip(), LittleTilesClient.KEY_MIRROR
                    .getTranslatedKeyMessage() };
        return new Object[] { Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage(), Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage(), Minecraft
                .getInstance().options.keyPickItem.getTranslatedKeyMessage(), LittleTilesClient.KEY_CONFIGURE.getTranslatedKeyMessage() };
    }
    
    @Override
    public Iterable<LittleTool> tools(ItemStack stack) {
        return Arrays.asList(new LittleToolBlueprint(stack));
    }
    
    @OnlyIn(Dist.CLIENT)
    public class LittleToolBlueprint extends LittleToolPlacer {
        
        public LittleToolBlueprint(ItemStack stack) {
            super(stack);
        }
        
        @Override
        public boolean onMouseWheelClickBlock(Level world, Player player, BlockHitResult result) {
            BlockState state = world.getBlockState(result.getBlockPos());
            if (state.getBlock() instanceof BlockTile) {
                CompoundTag nbt = new CompoundTag();
                nbt.putBoolean("secondMode", LittleActionHandlerClient.isUsingSecondMode());
                LittleTiles.NETWORK.sendToServer(new BlockPacket(world, result.getBlockPos(), player, BlockPacketAction.BLUEPRINT, nbt));
                return true;
            }
            return true;
        }
        
        @Override
        public boolean onRightClick(Level level, Player player, BlockHitResult result) {
            if (result == null)
                return false;
            if (hasTiles(stack))
                return super.onRightClick(level, player, result);
            
            var packet = new SelectionModePacket(result.getBlockPos(), true);
            packet.execute(player);
            LittleTiles.NETWORK.sendToServer(packet);
            return true;
        }
        
        @Override
        public boolean onLeftClick(Level level, Player player, BlockHitResult result) {
            if (result == null)
                return false;
            
            if (hasTiles(stack))
                return super.onLeftClick(level, player, result);
            
            var packet = new SelectionModePacket(result.getBlockPos(), false);
            packet.execute(player);
            LittleTiles.NETWORK.sendToServer(packet);
            return true;
        }
        
    }
    
}
