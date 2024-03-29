package team.creative.littletiles.common.structure.animation;

import net.minecraft.nbt.CompoundTag;

public class AnimationState extends PhysicalState {
    
    public final String name;
    
    private boolean backToBlockform;
    
    public AnimationState(CompoundTag nbt) {
        super(nbt);
        this.name = nbt.getString("n");
        this.backToBlockform = nbt.getBoolean("b");
    }
    
    public AnimationState(String name) {
        super();
        this.name = name;
        this.backToBlockform = true;
    }
    
    public AnimationState(String name, PhysicalState state, boolean backToBlockform) {
        super();
        this.name = name;
        set(state);
        this.backToBlockform = backToBlockform;
    }
    
    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        nbt.putString("n", name);
        if (backToBlockform)
            nbt.putBoolean("b", backToBlockform);
        return nbt;
    }
    
    public boolean shouldGoBackToBlockform() {
        return backToBlockform;
    }
    
}
