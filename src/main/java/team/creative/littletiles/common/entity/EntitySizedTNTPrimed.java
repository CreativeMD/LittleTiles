package team.creative.littletiles.common.entity;

import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraft.world.entity.item.PrimedTnt;
import team.creative.littletiles.common.math.vec.LittleVec;

public class EntitySizedTNTPrimed extends PrimedTnt {
    
    private static final DataParameter<String> TNTSIZE = EntityDataManager.<String>createKey(EntitySizedTNTPrimed.class, DataSerializers.STRING);
    
    public EntitySizedTNTPrimed(World worldIn) {
        super(worldIn);
    }
    
    public LittleGridContext context;
    public LittleVec size;
    
    public EntitySizedTNTPrimed(World worldIn, double x, double y, double z, EntityLivingBase igniter, LittleGridContext context, LittleVec size) {
        super(worldIn, x, y, z, igniter);
        // setFuse(1000);
        setSize(context, size);
        setSize((float) size.getPosX(context), (float) size.getPosY(context));
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TNTSIZE, "1.1.1.1");
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        size.writeToNBT("size", compound);
        context.set(compound);
    }
    
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        size = new LittleVec("size", compound);
        context = LittleGridContext.get(compound);
    }
    
    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (TNTSIZE.equals(key)) {
            String data = this.dataManager.get(TNTSIZE);
            String[] coords = data.split("\\.");
            this.size = new LittleVec(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
            
            this.context = LittleGridContext.get(Integer.parseInt(coords[3]));
            
        }
    }
    
    public void setSize(LittleGridContext context, LittleVec size) {
        this.dataManager.set(TNTSIZE, size.x + "." + size.y + "." + size.z + "." + context.size);
        this.size = size;
        this.context = context;
    }
    
    @Override
    public void onUpdate() {
        if (this.getFuse() <= 1) {
            this.setDead();
            
            if (!this.world.isRemote) {
                this.explode();
            }
        } else
            super.onUpdate();
    }
    
    @Override
    protected void explode() {
        this.world.createExplosion(this, this.posX, this.posY + this.height / 16.0F, this.posZ, (float) (4.0D * size.getPercentVolume(context)), true);
    }
    
}
