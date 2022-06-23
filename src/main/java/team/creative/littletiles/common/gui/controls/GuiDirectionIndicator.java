package team.creative.littletiles.common.gui.controls;

import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextBuilder;

public class GuiDirectionIndicator extends GuiIconButton {
    
    private Facing facing;
    
    public GuiDirectionIndicator(String name, Facing facing) {
        super(name, GuiIcon.EMPTY, x -> {});
        setFacing(facing);
    }
    
    public Facing getFacing() {
        return facing;
    }
    
    public void setFacing(Facing facing) {
        this.facing = facing;
        this.setIcon(toIcon(facing));
        this.setTooltip(new TextBuilder().translate("gui.points." + facing.name).build());
    }
    
    public static GuiIcon toIcon(Facing facing) {
        return switch (facing) {
            case EAST -> GuiIcon.ARROW_RIGHT;
            case WEST -> GuiIcon.ARROW_LEFT;
            case UP -> GuiIcon.ARROW_UP;
            case DOWN -> GuiIcon.ARROW_DOWN;
            case SOUTH -> GuiIcon.ARROW_IN;
            case NORTH -> GuiIcon.ARROW_OUT;
        };
    }
    
}
