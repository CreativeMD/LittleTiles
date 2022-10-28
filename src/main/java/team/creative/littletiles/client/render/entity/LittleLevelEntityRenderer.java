package team.creative.littletiles.client.render.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Vector3f;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.ForgeConfig;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.entity.LittleLevelEntity;

public class LittleLevelEntityRenderer extends EntityRenderer<LittleLevelEntity> {
    
    public static Minecraft mc = Minecraft.getInstance();
    public static LittleLevelEntityRenderer INSTANCE;
    
    public static RenderType getLayerByStage(RenderLevelStageEvent.Stage stage) {
        if (stage == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS)
            return RenderType.solid();
        if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)
            return RenderType.cutoutMipped();
        if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)
            return RenderType.cutout();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return RenderType.translucent();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)
            return RenderType.tripwire();
        throw new IllegalArgumentException("Unexpected value: " + stage);
    }
    
    public LittleLevelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        INSTANCE = this;
    }
    
    @Override
    public boolean shouldRender(LittleLevelEntity animation, Frustum frustum, double camX, double camY, double camZ) {
        if (animation.getRenderManager().isInSight == null)
            animation.getRenderManager().isInSight = animation.shouldRender(camX, camY, camZ) && frustum.isVisible(animation.getRealBB().inflate(0.5D));
        return animation.getRenderManager().isInSight;
    }
    
    @Override
    public void render(LittleLevelEntity animation, float p_114486_, float p_114487_, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        super.render(animation, p_114486_, p_114487_, pose, buffer, packedLight);
        
        // TODO Render entities (not sub animations)
    }
    
    @Override
    public ResourceLocation getTextureLocation(LittleLevelEntity animation) {
        return InventoryMenu.BLOCK_ATLAS;
    }
    
    public void setupRender(LittleLevelEntity animation, Camera camera, Frustum frustum, boolean capturedFrustum, boolean spectator) {
        Vec3d cam = new Vec3d(camera.getPosition());
        animation.getOrigin().transformPointToFakeWorld(cam);
        LittleLevelRenderManager manager = animation.getRenderManager();
        Level level = (Level) animation.getSubLevel();
        
        manager.setCameraPosition(cam);
        BlockPos cameraPos = manager.getCameraBlockPos();
        Vec3d chunkCamera = new Vec3d(Math.floor(cam.x / 8.0D), Math.floor(cam.y / 8.0D), Math.floor(cam.z / 8.0D));
        
        manager.needsFullRenderChunkUpdate |= manager.getChunkCameraPosition().equals(chunkCamera);
        
        manager.setChunkCameraPosition(chunkCamera);
        boolean headOccupied = mc.smartCull;
        if (spectator && level.getBlockState(cameraPos).isSolidRender(level, cameraPos))
            headOccupied = false;
        
        if (capturedFrustum || !manager.isInSight)
            return;
        
        if (manager.needsFullRenderChunkUpdate && (manager.lastFullRenderChunkUpdate == null || manager.lastFullRenderChunkUpdate.isDone())) {
            manager.needsFullRenderChunkUpdate = false;
            boolean flag1 = headOccupied;
            manager.lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> {
                Queue<LevelRenderer.RenderChunkInfo> queue1 = Queues.newArrayDeque();
                this.initializeQueueForFullUpdate(p_194339_, queue1);
                LevelRenderer.RenderChunkStorage levelrenderer$renderchunkstorage1 = new LevelRenderer.RenderChunkStorage(this.viewArea.chunks.length);
                this.updateRenderChunks(levelrenderer$renderchunkstorage1.renderChunks, levelrenderer$renderchunkstorage1.renderInfoMap, vec3, queue1, flag1);
                this.renderChunkStorage.set(levelrenderer$renderchunkstorage1);
            });
        }
        
        LevelRenderer.RenderChunkStorage levelrenderer$renderchunkstorage = this.renderChunkStorage.get();
        if (!this.recentlyCompiledChunks.isEmpty()) {
            Queue<LevelRenderer.RenderChunkInfo> queue = Queues.newArrayDeque();
            
            while (!this.recentlyCompiledChunks.isEmpty()) {
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.recentlyCompiledChunks.poll();
                LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = levelrenderer$renderchunkstorage.renderInfoMap.get(chunkrenderdispatcher$renderchunk);
                if (levelrenderer$renderchunkinfo != null && levelrenderer$renderchunkinfo.chunk == chunkrenderdispatcher$renderchunk) {
                    queue.add(levelrenderer$renderchunkinfo);
                }
            }
            
            this.updateRenderChunks(levelrenderer$renderchunkstorage.renderChunks, levelrenderer$renderchunkstorage.renderInfoMap, vec3, queue, headOccupied);
        }
    }
    
    public void compileChunks(LittleLevelEntity animation, Camera camera) {
        mc.getProfiler().push("compile_animation_chunks");
        LittleLevelRenderManager manager = animation.getRenderManager();
        List<LittleRenderChunk> schedule = Lists.newArrayList();
        
        Level level = (Level) animation.getSubLevel();
        
        for (LittleRenderChunk chunk : manager) {
            ChunkPos chunkpos = new ChunkPos(chunk.pos);
            if (chunk.isDirty() && level.getChunk(chunkpos.x, chunkpos.z).isClientLightReady()) {
                boolean immediate = false;
                if (mc.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED)
                    immediate = chunk.isDirtyFromPlayer();
                else if (mc.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                    immediate = !ForgeConfig.CLIENT.alwaysSetupTerrainOffThread
                            .get() && (chunk.pos.offset(8, 8, 8).distSqr(manager.getCameraBlockPos()) < 768.0D || chunk.isDirtyFromPlayer()); // the target is the else block below, so invert the forge addition to get there early
                }
                
                if (immediate) {
                    chunk.compile(renderregioncache);
                    chunk.setNotDirty();
                } else
                    schedule.add(chunk);
            }
        }
        
        for (LittleRenderChunk chunk : schedule) {
            chunk.compileASync(renderregioncache);
            chunk.setNotDirty();
        }
        
        mc.getProfiler().pop();
    }
    
    public void renderBlockEntities(PoseStack pose, LittleLevelEntity animation, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource.BufferSource bufferSource) {
        for (LittleRenderChunk chunk : animation.getRenderManager()) {
            List<BlockEntity> list = chunk.getCompiledChunk().getRenderableBlockEntities();
            if (list.isEmpty())
                continue;
            
            for (BlockEntity blockentity : list) {
                if (!frustum.isVisible(blockentity.getRenderBoundingBox()))
                    continue;
                BlockPos blockpos4 = blockentity.getBlockPos();
                MultiBufferSource multibuffersource1 = bufferSource;
                pose.pushPose();
                pose.translate(blockpos4.getX() - cam.x, blockpos4.getY() - cam.y, blockpos4.getZ() - cam.z);
                mc.getBlockEntityRenderDispatcher().render(blockentity, frameTime, pose, multibuffersource1);
                pose.popPose();
            }
        }
    }
    
    public void resortTransparency(LittleLevelEntity animation, RenderType layer, double x, double y, double z) {
        int i = 0;
        for (LittleRenderChunk chunk : animation.getRenderManager().visibleChunks()) {
            if (i > 14)
                return;
            if (chunk.resortTransparency(layer))
                i++;
        }
    }
    
    public void renderChunkLayer(LittleLevelEntity animation, RenderLevelStageEvent event) {
        RenderType layer = getLayerByStage(event.getStage());
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        LittleLevelRenderManager manager = animation.getRenderManager();
        
        ShaderInstance shaderinstance = RenderSystem.getShader();
        Uniform uniform = shaderinstance.CHUNK_OFFSET;
        
        for (Iterator<LittleRenderChunk> iterator = layer == RenderType.translucent() ? manager.visibleChunksInverse() : manager.visibleChunks().iterator(); iterator.hasNext();) {
            LittleRenderChunk chunk = iterator.next();
            if (!chunk.getCompiledChunk().isEmpty(layer)) {
                VertexBuffer vertexbuffer = chunk.getBuffer(layer);
                if (uniform != null) {
                    uniform.set((float) (chunk.pos.getX() - cam.x), (float) (chunk.pos.getY() - cam.y), (float) (chunk.pos.getZ() - cam.z));
                    uniform.upload();
                }
                
                vertexbuffer.bind();
                vertexbuffer.draw();
            }
        }
        
        if (uniform != null)
            uniform.set(Vector3f.ZERO);
    }
    
}
