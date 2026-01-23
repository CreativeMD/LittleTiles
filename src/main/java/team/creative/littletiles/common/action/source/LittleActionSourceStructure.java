package team.creative.littletiles.common.action.source;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.structure.LittleStructure;

public class LittleActionSourceStructure implements LittleActionSource {
    
    public final LittleStructure structure;
    
    public LittleActionSourceStructure(LittleStructure structure) {
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
    public LittleInventory createInventory() {
        return null;
    }
    
    @Override
    public boolean needsIngredients() {
        return false;
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
    public boolean addStack(ItemStack stack) {
        return false;
    }
    
    @Override
    public void dropStack(ItemStack stack) {
        LevelUtils.dropItem(getActionLevel(), stack, structure.mainBlock.getPos());
    }
    
}
