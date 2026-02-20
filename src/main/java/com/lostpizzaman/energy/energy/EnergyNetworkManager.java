package com.lostpizzaman.energy.energy;

import com.hypixel.hytale.math.vector.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyNetworkManager {
    private static final EnergyNetworkManager INSTANCE = new EnergyNetworkManager();

    private final Map<UUID, EnergyNetwork> networks = new ConcurrentHashMap<>();
    private final Map<Vector3i, UUID> cableToNetworkId = new ConcurrentHashMap<>();

    private EnergyNetworkManager() {
    }

    public static EnergyNetworkManager get() {
        return INSTANCE;
    }

    public EnergyNetwork getNetwork(UUID id) {
        return networks.get(id);
    }

    public EnergyNetwork getNetworkAt(Vector3i pos) {
        UUID id = cableToNetworkId.get(pos);
        return id != null ? networks.get(id) : null;
    }

    public EnergyNetwork createNetwork() {
        EnergyNetwork net = new EnergyNetwork();
        networks.put(net.getId(), net);
        return net;
    }

    public void registerCable(Vector3i pos, EnergyNetwork network) {
        cableToNetworkId.put(pos, network.getId());
        network.addCable(pos);
    }

    public void unregisterCable(Vector3i pos) {
        cableToNetworkId.remove(pos);
    }

    public void purgeNetwork(UUID networkId) {
        EnergyNetwork net = networks.remove(networkId);
        if (net != null) {
            for (Vector3i pos : net.getCables()) {
                cableToNetworkId.remove(pos);
            }
        }
    }

    public void tickAll(Context context) {
        for (EnergyNetwork net : networks.values()) {
            net.tick(context);
        }
    }

    public interface Context {
        long offerEnergy(Vector3i pos, long amount);
    }
}
