package team.creative.littletiles.common.structure.type;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;

public class LittleChair extends LittleStructure {
    
    private UUID sitUUID;
    private Player player;
    
    public LittleChair(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (nbt.contains("sit"))
            sitUUID = UUID.fromString(nbt.getString("sit"));
        else
            sitUUID = null;
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        if (sitUUID != null)
            nbt.putString("sit", sitUUID.toString());
        else
            nbt.remove("sit");
    }
    
    public void setPlayer(Player player) {
        this.player = player;
        if (!isClient())
            getInput(0).updateState(SignalState.of(player != null));
        if (this.player == null)
            sitUUID = null;
    }
    
    @Override
    public void afterPlaced() {
        super.afterPlaced();
        if (sitUUID != null) {
            LevelAccessor level = getStructureLevel();
            if (level instanceof IOrientatedLevel) {
                if (!(level instanceof ISubLevel))
                    return;
                level = ((ISubLevel) level).getRealLevel();
            }
            if (!level.isClientSide()) {
                Iterable<Entity> iterable;
                if (level instanceof ServerLevel)
                    iterable = ((ServerLevel) level).getAllEntities();
                else if (level instanceof LittleLevel little)
                    iterable = little.entities();
                else
                    throw new UnsupportedOperationException();
                
                for (Entity entity : iterable)
                    if (entity.getUUID().equals(sitUUID) && entity instanceof EntitySit) {
                        EntitySit sit = (EntitySit) entity;
                        StructureChildConnection temp = this.children.generateConnection(sit);
                        sit.getEntityData().set(EntitySit.CONNECTION, temp.save(new CompoundTag()));
                        break;
                    }
            }
            
        }
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide) {
            if (this.player != null)
                return InteractionResult.SUCCESS;
            try {
                LittleVecAbsolute vec = getHighestCenterPoint();
                if (vec != null) {
                    if (level instanceof ISubLevel)
                        level = ((ISubLevel) level).getRealLevel();
                    EntitySit sit = new EntitySit(this, level, vec.getPosX(), vec.getPosY(), vec.getPosZ());
                    sitUUID = sit.getUUID();
                    player.startRiding(sit);
                    level.addFreshEntity(sit);
                    setPlayer(player);
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                e.printStackTrace();
            }
            
        }
        return InteractionResult.SUCCESS;
    }
}
