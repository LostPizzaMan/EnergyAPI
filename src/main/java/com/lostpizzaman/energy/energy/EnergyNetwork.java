package com.lostpizzaman.energy.energy;

import com.hypixel.hytale.math.vector.Vector3i;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EnergyNetwork {
    private final UUID id = UUID.randomUUID();
    private final Set<Vector3i> cables = new HashSet<>();
    private final Set<Vector3i> acceptors = new HashSet<>();

    private long storedEnergy = 0;
    private long capacity = 0;

    private static final long CABLE_CAPACITY = 1000;

    public EnergyNetwork() {
    }

    public UUID getId() {
        return id;
    }

    public void addCable(Vector3i pos) {
        if (cables.add(pos)) {
            capacity += CABLE_CAPACITY;
        }
    }

    public void removeCable(Vector3i pos) {
        if (cables.remove(pos)) {
            capacity = Math.max(0, capacity - CABLE_CAPACITY);
            storedEnergy = Math.min(storedEnergy, capacity);
        }
    }

    public void addAcceptor(Vector3i pos) {
        acceptors.add(pos);
    }

    public void removeAcceptor(Vector3i pos) {
        acceptors.remove(pos);
    }

    public Set<Vector3i> getCables() {
        return cables;
    }

    public Set<Vector3i> getAcceptors() {
        return acceptors;
    }

    public long insert(long amount, boolean simulate) {
        long needed = capacity - storedEnergy;
        long toAdd = Math.min(amount, needed);
        if (!simulate) {
            storedEnergy += toAdd;
        }
        return toAdd;
    }

    public long extract(long amount, boolean simulate) {
        long toRemove = Math.min(amount, storedEnergy);
        if (!simulate) {
            storedEnergy -= toRemove;
        }
        return toRemove;
    }

    public long getStored() {
        return storedEnergy;
    }

    public long getCapacity() {
        return capacity;
    }

    public void tick(EnergyNetworkManager.Context context) {
        if (storedEnergy <= 0 || acceptors.isEmpty())
            return;

        long amountPerAcceptor = storedEnergy / acceptors.size();
        if (amountPerAcceptor == 0 && storedEnergy > 0)
            amountPerAcceptor = 1;

        long totalDistributed = 0;

        for (Vector3i pos : acceptors) {
            long remaining = storedEnergy - totalDistributed;
            if (remaining <= 0)
                break;

            long offer = Math.min(remaining, amountPerAcceptor);
            long accepted = context.offerEnergy(pos, offer);
            totalDistributed += accepted;
        }
        extract(totalDistributed, false);
    }

    public void merge(EnergyNetwork other) {
        if (other == null || other == this)
            return;

        this.cables.addAll(other.cables);
        this.acceptors.addAll(other.acceptors);

        this.capacity = this.cables.size() * CABLE_CAPACITY;
        this.storedEnergy = Math.min(this.capacity, this.storedEnergy + other.storedEnergy);

        other.cables.clear();
        other.acceptors.clear();
        other.storedEnergy = 0;
    }
}
