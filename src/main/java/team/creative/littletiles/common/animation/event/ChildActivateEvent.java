package team.creative.littletiles.common.animation.event;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.DoorController;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.animation.AnimationGuiHandler.AnimationGuiHolder;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.structure.IAnimatedStructure;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.door.LittleDoor;

public class ChildActivateEvent extends AnimationEvent {
    
    public int childId;
    
    public ChildActivateEvent(int tick, int childId) {
        super(tick);
        this.childId = childId;
    }
    
    public ChildActivateEvent(int tick) {
        super(tick);
    }
    
    @Override
    protected void write(NBTTagCompound nbt) {
        nbt.setInteger("childId", childId);
    }
    
    @Override
    protected void read(NBTTagCompound nbt) {
        childId = nbt.getInteger("childId");
    }
    
    @Override
    protected boolean run(EntityAnimationController controller) {
        LittleStructure structure = controller.parent.structure;
        
        try {
            StructureChildConnection connector = structure.getChild(childId);
            if (!(connector.getStructure() instanceof LittleDoor))
                return true;
            LittleDoor door = (LittleDoor) connector.getStructure();
            
            if (!door.canOpenDoor(null))
                return true;
            
            EntityAnimation childAnimation = door.openDoor(null, ((DoorController) controller).supplier, true);
            if (childAnimation != null)
                childAnimation.controller.onServerApproves();
            
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    @Override
    public int getEventDuration(LittleStructure structure) {
        
        try {
            StructureChildConnection connector = structure.getChild(childId);
            LittleStructure childStructure = connector.getStructure();
            if (childStructure instanceof LittleDoor) {
                LittleDoor door = (LittleDoor) childStructure;
                return door.getCompleteDuration();
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return 0;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void prepareInGui(LittlePreviews previews, LittleStructure structure, EntityAnimation animation, AnimationGuiHandler handler) {
        if (structure.countChildren() <= childId)
            return;
        try {
            StructureChildConnection connector = structure.getChild(childId);
            LittleStructure childStructure = connector.getStructure();
            if (childStructure instanceof LittleDoor) {
                LittleDoor child = (LittleDoor) childStructure;
                EntityAnimation childAnimation;
                if (!connector.isLinkToAnotherWorld())
                    childAnimation = child.openDoor(null, new UUIDSupplier(), false);
                else if (child instanceof IAnimatedStructure)
                    childAnimation = ((IAnimatedStructure) child).getAnimation();
                else
                    childAnimation = null;
                
                GuiParent parent = new GuiParent("temp", 0, 0, 0, 0) {};
                AnimationGuiHolder holder = new AnimationGuiHolder(previews
                    .getChild(childId), new AnimationGuiHandler(getTick(), handler), childAnimation == null ? child : childAnimation.structure, childAnimation);
                LittleStructureGuiParser parser = LittleStructureRegistry
                    .getParser(parent, holder.handler, LittleStructureRegistry.getParserClass("structure." + child.type.id + ".name"));
                parser.create(holder.previews, StructureParentCollection.create(holder.previews.structureNBT, null));
                if (holder.handler.hasTimeline())
                    handler.subHolders.add(holder);
            }
        } catch (LittleActionException e) {}
    }
    
}
