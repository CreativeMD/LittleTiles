package team.creative.littletiles.common.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage.ActionMessageObjectType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import team.creative.creativecore.common.network.type.NetworkFieldTypeClass;
import team.creative.creativecore.common.network.type.NetworkFieldTypes;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.LittleBlockRegistry;
import team.creative.littletiles.common.filter.TileFilter;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.math.location.TileLocation;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.collection.LittleCollection;
import team.creative.littletiles.common.tile.group.LittleGroup;
import team.creative.littletiles.common.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.tile.group.LittleGroupHolder;

public class LittlePacketTypes {
    
    public static void init() {
        NetworkFieldTypes.register(new NetworkFieldTypeClass<TileLocation>() {
            
            @Override
            protected void writeContent(TileLocation content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeBoolean(content.isStructure);
                buffer.writeInt(content.index);
                buffer.writeVarIntArray(content.box.getArray());
                if (content.levelUUID != null) {
                    buffer.writeBoolean(true);
                    buffer.writeUUID(content.levelUUID);
                } else
                    buffer.writeBoolean(false);
            }
            
            @Override
            protected TileLocation readContent(FriendlyByteBuf buffer) {
                BlockPos pos = buffer.readBlockPos();
                boolean isStructure = buffer.readBoolean();
                int index = buffer.readInt();
                int[] boxArray = buffer.readVarIntArray();
                UUID level = null;
                if (buffer.readBoolean())
                    level = buffer.readUUID();
                return new TileLocation(pos, isStructure, index, LittleBox.create(boxArray), level);
            }
            
        }, TileLocation.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<StructureLocation>() {
            
            @Override
            protected void writeContent(StructureLocation content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeInt(content.index);
                if (content.levelUUID != null) {
                    buffer.writeBoolean(true);
                    buffer.writeUUID(content.levelUUID);
                } else
                    buffer.writeBoolean(false);
            }
            
            @Override
            protected StructureLocation readContent(FriendlyByteBuf buffer) {
                BlockPos pos = buffer.readBlockPos();
                int index = buffer.readInt();
                UUID level = null;
                if (buffer.readBoolean())
                    level = buffer.readUUID();
                return new StructureLocation(pos, index, level);
            }
            
        }, StructureLocation.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleTile>() {
            
            @Override
            protected void writeContent(LittleTile content, FriendlyByteBuf buffer) {
                buffer.writeInt(content.size());
                for (LittleBox box : content)
                    buffer.writeVarIntArray(box.getArray());
                buffer.writeUtf(content.block.blockName());
                buffer.writeInt(content.color);
            }
            
            @Override
            protected LittleTile readContent(FriendlyByteBuf buffer) {
                int size = buffer.readInt();
                List<LittleBox> boxes = new ArrayList<>(size);
                for (int i = 0; i < size; i++)
                    boxes.add(LittleBox.create(buffer.readVarIntArray()));
                return new LittleTile(LittleBlockRegistry.get(buffer.readUtf()), buffer.readInt(), boxes);
            }
            
        }, LittleTile.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleCollection>() {
            
            @Override
            protected void writeContent(LittleCollection content, FriendlyByteBuf buffer) {
                buffer.writeInt(content.size());
                for (LittleTile tile : content)
                    NetworkFieldTypes.write(LittleTile.class, tile, buffer);
            }
            
            @Override
            protected LittleCollection readContent(FriendlyByteBuf buffer) {
                int size = buffer.readInt();
                LittleCollection collection = new LittleCollection();
                for (int i = 0; i < size; i++)
                    collection.add(NetworkFieldTypes.read(LittleTile.class, buffer));
                return collection;
            }
            
        }, LittleGroup.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleGroup>() {
            
            @Override
            protected void writeContent(LittleGroup content, FriendlyByteBuf buffer) {
                if (content instanceof LittleGroupHolder)
                    throw new RuntimeException("LittleGroupHolder cannot be send across the network");
                
                buffer.writeInt(content.children.sizeChildren());
                for (LittleGroup child : content.children.children())
                    NetworkFieldTypes.write(LittleGroup.class, child, buffer);
                
                buffer.writeInt(content.getGrid().count);
                if (content.hasStructure()) {
                    buffer.writeBoolean(true);
                    buffer.writeNbt(content.getStructureTag());
                } else
                    buffer.writeBoolean(false);
                
                buffer.writeInt(content.size());
                for (LittleTile tile : content)
                    NetworkFieldTypes.write(LittleTile.class, tile, buffer);
                
                buffer.writeInt(content.children.sizeExtensions());
                for (Entry<String, LittleGroup> extension : content.children.extensionEntries()) {
                    buffer.writeUtf(extension.getKey());
                    NetworkFieldTypes.write(LittleGroup.class, extension.getValue(), buffer);
                }
            }
            
            @Override
            protected LittleGroup readContent(FriendlyByteBuf buffer) {
                int size = buffer.readInt();
                List<LittleGroup> children = new ArrayList<>(size);
                for (int i = 0; i < size; i++)
                    children.add(NetworkFieldTypes.read(LittleGroup.class, buffer));
                
                LittleGrid grid = LittleGrid.get(buffer.readInt());
                LittleGroup group = new LittleGroup(buffer.readBoolean() ? buffer.readAnySizeNbt() : null, grid, children);
                int tileCount = buffer.readInt();
                for (int i = 0; i < tileCount; i++)
                    group.add(NetworkFieldTypes.read(LittleTile.class, buffer));
                
                int extensionCount = buffer.readInt();
                for (int i = 0; i < extensionCount; i++)
                    group.children.addExtension(buffer.readUtf(), NetworkFieldTypes.read(LittleGroup.class, buffer));
                
                return group;
            }
            
        }, LittleGroup.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleGroupAbsolute>() {
            
            @Override
            protected void writeContent(LittleGroupAbsolute content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                NetworkFieldTypes.write(LittleGroup.class, content.group, buffer);
            }
            
            @Override
            protected LittleGroupAbsolute readContent(FriendlyByteBuf buffer) {
                return new LittleGroupAbsolute(buffer.readBlockPos(), NetworkFieldTypes.read(LittleGroup.class, buffer));
            }
            
        }, LittleGroupAbsolute.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<PlacementMode>() {
            
            @Override
            protected void writeContent(PlacementMode content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.name);
            }
            
            @Override
            protected PlacementMode readContent(FriendlyByteBuf buffer) {
                return PlacementMode.getModeOrDefault(buffer.readUtf());
            }
            
        }, PlacementMode.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleAction>() {
            
            @Override
            protected void writeContent(LittleAction content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.getClass().getName());
                LittleTiles.NETWORK.getPacketType(content.getClass()).write(content, buffer);
            }
            
            @Override
            protected LittleAction readContent(FriendlyByteBuf buffer) {
                try {
                    Class clazz = Class.forName(buffer.readUtf());
                    return (LittleAction) LittleTiles.NETWORK.getPacketType(clazz).read(buffer);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            
        }, LittleAction.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleGrid>() {
            
            @Override
            protected void writeContent(LittleGrid content, FriendlyByteBuf buffer) {
                buffer.writeInt(content.count);
            }
            
            @Override
            protected LittleGrid readContent(FriendlyByteBuf buffer) {
                return LittleGrid.get(buffer.readInt());
            }
            
        }, LittleGrid.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleVec>() {
            
            @Override
            protected void writeContent(LittleVec vec, FriendlyByteBuf buffer) {
                buffer.writeInt(vec.x);
                buffer.writeInt(vec.y);
                buffer.writeInt(vec.z);
            }
            
            @Override
            protected LittleVec readContent(FriendlyByteBuf buffer) {
                return new LittleVec(buffer.readInt(), buffer.readInt(), buffer.readInt());
            }
            
        }, LittleVec.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleVecGrid>() {
            
            @Override
            protected void writeContent(LittleVecGrid vec, FriendlyByteBuf buffer) {
                buffer.writeInt(vec.getVec().x);
                buffer.writeInt(vec.getVec().y);
                buffer.writeInt(vec.getVec().z);
                buffer.writeInt(vec.getGrid().count);
            }
            
            @Override
            protected LittleVecGrid readContent(FriendlyByteBuf buffer) {
                return new LittleVecGrid(new LittleVec(buffer.readInt(), buffer.readInt(), buffer.readInt()), LittleGrid.get(buffer.readInt()));
            }
            
        }, LittleVecGrid.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleVecAbsolute>() {
            
            @Override
            protected void writeContent(LittleVecAbsolute content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.getPos());
                NetworkFieldTypes.write(LittleVecGrid.class, content.getVecGrid(), buffer);
            }
            
            @Override
            protected LittleVecAbsolute readContent(FriendlyByteBuf buffer) {
                return new LittleVecAbsolute(buffer.readBlockPos(), NetworkFieldTypes.read(LittleVecGrid.class, buffer));
            }
            
        }, LittleVecAbsolute.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<PlacementPosition>() {
            
            @Override
            protected void writeContent(PlacementPosition content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.getPos());
                NetworkFieldTypes.write(LittleVecGrid.class, content.getVecGrid(), buffer);
                buffer.writeEnum(content.facing);
            }
            
            @Override
            protected PlacementPosition readContent(FriendlyByteBuf buffer) {
                return new PlacementPosition(buffer.readBlockPos(), NetworkFieldTypes.read(LittleVecGrid.class, buffer), buffer.readEnum(Facing.class));
            }
            
        }, PlacementPosition.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleBox>() {
            
            @Override
            protected void writeContent(LittleBox content, FriendlyByteBuf buffer) {
                buffer.writeVarIntArray(content.getArray());
            }
            
            @Override
            protected LittleBox readContent(FriendlyByteBuf buffer) {
                return LittleBox.create(buffer.readVarIntArray());
            }
            
        }, LittleBox.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleBoxAbsolute>() {
            
            @Override
            protected void writeContent(LittleBoxAbsolute content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeVarIntArray(content.box.getArray());
                buffer.writeInt(content.getGrid().count);
            }
            
            @Override
            protected LittleBoxAbsolute readContent(FriendlyByteBuf buffer) {
                return new LittleBoxAbsolute(buffer.readBlockPos(), LittleBox.create(buffer.readVarIntArray()), LittleGrid.get(buffer.readInt()));
            }
            
        }, LittleBoxAbsolute.class);
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleBoxes>() {
            
            @Override
            protected void writeContent(LittleBoxes content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeInt(content.grid.count);
                if (content instanceof LittleBoxesSimple) {
                    buffer.writeBoolean(true);
                    buffer.writeInt(content.size());
                    for (LittleBox box : content.all())
                        buffer.writeVarIntArray(box.getArray());
                } else {
                    buffer.writeBoolean(false);
                    HashMapList<BlockPos, LittleBox> map = content.generateBlockWise();
                    buffer.writeInt(map.size());
                    for (Entry<BlockPos, ArrayList<LittleBox>> entry : map.entrySet()) {
                        buffer.writeBlockPos(entry.getKey());
                        buffer.writeInt(entry.getValue().size());
                        for (LittleBox box : entry.getValue())
                            buffer.writeVarIntArray(box.getArray());
                    }
                }
            }
            
            @Override
            protected LittleBoxes readContent(FriendlyByteBuf buffer) {
                BlockPos pos = buffer.readBlockPos();
                LittleGrid grid = LittleGrid.get(buffer.readInt());
                if (buffer.readBoolean()) {
                    LittleBoxes boxes = new LittleBoxesSimple(pos, grid);
                    int length = buffer.readInt();
                    for (int i = 0; i < length; i++)
                        boxes.add(LittleBox.create(buffer.readVarIntArray()));
                    return boxes;
                } else {
                    int posCount = buffer.readInt();
                    HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
                    for (int i = 0; i < posCount; i++) {
                        BlockPos posList = buffer.readBlockPos();
                        int boxCount = buffer.readInt();
                        List<LittleBox> blockBoxes = new ArrayList<>();
                        for (int j = 0; j < boxCount; j++)
                            blockBoxes.add(LittleBox.create(buffer.readVarIntArray()));
                        map.add(posList, blockBoxes);
                    }
                    return new LittleBoxesNoOverlap(pos, grid, map);
                }
            }
            
        }, LittleBoxes.class, LittleBoxesNoOverlap.class, LittleBoxesSimple.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<TileFilters>() {
            
            @Override
            protected void writeContent(TileFilters content, FriendlyByteBuf buffer) {
                buffer.writeNbt(content.write(new CompoundTag()));
            }
            
            @Override
            protected TileFilters readContent(FriendlyByteBuf buffer) {
                return TileFilters.load(buffer.readNbt());
            }
            
        }, TileFilters.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<ActionMessage>() {
            
            @Override
            protected void writeContent(ActionMessage content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.text);
                buffer.writeInt(content.objects.length);
                for (int i = 0; i < content.objects.length; i++) {
                    ActionMessageObjectType type = ActionMessage.getType(content.objects[i]);
                    buffer.writeInt(type.index());
                    type.write(content.objects[i], buffer);
                }
            }
            
            @Override
            protected ActionMessage readContent(FriendlyByteBuf buffer) {
                String text = buffer.readUtf();
                Object[] objects = new Object[buffer.readInt()];
                for (int i = 0; i < objects.length; i++) {
                    ActionMessageObjectType type = ActionMessage.getType(buffer.readInt());
                    objects[i] = type.read(buffer);
                }
                return new ActionMessage(text, objects);
            }
            
        }, ActionMessage.class);
        
        NetworkFieldTypes.register(new NetworkFieldTypeClass<PlacementPreview>() {
            
            @Override
            protected void writeContent(PlacementPreview content, FriendlyByteBuf buffer) {
                if (content.levelUUID != null) {
                    buffer.writeBoolean(true);
                    buffer.writeUUID(content.levelUUID);
                } else
                    buffer.writeBoolean(false);
                
                NetworkFieldTypes.write(LittleGroup.class, content.previews, buffer);
                NetworkFieldTypes.write(PlacementMode.class, content.mode, buffer);
                NetworkFieldTypes.write(PlacementPosition.class, content.position, buffer);
            }
            
            @Override
            protected PlacementPreview readContent(FriendlyByteBuf buffer) {
                return new PlacementPreview(buffer.readBoolean() ? buffer.readUUID() : null, NetworkFieldTypes.read(LittleGroup.class, buffer), NetworkFieldTypes
                        .read(PlacementMode.class, buffer), NetworkFieldTypes.read(PlacementPosition.class, buffer));
            }
            
        }, PlacementPreview.class);
    }
}
