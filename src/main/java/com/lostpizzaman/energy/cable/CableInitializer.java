package com.lostpizzaman.energy.cable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import javax.annotation.Nonnull;
import com.lostpizzaman.energy.Main;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lostpizzaman.energy.energy.EnergyNetworkManager;
import com.lostpizzaman.energy.energy.EnergyNetworkFormationSystem;

public class CableInitializer extends RefSystem {
    private static final Query QUERY = Query.and(BlockModule.BlockStateInfo.getComponentType(), Main.get().getCableComponentType());

    @Override
    public void onEntityAdded(@Nonnull Ref ref, @Nonnull AddReason reason, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        BlockModule.BlockStateInfo info = (BlockModule.BlockStateInfo) commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
        if (info == null)
            return;

        int x = ChunkUtil.xFromBlockInColumn(info.getIndex());
        int y = ChunkUtil.yFromBlockInColumn(info.getIndex());
        int z = ChunkUtil.zFromBlockInColumn(info.getIndex());

        BlockChunk blockChunk = (BlockChunk) commandBuffer.getComponent(info.getChunkRef(), BlockChunk.getComponentType());
        if (blockChunk != null) {
            if (commandBuffer.getComponent(ref, Main.get().getCableComponentType()) != null) {
                World world = ((ChunkStore) commandBuffer.getExternalData()).getWorld();
                int worldX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), x);
                int worldZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), z);
                Vector3i pos = new Vector3i(worldX, y, worldZ);

                world.execute(() -> {
                    if (EnergyNetworkManager.get().getNetworkAt(pos) == null) {
                        EnergyNetworkFormationSystem.rebuildNetworkFrom(world, pos);
                    }
                });
            }
        }
    }

    @Override
    public void onEntityRemove(@Nonnull Ref ref, @Nonnull RemoveReason reason, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
    }

    @Override
    public Query getQuery() {
        return QUERY;
    }
}
