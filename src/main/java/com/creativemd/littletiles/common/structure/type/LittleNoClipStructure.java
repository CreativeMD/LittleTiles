package com.creativemd.littletiles.common.structure.type;

import java.util.HashSet;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleNoClipStructure extends LittleStructure {
    
    public HashSet<Entity> entities = new HashSet<>();
    
    public boolean web = true;
    
    public LittleNoClipStructure(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        web = nbt.getBoolean("web");
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setBoolean("web", web);
    }
    
    @Override
    public void onEntityCollidedWithBlock(World worldIn, IParentTileList parent, BlockPos pos, Entity entityIn) {
        if (web)
            entityIn.setInWeb();
        if (worldIn.isRemote)
            return;
        
        boolean intersected = false;
        for (LittleTile tile : parent) {
            if (tile.getBox().getBox(parent.getContext(), pos).intersects(entityIn.getEntityBoundingBox())) {
                intersected = true;
                break;
            }
        }
        
        if (intersected)
            entities.add(entityIn);
        
        queueForNextTick();
    }
    
    @Override
    public void onEntityCollidedWithBlockAnimation(EntityAnimation animation, HashMap<Entity, AxisAlignedBB> entities) {
        if (web)
            entities.keySet().forEach(x-> x.setInWeb());
        
        if (animation.world.isRemote)
            return;
        
        this.entities.addAll(entities.keySet());
        queueForNextTick();
    }
    
    @Override
    public boolean queueTick() {
        int players = 0;
        for (Entity entity : entities)
            if (entity instanceof EntityPlayer)
                players++;
        getInput(0).updateState(BooleanUtils.toBits(players, 4));
        getInput(1).updateState(BooleanUtils.toBits(entities.size(), 4));
        boolean wasEmpty = entities.isEmpty();
        entities.clear();
        return !wasEmpty;
    }
    
    public static class LittleNoClipStructureParser extends LittleStructureGuiParser {
        
        public LittleNoClipStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            boolean slowness = true;
            if (structure instanceof LittleNoClipStructure)
                slowness = ((LittleNoClipStructure) structure).web;
            parent.controls.add(new GuiCheckBox("web", "slowness (cobwebs)", 3, 0, slowness));
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleNoClipStructure parseStructure(LittlePreviews previews) {
            LittleNoClipStructure structure = createStructure(LittleNoClipStructure.class, null);
            structure.web = ((GuiCheckBox) parent.get("web")).value;
            
            return structure;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleNoClipStructure.class);
        }
    }
    
}
