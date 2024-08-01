package team.creative.littletiles.common.entity.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.matrix.VecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.creativecore.common.util.type.itr.NestedFunctionIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.BlockStatePredictionHandlerExtender;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.client.level.little.LittleAnimationLevelClientCallback;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.level.little.LevelBlockChangeListener;
import team.creative.littletiles.common.level.little.LittleAnimationLevelCallback;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.packet.entity.animation.LittleAnimationBlocksPacket;
import team.creative.littletiles.server.level.little.LittleAnimationLevelServerCallback;

public class LittleAnimationLevel extends Level implements LittleSubLevel, Iterable<BETiles>, ClientLevelExtender {
    
    private Level parentLevel;
    public final LittleAnimationLevelCallback entityCallback;
    private final LittleAnimationLevelEntities entities;
    public Entity holder;
    public IVecOrigin origin;
    public LittleAnimationChunkCache chunks;
    private final List<LevelBlockChangeListener> blockChangeListeners = new ArrayList<>();
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager renderManager;
    private HashSet<BlockPos> trackedChanges;
    @OnlyIn(Dist.CLIENT)
    private BlockStatePredictionHandler blockStatePredictionHandler;
    
    public LittleAnimationLevel(Level level) {
        super((WritableLevelData) level.getLevelData(), level.dimension(), level.registryAccess(), level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE)
                .getHolderOrThrow(LittleTilesRegistry.FAKE_DIMENSION), level.getProfilerSupplier(), level.isClientSide, level.isDebug(), 0, 1000000);
        this.parentLevel = level;
        this.chunks = new LittleAnimationChunkCache(this);
        if (isClientSide) {
            this.blockStatePredictionHandler = new BlockStatePredictionHandler();
            this.entityCallback = new LittleAnimationLevelClientCallback(this);
        } else {
            this.trackedChanges = new HashSet<>();
            this.entityCallback = new LittleAnimationLevelServerCallback(this);
        }
        this.entities = new LittleAnimationLevelEntities(entityCallback);
    }
    
    @Override
    public LevelEntityGetter<Entity> getEntityGetter() {
        return entities;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager getRenderManager() {
        return renderManager;
    }
    
    @Override
    public MinecraftServer getServer() {
        return parentLevel.getServer();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleBlockChangedAckExtender(int sequence) {
        ((BlockStatePredictionHandlerExtender) this.blockStatePredictionHandler).setLevel(this);
        this.blockStatePredictionHandler.endPredictionsUpTo(sequence, null);
        ((BlockStatePredictionHandlerExtender) this.blockStatePredictionHandler).setLevel(null);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void setServerVerifiedBlockStateExtender(BlockPos pos, BlockState state, int p_233656_) {
        if (!this.blockStatePredictionHandler.updateKnownServerState(pos, state))
            super.setBlock(pos, state, p_233656_, 512);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void syncBlockStateExtender(BlockPos pos, BlockState state, Vec3 vec) {
        BlockState blockstate = this.getBlockState(pos);
        if (blockstate != state) {
            this.setBlock(pos, state, 19);
            Player player = Minecraft.getInstance().player;
            if (player.isColliding(pos, state))
                player.absMoveTo(vec.x, vec.y, vec.z);
        }
        
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockStatePredictionHandler blockStatePredictionHandler() {
        return blockStatePredictionHandler;
    }
    
    @Override
    public TransientEntitySectionManager getEntityStorage() {
        return null;
    }
    
    @Override
    public boolean allowPlacement() {
        return false;
    }
    
    @Override
    public Level getParent() {
        return parentLevel;
    }
    
    @Override
    public Level getRealLevel() {
        if (parentLevel instanceof LittleSubLevel sub)
            return sub.getRealLevel();
        return parentLevel;
    }
    
    @Override
    public void setParent(Level level) {
        parentLevel = level;
    }
    
    @Override
    public UUID key() {
        return getHolder().getUUID();
    }
    
    @Override
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    @Override
    public void setOrigin(Vec3d center) {
        if (parentLevel instanceof IOrientatedLevel)
            this.origin = new ChildVecOrigin(((IOrientatedLevel) parentLevel).getOrigin(), center);
        else
            this.origin = new VecOrigin(center);
    }
    
    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    
    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    
    @Override
    public LittleAnimationChunkCache getChunkSource() {
        return chunks;
    }
    
    @Override
    public void gameEvent(Holder<GameEvent> event, Vec3 vec, Context context) {
        getRealLevel().gameEvent(event, vec, context);
    }
    
    @Override
    public List<? extends Player> players() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public RegistryAccess registryAccess() {
        return getRealLevel().registryAccess();
    }
    
    @Override
    public Entity getHolder() {
        return holder;
    }
    
    @Override
    public void setHolder(Entity entity) {
        this.holder = entity;
    }
    
    @Override
    public void unload(LevelChunk chunk) {
        chunk.clearAllBlockEntities();
        this.getChunkSource().getLightEngine().setLightEnabled(chunk.getPos(), false);
    }
    
    @Override
    public void unload() {
        entities.removeAll();
        if (isClientSide && renderManager != null)
            renderManager.unload();
    }
    
    @Override
    public Iterable<Entity> entities() {
        return entities.getAll();
    }
    
    @Override
    public Iterable<LevelChunk> chunks() {
        return chunks.all();
    }
    
    public void initialTick() {
        tickBlockEntities();
    }
    
    @Override
    public void tick() {
        if (!isClientSide && !trackedChanges.isEmpty()) {
            LittleTiles.NETWORK.sendToClientTracking(new LittleAnimationBlocksPacket((LittleAnimationEntity) holder, trackedChanges), holder);
            trackedChanges.clear();
        }
        
        tickBlockEntities();
        entityCallback.tick();
    }
    
    @Override
    public void registerBlockChangeListener(LevelBlockChangeListener listener) {
        blockChangeListeners.add(listener);
    }
    
    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState actualState, BlockState setState, int p_104688_) {
        if (isClientSide)
            this.renderManager.blockChanged(this, pos, actualState, setState, p_104688_);
        else
            trackedChanges.add(pos);
    }
    
    @Override
    public void setBlocksDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        if (isClientSide)
            this.renderManager.setBlockDirty(pos, actualState, setState);
        blockChangeListeners.forEach(x -> x.blockChanged(pos, setState));
    }
    
    @Override
    public String gatherChunkSourceStats() {
        return "";
    }
    
    @Override
    public Entity getEntity(int id) {
        return entities.get(id);
    }
    
    @Override
    public MapItemSavedData getMapData(MapId key) {
        return getRealLevel().getMapData(key);
    }
    
    @Override
    public void setMapData(MapId key, MapItemSavedData data) {
        getRealLevel().setMapData(key, data);
    }
    
    @Override
    public MapId getFreeMapId() {
        return getRealLevel().getFreeMapId();
    }
    
    @Override
    public void destroyBlockProgress(int id, BlockPos pos, int progress) {
        if (isClientSide)
            renderManager.destroyBlockProgress(id, pos, progress);
    }
    
    @Override
    public Iterator<BETiles> iterator() {
        return new NestedFunctionIterator<BETiles>(chunks(), x -> () -> new FilterIterator<>(x.getBlockEntities().values(), BETiles.class));
    }
    
    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return entities;
    }
    
    @Override
    public void playSound(@Nullable Player p_184133_1_, BlockPos pos, SoundEvent p_184133_3_, SoundSource p_184133_4_, float p_184133_5_, float p_184133_6_) {
        if (getOrigin() == null)
            return;
        getRealLevel().playSound(p_184133_1_, transformToRealWorld(pos), p_184133_3_, p_184133_4_, p_184133_5_, p_184133_6_);
    }
    
    @Override
    public void playSound(@Nullable Player p_184148_1_, double x, double y, double z, SoundEvent p_184148_8_, SoundSource p_184148_9_, float p_184148_10_, float p_184148_11_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = new Vector3d(x, y, z);
        getOrigin().transformPointToWorld(vec);
        getRealLevel().playSound(p_184148_1_, vec.x, vec.y, vec.z, p_184148_8_, p_184148_9_, p_184148_10_, p_184148_11_);
    }
    
    @Override
    public void playSound(@Nullable Player p_217384_1_, Entity entity, SoundEvent p_217384_3_, SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {
        if (getOrigin() == null)
            return;
        Vec3 vec = getOrigin().transformPointToWorld(entity.getPosition(1.0F));
        getRealLevel().playSound(p_217384_1_, vec.x, vec.y, vec.z, p_217384_3_, p_217384_4_, p_217384_5_, p_217384_6_);
    }
    
    @Override
    public void playLocalSound(double x, double y, double z, SoundEvent p_184134_7_, SoundSource p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = getOrigin().transformPointToWorld(new Vector3d(x, y, z));
        getRealLevel().playLocalSound(vec.x, vec.y, vec.z, p_184134_7_, p_184134_8_, p_184134_9_, p_184134_10_, p_184134_11_);
    }
    
    @Override
    public void playSeededSound(Player p_262953_, double x, double y, double z, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_, float p_262914_,
            long p_262991_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = new Vector3d(x, y, z);
        getOrigin().transformPointToWorld(vec);
        getRealLevel().playSeededSound(p_262953_, vec.x, vec.y, vec.z, p_263359_, p_263020_, p_263055_, p_262914_, p_262991_);
    }
    
    @Override
    public void playSeededSound(Player player, double x, double y, double z, SoundEvent event, SoundSource source, float p_220369_, float p_220370_, long p_220371_) {
        if (getOrigin() == null)
            return;
        Vec3 vec = getOrigin().transformPointToWorld(new Vec3(x, y, z));
        getRealLevel().playSeededSound(player, vec.x, vec.y, vec.z, event, source, p_220369_, p_220370_, p_220371_);
    }
    
    @Override
    public void playSeededSound(Player player, Entity entity, Holder<SoundEvent> event, SoundSource source, float p_220376_, float p_220377_, long p_220378_) {
        if (getOrigin() == null)
            return;
        Vec3 vec = getOrigin().transformPointToWorld(entity.getEyePosition());
        getRealLevel().playSeededSound(player, vec.x, vec.y, vec.z, event, source, p_220376_, p_220377_, p_220378_);
    }
    
    @Override
    public void addParticle(ParticleOptions p_195594_1_, double x, double y, double z, double p_195594_8_, double p_195594_10_, double p_195594_12_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = getOrigin().transformPointToWorld(new Vector3d(x, y, z));
        getRealLevel().addParticle(p_195594_1_, vec.x, vec.y, vec.z, p_195594_8_, p_195594_10_, p_195594_12_);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addParticle(ParticleOptions p_195590_1_, boolean p_195590_2_, double x, double y, double z, double p_195590_9_, double p_195590_11_, double p_195590_13_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = getOrigin().transformPointToWorld(new Vector3d(x, y, z));
        getRealLevel().addParticle(p_195590_1_, p_195590_2_, vec.x, vec.y, vec.z, p_195590_9_, p_195590_11_, p_195590_13_);
    }
    
    @Override
    public void addAlwaysVisibleParticle(ParticleOptions p_195589_1_, double x, double y, double z, double p_195589_8_, double p_195589_10_, double p_195589_12_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = getOrigin().transformPointToWorld(new Vector3d(x, y, z));
        getRealLevel().addAlwaysVisibleParticle(p_195589_1_, vec.x, vec.y, vec.z, p_195589_8_, p_195589_10_, p_195589_12_);
    }
    
    @Override
    public void addAlwaysVisibleParticle(ParticleOptions p_217404_1_, boolean p_217404_2_, double x, double y, double z, double p_217404_9_, double p_217404_11_,
            double p_217404_13_) {
        if (getOrigin() == null)
            return;
        Vector3d vec = getOrigin().transformPointToWorld(new Vector3d(x, y, z));
        getRealLevel().addAlwaysVisibleParticle(p_217404_1_, p_217404_2_, vec.x, vec.y, vec.z, p_217404_9_, p_217404_11_, p_217404_13_);
    }
    
    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return getRealLevel().getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
    }
    
    @Override
    public float getShade(Direction direction, boolean p_230487_2_) {
        return getRealLevel().getShade(direction, p_230487_2_);
    }
    
    @Override
    public Scoreboard getScoreboard() {
        return getRealLevel().getScoreboard();
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return getRealLevel().getRecipeManager();
    }
    
    @Override
    public void levelEvent(Player player, int p_217378_2_, BlockPos pos, int p_217378_4_) {
        getRealLevel().levelEvent(player, p_217378_2_, pos, p_217378_4_);
    }
    
    @Override
    public void gameEvent(Entity p_151549_, Holder<GameEvent> p_151550_, BlockPos p_151551_) {
        getRealLevel().gameEvent(p_151549_, p_151550_, p_151551_);
    }
    
    @Override
    public String toString() {
        return "SubAnimationLevel[" + holder.getStringUUID() + "]";
    }
    
    @Override
    public FeatureFlagSet enabledFeatures() {
        return getParent().enabledFeatures();
    }
    
    @Override
    public boolean shouldUseLightingForRenderig() {
        return false;
    }
    
    public boolean isEmpty() {
        for (LevelChunk chunk : chunks())
            if (!chunk.getBlockEntities().isEmpty())
                return false;
        return true;
    }
    
    public void addFreshEntityFromPacket(Entity entity) {
        if (NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, this)).isCanceled())
            return;
        removeEntityById(entity.getId(), Entity.RemovalReason.DISCARDED);
        entities.addNewEntityWithoutEvent(entity);
        entity.onAddedToLevel();
    }
    
    @Override
    public void removeEntityById(int id, RemovalReason reason) {
        Entity entity = this.getEntities().get(id);
        if (entity != null) {
            entity.setRemoved(reason);
            if (LittleTilesClient.ANIMATION_HANDLER.checkInTransition(entity))
                return;
            entity.onClientRemoval();
        }
    }
    
    public void clearTrackingChanges() {
        if (trackedChanges != null)
            trackedChanges.clear();
    }
    
    @Override
    public boolean addFreshEntity(Entity entity) {
        if (isClientSide || entity.isRemoved())
            return false;
        
        if (this.entities.addNewEntity(entity)) {
            entity.onAddedToLevel();
            return true;
        }
        return false;
    }
    
    @Override
    public TickRateManager tickRateManager() {
        return parentLevel.tickRateManager();
    }
    
    @Override
    public PotionBrewing potionBrewing() {
        return parentLevel.potionBrewing();
    }
    
    @Override
    public void setDayTimeFraction(float dayTimeFraction) {
        parentLevel.setDayTimeFraction(dayTimeFraction);
    }
    
    @Override
    public float getDayTimeFraction() {
        return parentLevel.getDayTimeFraction();
    }
    
    @Override
    public float getDayTimePerTick() {
        return parentLevel.getDayTimePerTick();
    }
    
    @Override
    public void setDayTimePerTick(float dayTimePerTick) {
        parentLevel.setDayTimePerTick(dayTimePerTick);
    }
    
}
