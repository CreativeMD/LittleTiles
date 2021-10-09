package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.client.render.cache.ChunkBlockLayerManager;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUtils;
import com.creativemd.littletiles.common.packet.LittlePlacedAnimationPacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationController;
import com.creativemd.littletiles.common.structure.animation.AnimationState;
import com.creativemd.littletiles.common.structure.animation.AnimationTimeline;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorBase;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import team.creative.littletiles.common.animation.EntityAnimationController;
import team.creative.littletiles.common.placement.PlacementResult;

public class DoorController extends EntityAnimationController {
    
    protected boolean placedOnServer = false;
    protected boolean isWaitingForApprove = false;
    protected int ticksToWait = -1;
    protected static final int waitTimeApprove = 300;
    protected Boolean placed = null;
    
    public static final String openedState = "opened";
    public static final String closedState = "closed";
    public Boolean turnBack;
    public int duration;
    public int completeDuration;
    public int interpolation;
    public EntityPlayer activator;
    public UUIDSupplier supplier;
    
    public boolean noClip;
    
    protected boolean modifiedTransition;
    
    public DoorController() {
        
    }
    
    public DoorController(UUIDSupplier supplier, AnimationState closed, AnimationState opened, Boolean turnBack, int duration, int completeDuration, int interpolation) {
        this.supplier = supplier;
        
        this.turnBack = turnBack;
        this.duration = duration;
        this.completeDuration = completeDuration;
        
        this.interpolation = interpolation;
        
        addState(openedState, opened);
        addStateAndSelect(closedState, closed);
        
        generateAllTransistions(duration);
        modifiedTransition = false;
        
        stretchTransitions();
    }
    
    public DoorController(UUIDSupplier supplier, AnimationState closed, AnimationState opened, Boolean turnBack, int duration, int completeDuration, AnimationTimeline open, AnimationTimeline close, int interpolation) {
        this.supplier = supplier;
        
        this.turnBack = turnBack;
        this.duration = duration;
        this.completeDuration = completeDuration;
        
        this.interpolation = interpolation;
        
        addState(openedState, opened);
        addStateAndSelect(closedState, closed);
        
        addTransition("closed", "opened", open);
        addTransition("opened", "closed", close);
        
        stretchTransitions();
    }
    
    @Override
    public boolean noClip() {
        return noClip;
    }
    
    @Override
    public AnimationController addTransition(String from, String to, AnimationTimeline animation) {
        modifiedTransition = true;
        return super.addTransition(from, to, animation);
    }
    
    public DoorController(UUIDSupplier supplier, AnimationState opened, Boolean turnBack, int duration, int completeDuration, int interpolation) {
        this(supplier, new AnimationState(), opened, turnBack, duration, completeDuration, interpolation);
    }
    
    protected void stretchTransitions() {
        completeDuration = Math.max(completeDuration, duration);
        for (AnimationTimeline timeline : stateTransition.values()) {
            timeline.duration = completeDuration;
        }
    }
    
    @Override
    public int getInterpolationType() {
        return interpolation;
    }
    
    @Override
    public EntityPlayer activator() {
        return activator;
    }
    
    public void markWaitingForApprove() {
        isWaitingForApprove = true;
    }
    
    public boolean activate() {
        if (placed != null)
            return false;
        
        boolean isOpen = currentState.name.equals(openedState);
        if (!isChanging()) {
            startTransition(isOpen ? closedState : openedState);
            return true;
        }
        return false;
    }
    
    @Override
    public AnimationState tick() {
        if (parent.world.isRemote && placed != null) {
            if (isWaitingForApprove) {
                if (ticksToWait < 0)
                    ticksToWait = waitTimeApprove;
                else if (ticksToWait == 0)
                    parent.isDead = true;
                else
                    ticksToWait--;
            } else
                place();
        }
        
        if (isChanging()) {
            try {
                parent.structure.load();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                return currentState.state;
            }
            
            ((LittleDoor) parent.structure).beforeTick(parent, tick);
            AnimationState state = super.tick();
            ((LittleDoor) parent.structure).afterTick(parent, tick);
            return state;
        } else
            return super.tick();
    }
    
    @Override
    public void startTransition(String key) {
        super.startTransition(key);
        ((LittleDoor) parent.structure).startAnimation(parent);
    }
    
    @Override
    public void endTransition() {
        super.endTransition();
        ((LittleDoor) parent.structure).finishAnimation(parent);
        if (turnBack != null && turnBack == currentState.name.equals(openedState)) {
            if (isWaitingForApprove)
                placed = false;
            else
                place();
        } else
            ((LittleDoor) parent.structure).completeAnimation();
    }
    
    public void place() {
        try {
            parent.structure.load();
            
            World world = parent.world;
            LittleAbsolutePreviews previews = parent.structure.getAbsolutePreviewsSameWorldOnly(parent.absolutePreviewPos);
            
            parent.structure.callStructureDestroyedToSameWorld();
            
            if (world.isRemote)
                parent.getRenderChunkSuppilier().backToRAM(); //Just doesn't work you cannot get the render data after everything happened
                
            Placement placement = new Placement(null, PlacementHelper.getAbsolutePreviews(world, previews, previews.pos, PlacementMode.all))
                .setPlaySounds(((LittleDoorBase) parent.structure).playPlaceSounds);
            
            LittleDoor newDoor;
            PlacementResult result;
            if ((result = placement.tryPlace()) != null) {
                
                newDoor = (LittleDoor) result.parentStructure;
                if (!(world instanceof CreativeWorld) && world.isRemote && !placedOnServer)
                    newDoor.waitingForApproval = true;
                
                newDoor.transferChildrenFromAnimation(parent);
                
                if (parent.structure.getParent() != null) {
                    boolean dynamic = parent.structure.getParent().dynamic;
                    LittleStructure parentStructure = parent.structure.getParent().getStructure();
                    newDoor.updateParentConnection(parent.structure.getParent().getChildId(), parentStructure, dynamic);
                    parentStructure.updateChildConnection(parent.structure.getParent().getChildId(), newDoor, dynamic);
                }
                
                if (!world.isRemote) {
                    WorldServer serverWorld = (WorldServer) (world instanceof IOrientatedWorld ? ((IOrientatedWorld) world).getRealWorld() : world);
                    PacketHandler.sendPacketToTrackingPlayers(new LittlePlacedAnimationPacket(newDoor.getStructureLocation(), parent.getUniqueID()), parent
                        .getAbsoluteParent(), serverWorld, null);
                }
                newDoor.completeAnimation();
            } else {
                parent.markRemoved();
                if (!world.isRemote)
                    WorldUtils.dropItem(world, parent.structure.getStructureDrop(), parent.center.baseOffset);
                return;
            }
            
            parent.markRemoved();
            
            if (world.isRemote) {
                boolean subWorld = world instanceof IOrientatedWorld;
                HashMapList<Object, TileEntityLittleTiles> chunks = new HashMapList<>();
                if (subWorld)
                    for (TileEntityLittleTiles te : result.tileEntities)
                        chunks.add(RenderUtils.getRenderChunk((IOrientatedWorld) te.getWorld(), te.getPos()), te);
                else
                    for (TileEntityLittleTiles te : result.tileEntities)
                        chunks.add(RenderUtils.getRenderChunk(RenderUtils.getViewFrustum(), te.getPos()), te);
                    
                if (subWorld)
                    for (Object chunk : chunks.keySet())
                        ((LittleRenderChunk) chunk).backToRAM();
                else
                    for (Object chunk : chunks.keySet()) {
                        for (int i = 0; i < BlockRenderLayer.values().length; i++) {
                            VertexBuffer buffer = ((RenderChunk) chunk).getVertexBufferByLayer(i);
                            if (buffer == null)
                                continue;
                            ChunkBlockLayerManager manager = (ChunkBlockLayerManager) ChunkBlockLayerManager.blockLayerManager.get(buffer);
                            if (manager != null)
                                manager.backToRAM();
                        }
                        
                    }
                
                for (Entry<Object, ArrayList<TileEntityLittleTiles>> entry : chunks.entrySet()) {
                    for (TileEntityLittleTiles te : entry.getValue()) {
                        TileEntity oldTE = parent.fakeWorld.getTileEntity(te.getPos());
                        if (oldTE instanceof TileEntityLittleTiles)
                            te.render.getBufferCache().combine(((TileEntityLittleTiles) oldTE).render.getBufferCache());
                    }
                }
                
                if (!subWorld)
                    for (Entry<Object, ArrayList<TileEntityLittleTiles>> entry : chunks.entrySet())
                        RenderUploader.uploadRenderData((RenderChunk) entry.getKey(), entry.getValue());
                placed = true;
            }
        } catch (CorruptedConnectionException | NotYetConnectedException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setTag("closed", getState(closedState).state.writeToNBT(new NBTTagCompound()));
        nbt.setTag("opened", getState(openedState).state.writeToNBT(new NBTTagCompound()));
        
        nbt.setString("originaluuid", supplier.original().toString());
        nbt.setString("uuid", supplier.uuid.toString());
        
        nbt.setBoolean("isOpen", currentState.name.equals(openedState));
        if (isChanging())
            nbt.setInteger("tick", this.tick);
        
        nbt.setInteger("duration", duration);
        nbt.setInteger("completeDuration", completeDuration);
        nbt.setByte("turnBack", (byte) (turnBack == null ? 0 : (turnBack ? 1 : -1)));
        
        nbt.setInteger("interpolation", interpolation);
        
        if (modifiedTransition) {
            NBTTagList list = new NBTTagList();
            for (Entry<String, AnimationTimeline> entry : stateTransition.entrySet()) {
                NBTTagCompound transitionNBT = entry.getValue().writeToNBT(new NBTTagCompound());
                transitionNBT.setString("key", entry.getKey());
                list.appendTag(transitionNBT);
            }
            nbt.setTag("transitions", list);
        }
        
        nbt.setBoolean("noClip", noClip);
        
    }
    
    @Override
    protected void readFromNBT(NBTTagCompound nbt) {
        addState(closedState, new AnimationState(nbt.getCompoundTag("closed")));
        addState(openedState, new AnimationState(nbt.getCompoundTag("opened")));
        
        supplier = new UUIDSupplier(UUID.fromString(nbt.getString("originaluuid")), UUID.fromString(nbt.getString("uuid")));
        
        duration = nbt.getInteger("duration");
        completeDuration = nbt.getInteger("completeDuration");
        interpolation = nbt.getInteger("interpolation");
        
        if (nbt.hasKey("transitions")) {
            NBTTagList list = nbt.getTagList("transitions", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound transitionNBT = list.getCompoundTagAt(i);
                addTransition(transitionNBT.getString("key"), new AnimationTimeline(transitionNBT));
            }
            modifiedTransition = true;
        } else {
            generateAllTransistions(duration);
            stretchTransitions();
        }
        
        boolean isOpen = nbt.getBoolean("isOpen");
        if (isOpen)
            currentState = getState(openedState);
        else
            currentState = getState(closedState);
        
        if (nbt.hasKey("tick")) {
            startTransition(isOpen ? closedState : openedState);
            this.tick = nbt.getInteger("tick");
        }
        
        byte turnBackData = nbt.getByte("turnBack");
        turnBack = turnBackData == 0 ? null : (turnBackData > 0 ? true : false);
        noClip = nbt.getBoolean("noClip");
    }
    
    @Override
    public void onServerApproves() {
        isWaitingForApprove = false;
    }
    
    @Override
    public void onServerPlaces() {
        placedOnServer = true;
    }
    
    @Override
    public void transform(LittleTransformation transformation) {
        for (AnimationControllerState state : states.values())
            state.transform(transformation);
        for (AnimationTimeline timeline : stateTransition.values()) {
            if (transformation.rotX != 0) {
                Rotation rotation = transformation.getRotation(Axis.X);
                for (int i = 0; i < Math.abs(transformation.rotX); i++)
                    timeline.transform(rotation);
            }
            if (transformation.rotY != 0) {
                Rotation rotation = transformation.getRotation(Axis.Y);
                for (int i = 0; i < Math.abs(transformation.rotY); i++)
                    timeline.transform(rotation);
            }
            if (transformation.rotZ != 0) {
                Rotation rotation = transformation.getRotation(Axis.Z);
                for (int i = 0; i < Math.abs(transformation.rotZ); i++)
                    timeline.transform(rotation);
            }
        }
        
        if (tickingState != null)
            tickingState.clear();
        else if (isChanging())
            tickingState = new AnimationState();
        if (isChanging())
            animation.tick(tick, tickingState);
    }
    
}
