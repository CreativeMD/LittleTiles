package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.client.render.cache.ChunkBlockLayerManager;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUtils;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public class LittleAnimationDestroyPacket extends CreativeCorePacket {
    
    public UUID uuid;
    public boolean placed;
    
    public LittleAnimationDestroyPacket(UUID uuid, boolean placed) {
        this.uuid = uuid;
        this.placed = placed;
    }
    
    public LittleAnimationDestroyPacket() {}
    
    @Override
    public void writeBytes(ByteBuf buf) {
        writeString(buf, uuid.toString());
        buf.writeBoolean(placed);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        uuid = UUID.fromString(readString(buf));
        placed = buf.readBoolean();
    }
    
    public void remove(EntityAnimation animation) {
        try {
            if (placed && !animation.isDead) {
                animation.getRenderChunkSuppilier().backToRAM(); //Just doesn't work you cannot get the render data after everything happened
                World world = animation.world;
                boolean subWorld = world instanceof IOrientatedWorld;
                HashMapList<Object, TileEntityLittleTiles> chunks = new HashMapList<>();
                
                for (TileEntity te : animation.fakeWorld.loadedTileEntityList) {
                    if (te instanceof TileEntityLittleTiles) {
                        if (subWorld)
                            chunks.add(RenderUtils.getRenderChunk((IOrientatedWorld) te.getWorld(), te.getPos()), (TileEntityLittleTiles) te);
                        else
                            chunks.add(RenderUtils.getRenderChunk(RenderUtils.getViewFrustum(), te.getPos()), (TileEntityLittleTiles) te);
                    }
                }
                
                if (subWorld)
                    for (Object chunk : chunks.keySet())
                        ((LittleRenderChunk) chunk).backToRAM();
                else
                    for (Object chunk : chunks.keySet()) {
                        for (int i = 0; i < BlockRenderLayer.values().length; i++) {
                            VertexBuffer buffer = ((RenderChunk) chunk).getVertexBufferByLayer(i);
                            if (buffer == null)
                                continue;
                            ChunkBlockLayerManager manager = ChunkBlockLayerManager.get(buffer);
                            if (manager != null)
                                manager.backToRAM();
                        }
                        
                    }
                
                for (Entry<Object, ArrayList<TileEntityLittleTiles>> entry : chunks.entrySet()) {
                    List<TileEntityLittleTiles> newBlocks = new ArrayList<>();
                    for (TileEntityLittleTiles oldTe : entry.getValue()) {
                        TileEntity newTE = world.getTileEntity(oldTe.getPos());
                        if (newTE instanceof TileEntityLittleTiles) {
                            ((TileEntityLittleTiles) newTE).render.getBufferCache().additional(oldTe.render.getBufferCache());
                            newBlocks.add((TileEntityLittleTiles) newTE);
                        } else {
                            world.setBlockState(oldTe.getPos(), BlockTile.getState(oldTe));
                            newTE = world.getTileEntity(oldTe.getPos());
                            ((TileEntityLittleTiles) newTE).render.getBufferCache().additional(oldTe.render.getBufferCache());
                            newBlocks.add((TileEntityLittleTiles) newTE);
                        }
                    }
                    entry.getValue().clear();
                    entry.getValue().addAll(newBlocks);
                }
                
                if (!subWorld)
                    for (Entry<Object, ArrayList<TileEntityLittleTiles>> entry : chunks.entrySet())
                        RenderUploader.uploadRenderData((RenderChunk) entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        animation.destroyAnimation();
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
        if (animation != null) {
            remove(animation);
            return;
        }
        
        for (Iterator<EntityAnimation> iterator = player.world.getEntities(EntityAnimation.class, new Predicate<EntityAnimation>() {
            
            @Override
            public boolean apply(EntityAnimation input) {
                return true;
            }
            
        }).iterator();iterator.hasNext();) {
            Entity entity = iterator.next();
            if (entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid)) {
                remove((EntityAnimation) entity);
                return;
            }
        }
    }
    
    @Override
    public void executeServer(EntityPlayer player) {}
    
}
