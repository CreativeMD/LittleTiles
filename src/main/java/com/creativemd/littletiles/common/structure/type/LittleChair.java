package com.creativemd.littletiles.common.structure.type;

import java.util.UUID;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
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
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        if (nbt.hasKey("sit"))
            sitUUID = UUID.fromString(nbt.getString("sit"));
        else
            sitUUID = null;
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        if (sitUUID != null)
            nbt.setString("sit", sitUUID.toString());
        else
            nbt.removeTag("sit");
    }
    
    public void setPlayer(Player player) {
        this.player = player;
        if (!getWorld().isRemote)
            getInput(0).updateState(BooleanUtils.asArray(player != null));
        if (this.player == null)
            sitUUID = null;
    }
    
    @Override
    protected void afterPlaced() {
        super.afterPlaced();
        if (sitUUID != null) {
            World world = getWorld();
            if (world instanceof IOrientatedWorld) {
                if (world instanceof CreativeWorld && ((CreativeWorld) world).parent == null)
                    return;
                world = ((IOrientatedWorld) world).getRealWorld();
            }
            for (Entity entity : world.loadedEntityList)
                if (entity.getUniqueID().equals(sitUUID) && entity instanceof EntitySit) {
                    EntitySit sit = (EntitySit) entity;
                    StructureChildConnection temp = this.generateConnection(sit);
                    sit.getDataManager().set(EntitySit.CONNECTION, temp.writeToNBT(new NBTTagCompound()));
                    break;
                }
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
        if (!world.isRemote) {
            if (this.player != null)
                return true;
            try {
                LittleVecAbsolute vec = getHighestCenterPoint();
                if (vec != null) {
                    if (world instanceof IOrientatedWorld)
                        world = ((IOrientatedWorld) world).getRealWorld();
                    EntitySit sit = new EntitySit(this, world, vec.getPosX(), vec.getPosY() - 0.25, vec.getPosZ());
                    sitUUID = sit.getPersistentID();
                    player.startRiding(sit);
                    world.spawnEntity(sit);
                    setPlayer(player);
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                e.printStackTrace();
            }
            
        }
        return true;
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
