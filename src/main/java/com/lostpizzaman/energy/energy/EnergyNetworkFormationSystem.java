package com.lostpizzaman.energy.energy;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.lostpizzaman.energy.Main;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class EnergyNetworkFormationSystem {

    public static class PlaceListener extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public PlaceListener() {
            super(PlaceBlockEvent.class);
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }

        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent event) {
            Vector3i pos = event.getTargetBlock();

            World world = commandBuffer.getExternalData().getWorld();
            world.execute(() -> {
                if (isCable(world, pos)) {
                    handleCablePlaced(world, pos);
                } else if (isAcceptor(world, pos)) {
                    handleAcceptorPlaced(world, pos);
                }
            });
        }
    }

    public static class BreakListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public BreakListener() {
            super(BreakBlockEvent.class);
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }

        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {
            Vector3i pos = event.getTargetBlock();

            EnergyNetwork net = EnergyNetworkManager.get().getNetworkAt(pos);
            if (net != null) {
                Set<Vector3i> allCables = new HashSet<>(net.getCables());
                allCables.remove(pos);

                EnergyNetworkManager.get().purgeNetwork(net.getId());

                World world = commandBuffer.getExternalData().getWorld();
                world.execute(() -> {
                    for (Vector3i orphan : allCables) {
                        if (EnergyNetworkManager.get().getNetworkAt(orphan) == null) {
                            rebuildNetworkFrom(world, orphan);
                        }
                    }
                });
            } else {
                handleAcceptorRemoved(pos);
            }
        }

        private void handleAcceptorRemoved(Vector3i pos) {
            for (Vector3i neighbor : getNeighbors(pos)) {
                EnergyNetwork net = EnergyNetworkManager.get().getNetworkAt(neighbor);
                if (net != null && net.getAcceptors().contains(pos)) {
                    net.removeAcceptor(pos);
                }
            }
        }
    }

    public static void rebuildNetworkFrom(World world, Vector3i startPos) {
        EnergyNetwork newNet = EnergyNetworkManager.get().createNetwork();
        java.util.Queue<Vector3i> queue = new java.util.LinkedList<>();
        Set<Vector3i> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            Vector3i curr = queue.poll();
            EnergyNetworkManager.get().registerCable(curr, newNet);

            for (Vector3i neighbor : getNeighbors(curr)) {
                if (visited.contains(neighbor))
                    continue;

                if (isCable(world, neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else if (isAcceptor(world, neighbor)) {
                    visited.add(neighbor);
                    newNet.addAcceptor(neighbor);
                }
            }
        }
    }

    private static boolean isCable(World world, Vector3i pos) {
        WorldChunk chunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunk == null)
            return false;
        Ref<ChunkStore> ref = chunk.getBlockComponentChunk().getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
        if (ref == null)
            return false;

        return world.getChunkStore().getStore().getComponent(ref, Main.get().getCableComponentType()) != null;
    }

    private static boolean isAcceptor(World world, Vector3i pos) {
        WorldChunk chunk = world.getChunkIfLoaded(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunk == null)
            return false;
        Ref<ChunkStore> ref = chunk.getBlockComponentChunk().getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
        if (ref == null)
            return false;

        EnergyComponent energy = (EnergyComponent) world.getChunkStore().getStore().getComponent(ref, Main.get().getEnergyComponentType());
        return energy != null && energy.getType().isConsumer();
    }

    public static void handleCablePlaced(World world, Vector3i pos) {
        EnergyNetworkManager manager = EnergyNetworkManager.get();
        Set<EnergyNetwork> neighborNetworks = new HashSet<>();

        Vector3i[] neighbors = getNeighbors(pos);
        for (Vector3i n : neighbors) {
            EnergyNetwork net = manager.getNetworkAt(n);
            if (net != null) {
                neighborNetworks.add(net);
            }
        }

        EnergyNetwork finalNetwork;
        if (neighborNetworks.isEmpty()) {
            finalNetwork = manager.createNetwork();
        } else {
            finalNetwork = neighborNetworks.iterator().next();
            for (EnergyNetwork other : neighborNetworks) {
                if (other != finalNetwork) {

                    Set<Vector3i> cablesToMove = new HashSet<>(other.getCables());
                    finalNetwork.merge(other);

                    for (Vector3i c : cablesToMove) {
                        manager.registerCable(c, finalNetwork);
                    }

                    manager.purgeNetwork(other.getId());
                }
            }
        }

        manager.registerCable(pos, finalNetwork);
        scanNeighborsForAcceptors(world, pos, finalNetwork);
    }

    private static void handleAcceptorPlaced(World world, Vector3i pos) {
        for (Vector3i n : getNeighbors(pos)) {
            EnergyNetwork net = EnergyNetworkManager.get().getNetworkAt(n);
            if (net != null) {
                net.addAcceptor(pos);
            }
        }
    }

    private static void scanNeighborsForAcceptors(World world, Vector3i pos, EnergyNetwork network) {
        for (Vector3i n : getNeighbors(pos)) {
            if (isAcceptor(world, n)) {
                network.addAcceptor(n);
            }
        }
    }

    private static Vector3i[] getNeighbors(Vector3i pos) {
        return new Vector3i[]{
                new Vector3i(pos.x + 1, pos.y, pos.z),
                new Vector3i(pos.x - 1, pos.y, pos.z),
                new Vector3i(pos.x, pos.y + 1, pos.z),
                new Vector3i(pos.x, pos.y - 1, pos.z),
                new Vector3i(pos.x, pos.y, pos.z + 1),
                new Vector3i(pos.x, pos.y, pos.z - 1)
        };
    }
}
