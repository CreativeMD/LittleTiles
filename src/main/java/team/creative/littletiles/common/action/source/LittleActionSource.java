package team.creative.littletiles.common.action.source;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.ingredient.LittleInventory;

public interface LittleActionSource {
    
    public Level getActionLevel();
    
    public ItemStack getActionItem();
    
    public boolean isPlayer();
    
    public default Player asPlayer() {
        return (Player) this;
    }
    
    public Provider getActionRegistry();
    
    public LittleInventory createInventory();
    
    public boolean needsIngredients();
    
    public void broadcastChanges();
    
    public void playSound(SoundEvent event, SoundSource source, float volume, float pitch);
    
    public boolean addStack(ItemStack stack);
    
    public void dropStack(ItemStack stack);
    
    public boolean isClient();
    
    public void sendText(Component translatable);
}
