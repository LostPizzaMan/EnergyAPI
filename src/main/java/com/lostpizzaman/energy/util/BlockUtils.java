package com.lostpizzaman.energy.util;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class BlockUtils {
    public static Ref<ChunkStore> getBlockEntityRefAt(World world, Vector3i position) {
        WorldChunk worldChunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(position.x, position.z));
        if (worldChunk == null)
            return null;

        BlockComponentChunk blockComponentChunk = worldChunk.getBlockComponentChunk();
        if (blockComponentChunk == null)
            return null;

        return blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(position.x, position.y, position.z));
    }

    public static <T extends Component<ChunkStore>> T getBlockComponentAt(World world, Vector3i position, ComponentType<ChunkStore, T> componentType) {
        Ref<ChunkStore> blockRef = getBlockEntityRefAt(world, position);
        if (blockRef == null) {
            return null;
        }
        Store<ChunkStore> store = blockRef.getStore();
        return store.getComponent(blockRef, componentType);
    }
}
