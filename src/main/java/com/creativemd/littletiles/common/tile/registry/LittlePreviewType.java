package com.creativemd.littletiles.common.tile.registry;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;

public class LittlePreviewType {
    
    public final String id;
    public final Class<? extends LittlePreview> clazz;
    
    public LittlePreviewType(String id, Class<? extends LittlePreview> clazz) {
        this.id = id;
        this.clazz = clazz;
    }
    
}
