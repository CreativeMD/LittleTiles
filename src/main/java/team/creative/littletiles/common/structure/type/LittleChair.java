package team.creative.littletiles.common.structure.type;

import java.util.UUID;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class LittleChair extends LittleStructure {
    
    private UUID sitUUID;
    private Player player;
    
    public LittleChair(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(CompoundTag nbt) {
        if (nbt.contains("sit"))
            sitUUID = UUID.fromString(nbt.getString("sit"));
        else
            sitUUID = null;
    }
    
    @Override
    protected void writeToNBTExtra(CompoundTag nbt) {
        if (sitUUID != null)
            nbt.putString("sit", sitUUID.toString());
        else
            nbt.remove("sit");
    }
    
    public void setPlayer(Player player) {
        this.player = player;
        if (!getLevel().isClientSide)
            getInput(0).updateState(BooleanUtils.asArray(player != null));
        if (this.player == null)
            sitUUID = null;
    }
    
    @Override
    protected void afterPlaced() {
        super.afterPlaced();
        if (sitUUID != null) {
            Level level = getLevel();
            if (level instanceof IOrientatedLevel) {
                if (level instanceof CreativeLevel && ((CreativeLevel) level).parent == null)
                    return;
                level = ((IOrientatedLevel) level).getRealLevel();
            }
            for (Entity entity : level.loadedEntityList)
                if (entity.getUUID().equals(sitUUID) && entity instanceof EntitySit) {
                    EntitySit sit = (EntitySit) entity;
                    StructureChildConnection temp = this.generateConnection(sit);
                    sit.getEntityData().set(EntitySit.CONNECTION, temp.writeToNBT(new CompoundTag()));
                    break;
                }
        }
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide) {
            if (this.player != null)
                return InteractionResult.SUCCESS;
            try {
                LittleVecAbsolute vec = getHighestCenterPoint();
                if (vec != null) {
                    if (level instanceof IOrientatedLevel)
                        level = ((IOrientatedLevel) level).getRealLevel();
                    EntitySit sit = new EntitySit(this, level, vec.getPosX(), vec.getPosY() - 0.25, vec.getPosZ());
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
    
    public static class LittleChairParser extends LittleStructureGuiParser {
        
        public LittleChairParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleStructure parseStructure(LittlePreviews previews) {
            return createStructure(LittleChair.class, null);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleChair.class);
        }
    }
}
