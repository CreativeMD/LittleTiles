package team.creative.littletiles.common.structure.animation.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;

public class PlaySoundEvent extends AnimationEvent<CompoundTag> {
    
    public SoundEvent sound;
    public float volume;
    public float pitch;
    
    public PlaySoundEvent(Tag tag) {
        if (tag instanceof CompoundTag nbt) {
            ResourceLocation location = new ResourceLocation(nbt.getString("s"));
            sound = ForgeRegistries.SOUND_EVENTS.getValue(location);
            if (sound == null)
                sound = SoundEvent.createVariableRangeEvent(location);
            volume = nbt.getFloat("v");
            pitch = nbt.getFloat("p");
        } else
            throw new UnsupportedOperationException();
    }
    
    public PlaySoundEvent(SoundEvent event, float volume, float pitch) {
        this.sound = event;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    @Override
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("s", sound.getLocation().toString());
        nbt.putFloat("v", volume);
        nbt.putFloat("p", pitch);
        return nbt;
    }
    
    @Override
    public void start(AnimationContext context) {
        context.play(sound, volume, pitch);
    }
    
    @Override
    public boolean isDone(int ticksActive, AnimationContext context) {
        return true;
    }
    
    @Override
    public PlaySoundEvent copy() {
        return new PlaySoundEvent(sound, volume, pitch);
    }
    
    @Override
    public int reverseTick(int start, int duration, AnimationContext context) {
        return start;
    }
    
}
