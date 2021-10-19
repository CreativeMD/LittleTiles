package team.creative.littletiles.common.gui.controls;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;

import net.minecraft.util.EnumFacing;
import team.creative.creativecore.common.util.math.base.Facing;

public class GuiDirectionIndicator extends GuiIconButton {
    
    private Facing direction;
    
    public GuiDirectionIndicator(String name, Facing facing) {
        super(name,);
        setDirection(facing);
    }
    
    @Override
    public void onClicked(int x, int y, int button) {
        
    }
    
    public EnumFacing getDirection() {
        return direction;
    }
    
    public void setDirection(EnumFacing direction) {
        if (direction.getAxis() != Axis.Z)
            caption = "->";
        
        switch (direction) {
        case EAST:
            setIcon(0);
            break;
        case WEST:
            setIcon(2);
            break;
        case UP:
            setIcon(1);
            break;
        case DOWN:
            setIcon(3);
            break;
        case SOUTH:
            setIcon(6);
            break;
        case NORTH:
            setIcon(5);
            break;
        }
        this.direction = direction;
    }
    
    @Override
    public ArrayList<String> getTooltip() {
        ArrayList<String> tooltip = new ArrayList<>();
        tooltip.add("relative direction:");
        switch (direction) {
        case EAST:
            tooltip.add("points right");
            break;
        case WEST:
            tooltip.add("points left");
            break;
        case UP:
            tooltip.add("points upwards");
            break;
        case DOWN:
            tooltip.add("points downwards");
            break;
        case SOUTH:
            tooltip.add("points directly towards you");
            break;
        case NORTH:
            tooltip.add("points away from you");
            break;
        }
        return tooltip;
    }
    
}
