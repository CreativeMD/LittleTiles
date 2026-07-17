package team.creative.littletiles.client.tool;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.BlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.item.WrenchRequestInfoPacket;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleToolWrench extends LittleTool {
    
    private List<StructureTooltip> requestingStructures;
    private List<StructureTooltip> receivedStructures;
    private long tickTime;
    private boolean received = true;
    
    public LittleToolWrench(ItemStack stack) {
        super(stack);
    }
    
    private boolean containsStructure(LittleStructure structure) {
        for (StructureTooltip s : requestingStructures)
            if (s.structure == structure)
                return true;
        return false;
    }
    
    protected void scanLevel(LevelAccessor level, MutableBlockPos pos, BlockPos origin, int range) {
        for (int x = origin.getX() - range; x <= origin.getX() + range; x++)
            for (int y = origin.getY() - range; y <= origin.getY() + range; y++)
                for (int z = origin.getZ() - range; z <= origin.getZ() + range; z++) {
                    pos.set(x, y, z);
                    BETiles be = BlockTile.loadBE(level, pos);
                    if (be == null)
                        continue;
                    for (LittleStructure s : be.loadedStructures())
                        if (s.hasWrenchInfo() && !containsStructure(s))
                            try {
                                Vec3 center = s.getSurroundingBox().getAABB().getCenter();
                                requestingStructures.add(new StructureTooltip(s, center));
                            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                }
    }
    
    public void receive(List<List<Component>> result) {
        received = true;
        if (result.size() == requestingStructures.size()) {
            for (int i = 0; i < requestingStructures.size(); i++)
                requestingStructures.get(i).receive(result.get(i));
            receivedStructures = requestingStructures;
            requestingStructures = null;
        } else
            requestingStructures = null;
    }
    
    @Override
    protected void tickInternal(PreviewRenderer renderer) {
        if (tickTime <= System.currentTimeMillis() && received) {
            MutableBlockPos pos = new MutableBlockPos();
            BlockPos origin = renderer.player().blockPosition();
            pos.set(origin);
            
            requestingStructures = new ArrayList<>();
            
            int range = LittleTiles.CONFIG.rendering.wrenchInfoRange;
            scanLevel(renderer.level(), pos, origin, range);
            
            AABB bb = new AABB(origin.getX() - range, origin.getY() - range, origin.getZ() - range, origin.getX() + 1 + range, origin.getY() + 1 + range, origin
                    .getZ() + 1 + range);
            for (LittleEntity entity : LittleTilesClient.ANIMATION_HANDLER.find(bb))
                scanLevel(entity.getSubLevel(), pos, origin, range);
            
            received = false;
            List<StructureLocation> structures = new ArrayList<>();
            for (int i = 0; i < requestingStructures.size(); i++)
                structures.add(requestingStructures.get(i).structure.getStructureLocation());
            LittleTiles.NETWORK.sendToServer(new WrenchRequestInfoPacket(structures));
            
            tickTime = System.currentTimeMillis() + (long) (LittleTiles.CONFIG.rendering.wrenchInfoRefresh * 1000);
        }
    }
    
    @Override
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (lines || receivedStructures == null || receivedStructures.isEmpty())
            return;
        
        Minecraft mc = Minecraft.getInstance();
        var cameraOrientation = mc.getEntityRenderDispatcher().cameraOrientation();
        var buffer = mc.renderBuffers().bufferSource();
        
        boolean focus = false;
        LittleStructure focusedStructure = null;
        if (Screen.hasShiftDown()) {
            focus = true;
            if (mc.hitResult.getType() == Type.BLOCK && mc.hitResult instanceof BlockHitResult b && renderer.level().getBlockState(b.getBlockPos())
                    .getBlock() instanceof BlockTile) {
                LittleTileContext tileContext = renderer.selectFocused(b);
                if (tileContext.isComplete() && tileContext.parent.isStructure())
                    try {
                        focusedStructure = tileContext.parent.getStructure();
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            }
        }
        
        buffer.endBatch();
        
        pose.pushPose();
        pose.translate((float) -cam.x, (float) -cam.y, (float) -cam.z);
        for (StructureTooltip s : receivedStructures)
            if (s.visible(focus, focusedStructure))
                s.render(pose, buffer, mc.font, cameraOrientation);
        pose.popPose();
        
        RenderSystem.disableDepthTest();
        buffer.endBatch();
        RenderSystem.enableDepthTest();
    }
    
    @Override
    public boolean onRightClick(PreviewRenderer renderer, Level level, BlockHitResult result) {
        LittleTileContext context = renderer.selectFocused(result);
        if (renderer.isUsingSecondMode()) {
            try {
                if (context.isComplete() && context.parent.isStructure())
                    if (context.parent.getStructure().wrenchInteract(renderer.player()))
                        LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), renderer.player(), BlockPacketAction.WRENCH_INFO));
                    else
                        LittleTilesGuiRegistry.STRUCTURE_SIGNAL.open(renderer.player(), context.parent.getStructure());
                else
                    LittleTiles.NETWORK.sendToServer(new BlockPacket(level, result.getBlockPos(), renderer.player(), BlockPacketAction.WRENCH));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        return true;
    }
    
    public static class StructureTooltip {
        
        public final LittleStructure structure;
        public final Vec3 pos;
        public List<Component> info;
        
        public StructureTooltip(LittleStructure structure, Vec3 pos) {
            this.structure = structure;
            this.pos = pos;
        }
        
        public void receive(List<Component> info) {
            this.info = info;
        }
        
        public boolean visible(boolean focus, LittleStructure structure) {
            try {
                return !focus || this.structure.isChildOf(structure);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                return false;
            }
        }
        
        public void render(PoseStack pose, MultiBufferSource buffer, Font font, Quaternionf cameraOrientation) {
            if (info == null)
                return;
            int light = 255;
            pose.pushPose();
            Vec3 pos = this.pos;
            if (structure.getStructureLevel() instanceof IOrientatedLevel l)
                pos = l.getOrigin().transformPointToWorld(pos);
            pose.translate(pos.x, pos.y, pos.z);
            pose.mulPose(cameraOrientation);
            float scale = 0.005F;
            pose.scale(scale, -scale, scale);
            Matrix4f matrix4f = pose.last().pose();
            int j = 255;
            
            int x = 0;
            for (int i = 0; i < info.size(); i++) {
                Component text = info.get(i);
                float f1 = -font.width(text) / 2;
                font.drawInBatch(text, f1, x, -1, true, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, j, light);
                x += 10;
            }
            
            pose.popPose();
        }
        
    }
    
}
