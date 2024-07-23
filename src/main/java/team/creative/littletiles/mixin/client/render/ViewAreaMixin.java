package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.client.render.mc.ViewAreaExtender;

@Mixin(ViewArea.class)
public abstract class ViewAreaMixin implements ViewAreaExtender {
    
    @Final
    @Shadow
    protected Level level;
    @Shadow
    protected int sectionGridSizeY;
    @Shadow
    protected int sectionGridSizeX;
    @Shadow
    protected int sectionGridSizeZ;
    
    @Shadow
    public SectionRenderDispatcher.RenderSection[] sections;
    
    @Override
    public RenderSection getSection(long pos) {
        int x = Math.floorMod(SectionPos.x(pos), this.sectionGridSizeX);
        int y = Math.floorMod(SectionPos.y(pos) - this.level.getMinSection(), this.sectionGridSizeY);
        int z = Math.floorMod(SectionPos.z(pos), this.sectionGridSizeZ);
        return this.sections[this.getSectionIndex(x, y, z)];
    }
    
    @Shadow
    public abstract int getSectionIndex(int x, int y, int z);
    
}
