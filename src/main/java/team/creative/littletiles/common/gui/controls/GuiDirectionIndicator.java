package team.creative.littletiles.common.gui.controls;

import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextBuilder;

public class GuiDirectionIndicator extends GuiButtonIcon {
    
    private Facing facing;
    
    public GuiDirectionIndicator(String name, Facing facing) {
        super(name, Icon.EMPTY, x -> {});
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
    
    public static Icon toIcon(Facing facing) {
        return switch (facing) {
            case EAST -> Icon.ARROW_RIGHT;
            case WEST -> Icon.ARROW_LEFT;
            case UP -> Icon.ARROW_UP;
            case DOWN -> Icon.ARROW_DOWN;
            case SOUTH -> Icon.ARROW_IN;
            case NORTH -> Icon.ARROW_OUT;
        };
    }
    
}
