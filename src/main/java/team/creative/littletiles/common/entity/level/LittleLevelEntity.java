package team.creative.littletiles.common.entity.level;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleChunkSerializer;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.packet.entity.level.LittleLevelInitPacket;
import team.creative.littletiles.server.level.little.LittleServerLevel;
import team.creative.littletiles.server.level.little.SubServerLevel;

public class LittleLevelEntity extends LittleEntity<LittleLevelEntityPhysic> {
    
    public BlockPos center;
    
    public LittleLevelEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleLevelEntity(Level level, BlockPos pos) {
        super(LittleTilesRegistry.ENTITY_LEVEL.get(), level, new Vec3d(pos));
        this.center = pos;
    }
    
    @Override
    protected LittleSubLevel createLevel() {
        return SubServerLevel.createSubLevel(level);
    }
    
    @Override
    protected LittleLevelEntityPhysic createPhysic() {
        return new LittleLevelEntityPhysic(this);
    }
    
    @Override
    protected Vec3d loadCenter(CompoundTag nbt) {
        return new Vec3d(nbt.getInt("cX"), nbt.getInt("cY"), nbt.getInt("cZ"));
    }
    
    @Override
    public void loadEntity(CompoundTag nbt) {
        center = new BlockPos(nbt.getInt("cX"), nbt.getInt("cY"), nbt.getInt("cZ"));
        LittleServerLevel sub = (LittleServerLevel) subLevel;
        ListTag chunks = nbt.getList("chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag chunk = chunks.getCompound(i);
            sub.load(new ChunkPos(chunk.getInt("xPos"), chunk.getInt("zPos")), chunk);
        }
    }
    
    @Override
    public void saveEntity(CompoundTag nbt) {
        nbt.putInt("cX", center.getX());
        nbt.putInt("cY", center.getY());
        nbt.putInt("cZ", center.getZ());
        LittleServerLevel sub = (LittleServerLevel) subLevel;
        ListTag chunks = new ListTag();
        for (ChunkAccess chunk : sub.chunks())
            chunks.add(LittleChunkSerializer.write(sub, chunk));
        nbt.put("chunks", chunks);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public CreativePacket initClientPacket() {
        return new LittleLevelInitPacket(this);
    }
    
    @OnlyIn(Dist.CLIENT)
    public CompoundTag saveExtraClientData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("cX", center.getX());
        nbt.putInt("cY", center.getY());
        nbt.putInt("cZ", center.getZ());
        nbt.put("physic", physic.save());
        return nbt;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void initSubLevelClient(CompoundTag extraData) {
        setSubLevel(createLevel(), new Vec3d(extraData.getInt("cX"), extraData.getInt("cY"), extraData.getInt("cZ")));
        ((LittleClientLevel) subLevel).renderManager = new LittleLevelRenderManager(this);
        physic.load(extraData.getCompound("physic"));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager getRenderManager() {
        return ((LittleClientLevel) subLevel).renderManager;
    }
    
    @Override
    public void performTick() {
        super.performTick();
        if (!level.isClientSide && physic.getBlockUpdateLevelSystem().isEntirelyEmpty())
            destroyAnimation();
    }
    
    @Override
    public void internalTick() {}
    
    @Override
    public void initialTick() {}
    
}
