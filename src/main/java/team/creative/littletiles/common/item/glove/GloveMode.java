package team.creative.littletiles.common.item.glove;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.GuiGlove;

public abstract class GloveMode {
    
    public static final NamedHandlerRegistry<GloveMode> REGISTRY = new NamedHandlerRegistry<>(null);
    
    static {
        REGISTRY.registerDefault("pixel", new PixelMode());
        REGISTRY.register("blueprint", new BlueprintGloveMode());
        REGISTRY.register("replace", new ReplaceMode());
    }
    
    public GloveMode() {}
    
    public String translateKey() {
        return "glove.mode." + REGISTRY.getId(this);
    }
    
    public Component translatable() {
        return Component.translatable(translateKey());
    }
    
    public void addExtraInformation(CompoundTag nbt, List<Component> tooltip) {}
    
    @OnlyIn(Dist.CLIENT)
    public void leftClickAir(Level level, Player player, ItemStack stack) {}
    
    @OnlyIn(Dist.CLIENT)
    public void leftClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {}
    
    @OnlyIn(Dist.CLIENT)
    public boolean rightClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result) {
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public abstract boolean wheelClickBlock(Level level, Player player, ItemStack stack, BlockHitResult result);
    
    @OnlyIn(Dist.CLIENT)
    public abstract boolean hasPreviewElement(ItemStack stack);
    
    @OnlyIn(Dist.CLIENT)
    public abstract LittleElement getPreviewElement(ItemStack stack);
    
    public abstract void loadGui(GuiGlove gui);
    
    public abstract void saveGui(GuiGlove gui, CompoundTag nbt);
    
    public boolean hasTiles(ItemStack stack) {
        return true;
    }
    
    public abstract LittleGroup getTiles(ItemStack stack);
    
    public abstract void setTiles(LittleGroup previews, ItemStack stack);
    
    public abstract void vanillaBlockAction(Level level, ItemStack stack, BlockPos pos, BlockState state);
    
    public abstract void littleBlockAction(Level level, BETiles be, LittleTileContext context, ItemStack stack, BlockPos pos, CompoundTag nbt);
    
    public String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        return defaultKey;
    }
    
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { translatable(), LittleTilesClient.configure.getTranslatedKeyMessage() };
    }
    
}