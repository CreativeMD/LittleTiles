package team.creative.littletiles.common.structure.animation;

import net.minecraft.network.chat.MutableComponent;
import team.creative.creativecore.common.gui.GuiControl;

public enum PhysicalPart {
    
    ROTX(false),
    ROTY(false),
    ROTZ(false),
    OFFX(true),
    OFFY(true),
    OFFZ(true);
    
    public final boolean offset;
    
    private PhysicalPart(boolean offset) {
        this.offset = offset;
    }
    
    public MutableComponent title() {
        return GuiControl.translatable("gui.door." + name().toLowerCase());
    }
}
