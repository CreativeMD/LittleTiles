package team.creative.littletiles.common.structure.animation;

import net.minecraft.network.chat.MutableComponent;
import team.creative.creativecore.common.gui.GuiControl;

public enum PhysicalPart {
    
    ROTX(false, "rotX"),
    ROTY(false, "rotY"),
    ROTZ(false, "rotZ"),
    OFFX(true, "offX"),
    OFFY(true, "offY"),
    OFFZ(true, "offZ");
    
    public final boolean offset;
    public final String oldKey;
    
    private PhysicalPart(boolean offset, String oldKey) {
        this.offset = offset;
        this.oldKey = oldKey;
    }
    
    public MutableComponent title() {
        return GuiControl.translatable("gui.door." + name().toLowerCase());
    }
}
