package com.lostpizzaman.energy.energy;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lostpizzaman.energy.Main;

public class EnergyNetworkTickingSystem extends TickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();

        EnergyNetworkManager.Context context = (pos, amount) -> insertIntoMachine(world, pos, amount);
        EnergyNetworkManager.get().tickAll(context);
    }

    private long insertIntoMachine(World world, Vector3i pos, long amount) {
        try {
            WorldChunk chunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
            if (chunk == null)
                return 0;

            Ref<ChunkStore> blockRef = chunk.getBlockComponentChunk().getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
            if (blockRef == null)
                return 0;

            EnergyComponent energy = (EnergyComponent) world.getChunkStore().getStore().getComponent(blockRef, Main.get().getEnergyComponentType());
            if (energy != null) {
                return energy.insert(amount, false);
            }
        } catch (Exception e) {
            // log later
        }
        return 0;
    }
}
