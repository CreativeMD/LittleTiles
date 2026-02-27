package team.creative.littletiles.common.action.source;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.structure.LittleStructure;

public abstract class LittleActionSourceStructure<T extends LittleStructure> implements LittleActionSource {
    
    public final T structure;
    
    public LittleActionSourceStructure(T structure) {
        this.structure = structure;
    }
    
    @Override
    public Level getActionLevel() {
        return structure.getStructureLevel();
    }
    
    @Override
    public ItemStack getActionItem() {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isPlayer() {
        return false;
    }
    
    @Override
    public Provider getActionRegistry() {
        return structure.getStructureLevel().registryAccess();
    }
    
    @Override
    public void broadcastChanges() {
        structure.updateStructure();
    }
    
    @Override
    public void playSound(SoundEvent event, SoundSource source, float volume, float pitch) {
        structure.playSound(event, source, volume, pitch);
    }
    
    @Override
    public void dropStack(ItemStack stack) {
        LevelUtils.dropItem(getActionLevel(), stack, structure.mainBlock.getPos());
    }
    
    @Override
    public boolean isClient() {
        return structure.isClient();
    }
    
    @Override
    public void sendText(Component translatable) {}
    
    @Override
    public void requestInventoryUpdate() {}
    
}
