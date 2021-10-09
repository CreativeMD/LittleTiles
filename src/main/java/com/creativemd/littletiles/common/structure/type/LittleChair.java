package com.creativemd.littletiles.common.structure.type;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.registry.LittleStructureType;

public class LittleChair extends LittleStructure {
    
    private UUID sitUUID;
    private EntityPlayer player;
    
    public LittleChair(LittleStructureType type, IStructureTileList mainBlock) {
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
    
    public void setPlayer(EntityPlayer player) {
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
