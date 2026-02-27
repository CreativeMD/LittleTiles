package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.packet.action.RequestInventoryResetPacket;

@Mixin(Player.class)
public class PlayerMixin implements LittleActionSource {
    
    @Override
    public Level getActionLevel() {
        return asPlayer().level();
    }
    
    @Override
    public ItemStack getActionItem() {
        return asPlayer().getMainHandItem();
    }
    
    @Override
    public boolean isPlayer() {
        return true;
    }
    
    @Override
    public Provider getActionRegistry() {
        return asPlayer().registryAccess();
    }
    
    @Override
    public LittleInventory createInventory() {
        return new LittleInventory(asPlayer());
    }
    
    @Override
    public boolean needsIngredients() {
        return !asPlayer().isCreative();
    }
    
    @Override
    public void broadcastChanges() {
        asPlayer().inventoryMenu.broadcastChanges();
    }
    
    @Override
    public void playSound(SoundEvent event, SoundSource source, float volume, float pitch) {
        asPlayer().level().playSound(null, asPlayer(), event, source, volume, pitch);
    }
    
    @Override
    public boolean addStack(ItemStack stack) {
        return asPlayer().addItem(stack);
    }
    
    @Override
    public void dropStack(ItemStack stack) {
        LevelUtils.dropItem(asPlayer(), stack);
    }
    
    @Override
    public boolean isClient() {
        return asPlayer().level().isClientSide;
    }
    
    @Override
    public void sendText(Component translatable) {
        asPlayer().sendSystemMessage(translatable);
    }
    
    @Override
    public void requestInventoryUpdate() {
        LittleTiles.NETWORK.sendToServer(new RequestInventoryResetPacket());
    }
}
