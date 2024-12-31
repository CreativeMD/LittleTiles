package team.creative.littletiles.client.mod.sodium.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder.Vertex;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.NormalHelper;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.core.Direction;
import team.creative.littletiles.client.mod.sodium.SodiumInteractor;

public class LittleQuadView implements QuadView {
    
    private final Vertex[] vertices;
    private Vector3f normal = new Vector3f();
    private Direction nominalFace;
    private int packedNormal;
    private ModelQuadFacing quadFacing;
    
    public LittleQuadView() {
        this(Vertex.uninitializedQuad());
    }
    
    public LittleQuadView(Vertex[] vertices) {
        this.vertices = vertices;
    }
    
    public void readVertices(long ptr, int posPtrOffset, int stride, ModelQuadFacing facing) {
        for (int j = 0; j < 4; j++) { // Iterate over 4 vertices because they make a quad
            int hi = MemoryUtil.memGetInt(ptr + posPtrOffset);
            int lo = MemoryUtil.memGetInt(ptr + posPtrOffset + 4);
            vertices[j].x = SodiumInteractor.unpackPositionX(hi, lo);
            vertices[j].y = SodiumInteractor.unpackPositionY(hi, lo);
            vertices[j].z = SodiumInteractor.unpackPositionZ(hi, lo);
            ptr += stride;
        }
        nominalFace = SodiumInteractor.toDirection(facing);
        NormalHelper.computeFaceNormal(normal, this);
        quadFacing = ModelQuadFacing.fromNormal(normal.x, normal.y, normal.z);
        packedNormal = NormI8.pack(normal);
    }
    
    public Vertex[] getVertices() {
        return vertices;
    }
    
    public int getPackedNormal() {
        return packedNormal;
    }
    
    @Override
    public float x(int vertexIndex) {
        return vertices[vertexIndex].x;
    }
    
    @Override
    public float y(int vertexIndex) {
        return vertices[vertexIndex].y;
    }
    
    @Override
    public float z(int vertexIndex) {
        return vertices[vertexIndex].z;
    }
    
    public ModelQuadFacing quadFacing() {
        return quadFacing;
    }
    
    @Override
    public float posByIndex(int vertexIndex, int coordinateIndex) {
        return switch (coordinateIndex) {
            case 0 -> vertices[vertexIndex].x;
            case 1 -> vertices[vertexIndex].y;
            case 2 -> vertices[vertexIndex].z;
            default -> throw new UnsupportedOperationException();
        };
    }
    
    @Override
    public Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int color(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public float u(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public float v(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int lightmap(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean hasNormal(int vertexIndex) {
        return false;
    }
    
    @Override
    public float normalX(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public float normalY(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public float normalZ(int vertexIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public @Nullable Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Vector3f faceNormal() {
        return normal;
    }
    
    @Override
    public RenderMaterial material() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int colorIndex() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int tag() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void toVanilla(int[] target, int targetIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public @Nullable Direction cullFace() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public @NotNull Direction lightFace() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public @Nullable Direction nominalFace() {
        return nominalFace;
    }
    
}
