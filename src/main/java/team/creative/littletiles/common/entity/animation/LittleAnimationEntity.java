package team.creative.littletiles.common.entity.animation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.entity.LittleAnimationRenderManager;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.entity.OrientationAwareEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.packet.entity.animation.LittleAnimationInitPacket;
import team.creative.littletiles.common.packet.entity.animation.LittleBlockChange;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.direct.StructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleAnimationEntity extends LittleEntity<LittleAnimationEntityPhysic> {
    
    public static void loadBE(LevelAccessor level, CompoundTag nbt) {
        BlockState state = BlockTile.getState(nbt.getBoolean("ticking"), nbt.getBoolean("rendered"));
        BlockPos pos = BlockEntity.getPosFromTag(nbt);
        level.setBlock(pos, state, 0);
        
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof BETiles be)
            be.handleUpdate(nbt, false);
    }
    
    public static CompoundTag saveBE(BETiles tiles) {
        CompoundTag nbt = tiles.serializeNBT();
        nbt.putBoolean("ticking", tiles.isTicking());
        nbt.putBoolean("rendered", tiles.isRendered());
        return nbt;
    }
    
    private StructureAbsolute center;
    private StructureConnection structure;
    
    public LittleAnimationEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleAnimationEntity(Level level, LittleAnimationLevel subLevel, StructureAbsolute center, Placement placement) throws LittleActionException {
        super(LittleTilesRegistry.ENTITY_ANIMATION.get(), level, subLevel, center.rotationCenter);
        setCenter(center);
        beforeInitalPlacement();
        PlacementResult result = placement.place();
        if (result == null)
            throw new LittleActionException("Could not be placed");
        this.structure = new StructureConnection(subLevel, new LocalStructureLocation(result.parentStructure));
    }
    
    protected void beforeInitalPlacement() {
        if (level.isClientSide)
            getSubLevel().renderManager = new LittleAnimationRenderManager(this);
    }
    
    @Override
    protected LittleSubLevel createLevel() {
        return new LittleAnimationLevel(level);
    }
    
    @Override
    public LittleAnimationLevel getSubLevel() {
        return (LittleAnimationLevel) super.getSubLevel();
    }
    
    public void setCenter(StructureAbsolute center) {
        this.center = center;
        if (getOrigin() != null)
            getOrigin().setCenter(center.rotationCenter);
        for (OrientationAwareEntity entity : children())
            entity.parentVecOriginChange(origin);
    }
    
    public void setParentLevel(Level subLevel) {
        this.level = subLevel;
        getSubLevel().setParent(subLevel);
        getSubLevel().setOrigin(center.rotationCenter);
        this.origin = this.subLevel.getOrigin();
        hasOriginChanged = true;
    }
    
    public StructureAbsolute getCenter() {
        return center;
    }
    
    public boolean is(LittleStructure structure) {
        return this.structure.is(structure);
    }
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        return structure.getStructure();
    }
    
    public void applyChanges(Iterable<LittleBlockChange> changes) {
        for (LittleBlockChange change : changes)
            if (change.isEmpty())
                subLevel.removeBlock(change.pos(), true);
            else
                loadBE(subLevel, change.block());
    }
    
    protected void loadBlocks(CompoundTag nbt) {
        LittleAnimationLevel level = getSubLevel();
        ListTag blocks = nbt.getList("b", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++)
            loadBE(level, blocks.getCompound(i));
    }
    
    protected void saveBlocks(CompoundTag nbt) {
        ListTag blocks = new ListTag();
        for (BETiles block : getSubLevel())
            blocks.add(saveBE(block));
        nbt.put("b", blocks);
    }
    
    @Override
    protected Vec3d loadCenter(CompoundTag nbt) {
        this.center = new StructureAbsolute("c", nbt);
        return center.rotationCenter;
    }
    
    @Override
    public void loadEntity(CompoundTag nbt) {
        setCenter(center); // center half way loaded due to loadCenter called before
        loadBlocks(nbt);
        
        this.structure = new StructureConnection((Level) subLevel, nbt.getCompound("s"));
    }
    
    @Override
    public void saveEntity(CompoundTag nbt) {
        nbt.put("s", structure.write());
        center.save("c", nbt);
        saveBlocks(nbt);
    }
    
    @Override
    public CreativePacket initClientPacket() {
        return new LittleAnimationInitPacket(this);
    }
    
    public CompoundTag saveExtraClientData() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("physic", physic.save());
        saveBlocks(nbt);
        return nbt;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void initSubLevelClient(StructureAbsolute absolute, CompoundTag extraData) {
        setSubLevel(new LittleAnimationLevel(level), absolute.rotationCenter);
        setCenter(absolute);
        ((LittleAnimationLevel) subLevel).renderManager = new LittleAnimationRenderManager(this);
        loadBlocks(extraData);
        physic.load(extraData.getCompound("physic"));
    }
    
    @Override
    public void performTick() {
        super.performTick();
        if (!level.isClientSide && getSubLevel().isEmpty())
            destroyAnimation();
    }
    
    @Override
    public void internalTick() {}
    
    @Override
    public void initialTick() {
        getSubLevel().initialTick();
    }
    
    @Override
    protected LittleAnimationEntityPhysic createPhysic() {
        return new LittleAnimationEntityPhysic(this);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager getRenderManager() {
        return ((LittleAnimationLevel) subLevel).renderManager;
    }
    
    @Override
    public void syncMovement() {}
    
    @Override
    public void startTracking(ServerPlayer player) {
        getSubLevel().entityCallback.addTrackingPlayer(player);
    }
    
    @Override
    public void stopTracking(ServerPlayer player) {
        getSubLevel().entityCallback.removeTrackingPlayer(player);
    }
    
    public void clearTrackingChanges() {
        getSubLevel().clearTrackingChanges();
    }
    
}
