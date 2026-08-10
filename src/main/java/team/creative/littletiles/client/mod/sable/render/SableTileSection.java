package team.creative.littletiles.client.mod.sable.render;

public interface SableTileSection {
    
    SableTileMesh.UploadedMesh getTileMesh();
    
    void publishTiles(SableTileMesh.UploadedMesh mesh);
    
}
