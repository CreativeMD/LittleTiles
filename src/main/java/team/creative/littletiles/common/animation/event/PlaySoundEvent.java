package team.creative.littletiles.common.animation.event;

import com.creativemd.creativecore.client.sound.EntitySound;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.DoorController;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.door.LittleDoor;

public class PlaySoundEvent extends AnimationEvent {
    
    public SoundEvent sound;
    public float volume;
    public float pitch;
    public boolean opening;
    
    public PlaySoundEvent(int tick) {
        super(tick);
    }
    
    @Override
    public int getEventDuration(LittleStructure structure) {
        return 0;
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        if (sound instanceof SoundEventMissing)
            nbt.putString("sound", ((SoundEventMissing) sound).location.toString());
        else
            nbt.putString("sound", sound.getRegistryName().toString());
        nbt.putFloat("volume", volume);
        nbt.putFloat("pitch", pitch);
        nbt.putBoolean("opening", opening);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(nbt.getString("sound")));
        if (sound == null)
            sound = new SoundEventMissing(new ResourceLocation(nbt.getString("sound")));
        volume = nbt.getFloat("volume");
        pitch = nbt.getFloat("pitch");
        opening = nbt.getBoolean("opening");
    }
    
    @Override
    protected boolean run(EntityAnimationController controller) {
        if (controller.parent.level.isClientSide && controller.getAimedState().name.equals(DoorController.openedState) == opening)
            playSound(controller);
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void playSound(EntityAnimationController controller) {
        if (!(sound instanceof SoundEventMissing))
            GuiControl.playSound(new EntitySound(sound, controller.parent, volume, pitch, SoundSource.NEUTRAL));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void runGui(AnimationGuiHandler handler) {
        if (opening && !(sound instanceof SoundEventMissing))
            GuiControl.playSound(sound, volume, pitch);
    }
    
    @Override
    public void invert(LittleDoor door, int duration) {
        //this.tick = duration - getMinimumRequiredDuration(door);
    }
    
    public static class SoundEventMissing extends SoundEvent {
        
        public final ResourceLocation location;
        
        public SoundEventMissing(ResourceLocation location) {
            super(location);
            this.location = location;
        }
    }
}
