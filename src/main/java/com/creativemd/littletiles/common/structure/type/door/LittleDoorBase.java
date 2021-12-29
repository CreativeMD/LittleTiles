package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiDoorEvents.GuiDoorEventsButton;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiDoorSettings.GuiDoorSettingsButton;
import com.creativemd.littletiles.client.render.world.LittleRenderChunkSuppilier;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.DoorController;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.packet.LittleAnimationControllerPacket;
import com.creativemd.littletiles.common.packet.LittleAnimationDataPacket;
import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.animation.AnimationTimeline;
import com.creativemd.littletiles.common.structure.animation.ValueTimeline;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.structure.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;
import com.creativemd.littletiles.common.structure.type.door.LittleAdvancedDoor.LittleAdvancedDoorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleAdvancedDoor.LittleAdvancedDoorType;
import com.creativemd.littletiles.common.structure.type.door.LittleAxisDoor.LittleAxisDoorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleAxisDoor.LittleAxisDoorType;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorActivator.LittleDoorActivatorParser;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorActivator.LittleDoorActivatorType;
import com.creativemd.littletiles.common.structure.type.door.LittleSlidingDoor.LittleSlidingDoorParser;
import com.creativemd.littletiles.common.tile.math.location.LocalStructureLocation;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementResult;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleDoorBase extends LittleDoor implements IAnimatedStructure {
    
    public LittleDoorBase(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    public int interpolation = 0;
    public int duration = 50;
    public boolean stayAnimated = false;
    public boolean noClip = false;
    public boolean playPlaceSounds = true;
    public List<AnimationEvent> events = new ArrayList<>();
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        super.loadFromNBTExtra(nbt);
        events = new ArrayList<>();
        NBTTagList list = nbt.getTagList("events", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            AnimationEvent event = AnimationEvent.loadFromNBT(list.getCompoundTagAt(i));
            if (event != null)
                events.add(event);
        }
        if (nbt.hasKey("duration"))
            duration = nbt.getInteger("duration");
        else
            duration = 10;
        if (nbt.hasKey("sounds"))
            playPlaceSounds = nbt.getBoolean("sounds");
        else
            playPlaceSounds = true;
        stayAnimated = nbt.getBoolean("stayAnimated");
        
        interpolation = nbt.getInteger("interpolation");
        noClip = nbt.getBoolean("noClip");
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        super.writeToNBTExtra(nbt);
        NBTTagList list = new NBTTagList();
        for (AnimationEvent event : events)
            list.appendTag(event.writeToNBT(new NBTTagCompound()));
        nbt.setTag("events", list);
        nbt.setInteger("duration", duration);
        if (stayAnimated)
            nbt.setBoolean("stayAnimated", stayAnimated);
        nbt.setInteger("interpolation", interpolation);
        if (noClip)
            nbt.setBoolean("noClip", noClip);
        else
            nbt.removeTag("noClip");
        nbt.setBoolean("sounds", playPlaceSounds);
    }
    
    public abstract void transformDoorPreview(LittleAbsolutePreviews previews);
    
    public LittleAbsolutePreviews getDoorPreviews() throws CorruptedConnectionException, NotYetConnectedException {
        LittleAbsolutePreviews previews = getAbsolutePreviewsSameWorldOnly(getPos());
        transformDoorPreview(previews);
        InternalSignalOutput output = getOutput(0);
        previews.structureNBT.setTag(output.component.identifier, output.write(true, new NBTTagCompound()));
        return previews;
    }
    
    @Override
    public void startAnimation(EntityAnimation animation) {
        for (int i = 0; i < events.size(); i++)
            events.get(i).reset();
    }
    
    @Override
    public void beforeTick(EntityAnimation animation, int tick) {
        super.beforeTick(animation, tick);
        DoorController controller = (DoorController) animation.controller;
        for (AnimationEvent event : events)
            if (event.shouldBeProcessed(tick))
                event.process(controller);
    }
    
    @Override
    public void finishAnimation(EntityAnimation animation) {
        int duration = getCompleteDuration();
        for (AnimationEvent event : events)
            event.invert(this, duration);
        events.sort(null);
    }
    
    @Override
    public int getCompleteDuration() {
        int duration = this.duration;
        for (AnimationEvent event : events)
            duration = Math.max(duration, event.getMinimumRequiredDuration(this));
        return duration;
    }
    
    @Override
    public List<LittleDoor> collectDoorsToCheck() {
        List<Integer> children = new ArrayList<>();
        for (AnimationEvent event : events)
            if (event instanceof ChildActivateEvent && !children.contains(((ChildActivateEvent) event).childId))
                children.add(((ChildActivateEvent) event).childId);
            
        List<LittleDoor> doors = new ArrayList<>();
        if (children.isEmpty())
            return doors;
        for (Integer integer : children) {
            if (this.countChildren() <= integer)
                continue;
            try {
                LittleStructure child = getChild(integer).getStructure();
                if (child instanceof LittleDoor)
                    doors.add((LittleDoor) child);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        return doors;
    }
    
    @Override
    public boolean canOpenDoor(@Nullable EntityPlayer player) {
        if (!super.canOpenDoor(player))
            return false;
        
        for (AnimationEvent event : events)
            event.reset();
        
        if (isAnimated()) // No transformations done if the door is already an animation
            return true;
        
        return true;
    }
    
    public EntityAnimation place(World world, SubWorld fakeWorld, @Nullable EntityPlayer player, Placement placement, UUIDSupplier supplier, StructureAbsolute absolute, boolean tickOnce) throws LittleActionException {
        ArrayList<TileEntityLittleTiles> blocks = new ArrayList<>();
        
        fakeWorld.preventNeighborUpdate = true;
        
        PlacementResult result = placement.tryPlace();
        
        if (result == null)
            throw new RuntimeException("Something went wrong during placing the door!");
        
        DoorController controller = createController(supplier, placement, getCompleteDuration());
        controller.noClip = noClip;
        
        controller.activator = player;
        
        fakeWorld.preventNeighborUpdate = false;
        
        LittleDoorBase newDoor = (LittleDoorBase) result.parentStructure;
        
        EntityAnimation animation = new EntityAnimation(world, fakeWorld, controller, placement.pos, supplier.next(), absolute, new LocalStructureLocation(newDoor));
        
        // Move animated worlds
        newDoor.transferChildrenToAnimation(animation);
        
        if (getParent() != null) {
            LittleStructure parentStructure = getParent().getStructure();
            boolean dynamic = getParent().dynamic;
            parentStructure.updateChildConnection(getParent().getChildId(), newDoor, dynamic);
            newDoor.updateParentConnection(getParent().getChildId(), parentStructure, dynamic);
        }
        
        newDoor.notifyAfterPlaced();
        
        animation.controller.startTransition(DoorController.openedState);
        
        world.spawnEntity(animation);
        
        if (tickOnce)
            animation.onUpdateForReal();
        return animation;
    }
    
    @Override
    public EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, boolean tickOnce) throws LittleActionException {
        if (isAnimated()) {
            ((DoorController) animation.controller).activate();
            if (tickOnce)
                animation.onUpdateForReal();
            PacketHandler.sendPacketToTrackingPlayers(new LittleAnimationControllerPacket(animation), animation, null);
            return animation;
        }
        
        if (mainBlock.isRemoved())
            return null;
        
        LittleAbsolutePreviews previews = getDoorPreviews();
        World world = getWorld();
        SubWorld fakeWorld = SubWorld.createFakeWorld(world);
        if (world.isRemote)
            fakeWorld.renderChunkSupplier = new LittleRenderChunkSuppilier();
        Placement placement = new Placement(player, PlacementHelper.getAbsolutePreviews(fakeWorld, previews, previews.pos, PlacementMode.all)).setIgnoreWorldBoundaries(false);
        StructureAbsolute absolute = getAbsoluteAxis();
        
        HashMapList<BlockPos, IStructureTileList> blocks = collectAllBlocksListSameWorld();
        
        EntityAnimation animation = place(getWorld(), fakeWorld, player, placement, uuid, absolute, tickOnce);
        
        boolean sendUpdate = !world.isRemote;
        EntityAnimation topAnimation = world instanceof WorldServer ? null : (EntityAnimation) fakeWorld.getTopEntity();
        
        for (Entry<BlockPos, ArrayList<IStructureTileList>> entry : blocks.entrySet()) {
            if (entry.getValue().isEmpty())
                continue;
            TileEntityLittleTiles te = entry.getValue().get(0).getTe();
            te.updateTiles((x) -> {
                for (IStructureTileList list : entry.getValue())
                    x.get(list).remove();
            });
            if (sendUpdate)
                if (topAnimation == null)
                    ((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(te.getPos());
                else
                    PacketHandler.sendPacketToTrackingPlayers(new LittleAnimationDataPacket(topAnimation), topAnimation, null);
                
        }
        
        return animation;
        
    }
    
    public abstract DoorController createController(UUIDSupplier supplier, Placement placement, int completeDuration);
    
    public abstract StructureAbsolute getAbsoluteAxis();
    
    public EntityAnimation animation;
    
    @Override
    public void setAnimation(EntityAnimation animation) {
        this.animation = animation;
    }
    
    @Override
    public boolean isInMotion() {
        if (animation != null && animation.controller.isChanging())
            return true;
        return false;
    }
    
    @Override
    public boolean isAnimated() {
        return animation != null;
    }
    
    @Override
    public EntityAnimation getAnimation() {
        return animation;
    }
    
    @Override
    public void destroyAnimation() {
        animation.destroyAndNotify();
    }
    
    public static void initDoors() {
        LittleStructureRegistry.registerStructureType(new LittleAxisDoorType("door", "door", LittleAxisDoor.class, LittleStructureAttribute.NONE)
            .addOutput("state", 1, SignalMode.TOGGLE), LittleAxisDoorParser.class);
        LittleStructureRegistry.registerStructureType(new LittleDoorType("slidingDoor", "door", LittleSlidingDoor.class, LittleStructureAttribute.NONE)
            .addOutput("state", 1, SignalMode.TOGGLE), LittleSlidingDoorParser.class);
        LittleStructureRegistry.registerStructureType(new LittleAdvancedDoorType("advancedDoor", "door", LittleAdvancedDoor.class, LittleStructureAttribute.NONE)
            .addOutput("state", 1, SignalMode.TOGGLE), LittleAdvancedDoorParser.class);
        LittleStructureRegistry.registerStructureType(new LittleDoorActivatorType("doorActivator", "door", LittleDoorActivator.class, LittleStructureAttribute.NONE)
            .addOutput("state", 1, SignalMode.TOGGLE), LittleDoorActivatorParser.class);
    }
    
    public static abstract class LittleDoorBaseType extends LittleStructureType {
        
        public LittleDoorBaseType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
            super(id, category, structureClass, attribute);
        }
        
        public abstract void setBit(LittlePreviews previews, BitSet set);
        
        @Override
        public void finializePreview(LittlePreviews previews) {
            super.finializePreview(previews);
            if (previews.hasChildren()) {
                BitSet set = new BitSet(previews.childrenCount());
                
                setBit(previews, set);
                for (int i = 0; i < previews.childrenCount(); i++) {
                    LittlePreviews child = previews.getChild(i);
                    if (!child.hasStructure())
                        continue;
                    if (set.get(i))
                        child.structureNBT.setBoolean("activateParent", true);
                    else
                        child.structureNBT.removeTag("activateParent");
                }
            }
        }
    }
    
    public static abstract class LittleDoorBaseParser extends LittleStructureGuiParser {
        
        public LittleDoorBaseParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @SideOnly(Side.CLIENT)
        @CustomEventSubscribe
        public void onChanged(GuiControlChangedEvent event) {
            if (event.source.is("duration_s") || event.source.is("children_activate") || event.source.is("interpolation"))
                updateTimeline();
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            boolean stayAnimated = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).stayAnimated : false;
            boolean disableRightClick = structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true;
            boolean noClip = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).noClip : false;
            boolean playPlaceSounds = structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).playPlaceSounds : true;
            parent.controls.add(new GuiDoorSettingsButton("settings", 108, 93, stayAnimated, disableRightClick, noClip, playPlaceSounds));
            parent.controls.add(new GuiLabel(CoreControl.translate("gui.door.duration") + ":", 90, 122));
            parent.controls.add(new GuiSteppedSlider("duration_s", 140, 122, 50, 6, structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).duration : 10, 1, 500));
            parent.controls.add(new GuiDoorEventsButton("children_activate", 93, 107, previews, structure instanceof LittleDoorBase ? (LittleDoorBase) structure : null));
            parent.controls
                .add(new GuiStateButton("interpolation", structure instanceof LittleDoorBase ? ((LittleDoorBase) structure).interpolation : 0, 140, 107, 40, 7, ValueTimeline.interpolationTypes));
            
            updateTimeline();
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleDoorBase parseStructure(LittlePreviews previews) {
            GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
            GuiDoorSettingsButton settings = (GuiDoorSettingsButton) parent.get("settings");
            GuiDoorEventsButton button = (GuiDoorEventsButton) parent.get("children_activate");
            GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
            
            int duration = (int) slider.value;
            LittleDoorBase door = parseStructure();
            door.duration = duration;
            door.stayAnimated = settings.stayAnimated;
            door.disableRightClick = !settings.disableRightClick;
            door.noClip = settings.noClip;
            door.playPlaceSounds = settings.playPlaceSounds;
            door.events = button.events;
            door.interpolation = interpolationButton.getState();
            
            return door;
        }
        
        @SideOnly(Side.CLIENT)
        public abstract LittleDoorBase parseStructure();
        
        @SideOnly(Side.CLIENT)
        public abstract void populateTimeline(AnimationTimeline timeline, int interpolation);
        
        public void updateTimeline() {
            GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("duration_s");
            AnimationTimeline timeline = new AnimationTimeline((int) slider.value, new PairList<>());
            GuiDoorEventsButton children = (GuiDoorEventsButton) parent.get("children_activate");
            GuiStateButton interpolationButton = (GuiStateButton) parent.get("interpolation");
            
            populateTimeline(timeline, interpolationButton.getState());
            handler.setTimeline(timeline, children.events);
        }
        
    }
}
