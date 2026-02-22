package com.lostpizzaman.energy.energy;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lostpizzaman.energy.Main;

import java.util.ArrayList;
import java.util.List;

public class EnergySystem {

    public static class Initializer extends RefSystem {
        private static final HytaleLogger LOGGER = HytaleLogger.get("EnergySystemInitializer");

        @Override
        public void onEntityAdded(@Nonnull Ref ref, @Nonnull AddReason reason, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
            BlockModule.BlockStateInfo info = (BlockModule.BlockStateInfo) commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (info == null)
                return;

            EnergyComponent energy = (EnergyComponent) commandBuffer.getComponent(ref, Main.get().getEnergyComponentType());
            if (energy != null && (energy.getType().isProducer() || energy.getType().isConsumer())) {
                int x = ChunkUtil.xFromBlockInColumn(info.getIndex());
                int y = ChunkUtil.yFromBlockInColumn(info.getIndex());
                int z = ChunkUtil.zFromBlockInColumn(info.getIndex());

                WorldChunk worldChunk = (WorldChunk) commandBuffer.getComponent(info.getChunkRef(), WorldChunk.getComponentType());
                if (worldChunk != null) {
                    LOGGER.atInfo().log("Initializing energy component at %d, %d, %d. Marking as ticking.", x, y, z);
                    worldChunk.setTicking(x, y, z, true);
                }
            }
        }

        @Override
        public void onEntityRemove(@Nonnull Ref ref, @Nonnull RemoveReason reason, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        }

        @Override
        public Query getQuery() {
            return Query.and(BlockModule.BlockStateInfo.getComponentType(), Main.get().getEnergyComponentType());
        }
    }

    public static class Ticking extends EntityTickingSystem {
        private static final Query QUERY = Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk archetypeChunk, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
            BlockSection blocks = (BlockSection) archetypeChunk.getComponent(index, BlockSection.getComponentType());
            if (blocks == null || blocks.getTickingBlocksCountCopy() == 0)
                return;

            ChunkSection section = (ChunkSection) archetypeChunk.getComponent(index, ChunkSection.getComponentType());
            if (section == null || section.getChunkColumnReference() == null || !section.getChunkColumnReference().isValid())
                return;

            BlockComponentChunk blockComponentChunk = (BlockComponentChunk) commandBuffer.getComponent(section.getChunkColumnReference(), BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null)
                return;

            BlockChunk blockChunk = (BlockChunk) commandBuffer.getComponent(section.getChunkColumnReference(), BlockChunk.getComponentType());
            if (blockChunk == null)
                return;

            blocks.forEachTicking(blockComponentChunk, commandBuffer, section.getY(), (blockComponentChunk1, commandBuffer1, localX, localY, localZ, blockId) -> {
                Ref<ChunkStore> blockRef = blockComponentChunk1.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
                if (blockRef == null)
                    return BlockTickStrategy.IGNORED;

                EnergyComponent energy = (EnergyComponent) commandBuffer1.getComponent(blockRef, Main.get().getEnergyComponentType());
                if (energy != null) {
                    boolean handled = false;

                    if (energy.getType().isConsumer()) {
                        processConsumerTick(energy);
                        handled = true;
                    }

                    if (energy.getType().isProducer()) {
                        int worldX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), localX);
                        int worldZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), localZ);
                        processProducerTick(energy, new Vector3i(worldX, localY, worldZ));
                        handled = true;
                    }

                    if (handled) {
                        return BlockTickStrategy.CONTINUE;
                    }
                }

                return BlockTickStrategy.IGNORED;
            });
        }

        private static void processConsumerTick(EnergyComponent energy) {
            long required = energy.getConsumptionRate();
            if (required > 0) {
                if (energy.extract(required, true) < required) {
                    return;
                }

                energy.extract(required, false);
            }
        }

        private static void processProducerTick(EnergyComponent energy, Vector3i pos) {
            long generated = energy.getGenerationRate();
            if (generated > 0) {
                energy.insert(generated, false);
            }
            if (energy.getStored() > 0) {
                pushToNetwork(pos, energy);
            }
        }

        private static void pushToNetwork(Vector3i currentPos, EnergyComponent source) {
            Vector3i[] neighbors = new Vector3i[]{
                    new Vector3i(currentPos.x + 1, currentPos.y, currentPos.z),
                    new Vector3i(currentPos.x - 1, currentPos.y, currentPos.z),
                    new Vector3i(currentPos.x, currentPos.y + 1, currentPos.z),
                    new Vector3i(currentPos.x, currentPos.y - 1, currentPos.z),
                    new Vector3i(currentPos.x, currentPos.y, currentPos.z + 1),
                    new Vector3i(currentPos.x, currentPos.y, currentPos.z - 1)
            };

            List<EnergyNetwork> validNetworks = new ArrayList<>();
            for (Vector3i neighborPos : neighbors) {
                EnergyNetwork net = EnergyNetworkManager.get().getNetworkAt(neighborPos);
                if (net != null && !validNetworks.contains(net)) {
                    validNetworks.add(net);
                }
            }

            if (validNetworks.isEmpty()) {
                return;
            }

            long remainingToPush = source.extract(source.getMaxOutput(), true);
            if (remainingToPush <= 0) {
                return;
            }

            int remainingNetworks = validNetworks.size();
            for (EnergyNetwork net : validNetworks) {
                if (remainingToPush <= 0) break;
                
                long offer = (remainingToPush + remainingNetworks - 1) / remainingNetworks;
                long accepted = net.insert(offer, false);
                source.extract(accepted, false);
                
                remainingToPush -= accepted;
                remainingNetworks--;
            }
        }

        @Nullable
        @Override
        public Query getQuery() {
            return QUERY;
        }
    }
}
