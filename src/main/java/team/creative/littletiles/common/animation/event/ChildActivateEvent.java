package team.creative.littletiles.common.animation.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.AnimationGuiHandler.AnimationGuiHolder;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.structure.IAnimatedStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
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
    protected void saveExtra(CompoundTag nbt) {
        nbt.putInt("childId", childId);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        childId = nbt.getInt("childId");
    }
    
    @Override
    protected boolean run(EntityAnimationController controller) {
        LittleStructure structure = controller.parent.structure;
        
        try {
            StructureChildConnection connector = structure.children.getChild(childId);
            if (!(connector.getStructure() instanceof LittleDoor))
                return true;
            LittleDoor door = (LittleDoor) connector.getStructure();
            
            if (!door.canOpenDoor(null))
                return true;
            
            EntityAnimation childAnimation = door.openDoor(null, true);
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
            StructureChildConnection connector = structure.children.getChild(childId);
            LittleStructure childStructure = connector.getStructure();
            if (childStructure instanceof LittleDoor) {
                LittleDoor door = (LittleDoor) childStructure;
                return door.getCompleteDuration();
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return 0;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void prepareInGui(LittleGroup previews, LittleStructure structure, EntityAnimation animation, AnimationGuiHandler handler) {
        if (!structure.children.hasChild(childId))
            return;
        try {
            StructureChildConnection connector = structure.children.getChild(childId);
            LittleStructure childStructure = connector.getStructure();
            if (childStructure instanceof LittleDoor) {
                LittleDoor child = (LittleDoor) childStructure;
                EntityAnimation childAnimation;
                if (!connector.isLinkToAnotherWorld())
                    childAnimation = child.openDoor(null, false);
                else if (child instanceof IAnimatedStructure)
                    childAnimation = ((IAnimatedStructure) child).getAnimation();
                else
                    childAnimation = null;
                
                GuiParent parent = new GuiParent("temp");
                AnimationGuiHolder holder = new AnimationGuiHolder(previews.children
                        .getChild(childId), new AnimationGuiHandler(getTick(), handler), childAnimation == null ? child : childAnimation.structure, childAnimation);
                LittleStructureGuiParser parser = LittleStructureRegistry
                        .getParser(parent, holder.handler, LittleStructureRegistry.getParserClass("structure." + child.type.id + ".name"));
                parser.create(holder.previews, StructureParentCollection.create(holder.previews.getStructureTag(), null));
                if (holder.handler.hasTimeline())
                    handler.subHolders.add(holder);
            }
        } catch (LittleActionException e) {}
    }
    
}