package team.creative.littletiles.common.animation.event;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

public abstract class AnimationEventGuiParser<T extends AnimationEvent> {
    
    @OnlyIn(Dist.CLIENT)
    public abstract void createControls(GuiParent parent, @Nullable T event, LittleGroup previews);
    
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public abstract T parse(GuiParent parent, T event);
    
    @OnlyIn(Dist.CLIENT)
    public int getHeight() {
        return 30;
    }
    
}
