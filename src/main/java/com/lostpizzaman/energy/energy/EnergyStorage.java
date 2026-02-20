package com.lostpizzaman.energy.energy;

public interface EnergyStorage {
    long getStored();
    long getCapacity();
    long getMaxInput();
    long getMaxOutput();

    /**
     * @param amount Amount to insert.
     * @param simulate If true, only calculate what would happen.
     * @return Amount actually inserted.
     */
    long insert(long amount, boolean simulate);

    /**
     * @param amount Amount to extract.
     * @param simulate If true, only calculate what would happen.
     * @return Amount actually extracted.
     */
    long extract(long amount, boolean simulate);
}