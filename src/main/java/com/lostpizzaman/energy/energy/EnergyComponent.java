package com.lostpizzaman.energy.energy;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;

public class EnergyComponent implements Component, EnergyStorage {
    public static final BuilderCodec CODEC;

    static {
        BuilderCodec.Builder<EnergyComponent> builder = BuilderCodec.builder(EnergyComponent.class, EnergyComponent::new);
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec("Stored", Codec.LONG), (c, v) -> c.stored = v, (c) -> c.stored).add();
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec("Capacity", Codec.LONG), (c, v) -> c.capacity = v, (c) -> c.capacity).add();
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec("MaxInput", Codec.LONG), (c, v) -> c.maxInput = v, (c) -> c.maxInput).add();
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec("MaxOutput", Codec.LONG), (c, v) -> c.maxOutput = v, (c) -> c.maxOutput).add();
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec<>("Type", new EnumCodec(EnergyType.class)), (c, v) -> c.type = (EnergyType) v, (c) -> c.type).add();
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec("GenerationRate", Codec.LONG), (c, v) -> c.generationRate = v, (c) -> c.generationRate).add();
        builder = (BuilderCodec.Builder<EnergyComponent>) builder.append(new KeyedCodec("ConsumptionRate", Codec.LONG), (c, v) -> c.consumptionRate = v, (c) -> c.consumptionRate).add();
        CODEC = builder.build();
    }

    private long stored;
    private long capacity;
    private long maxInput;
    private long maxOutput;
    private long generationRate;
    private long consumptionRate;
    private EnergyType type = EnergyType.NONE;

    public EnergyComponent() {
        this(0, 10000, 1000, 1000, 0, 0, EnergyType.NONE);
    }

    public EnergyComponent(long capacity, long maxInOut, EnergyType type) {
        this(0, capacity, maxInOut, maxInOut, 0, 0, type);
    }

    public EnergyComponent(long capacity, long maxInOut, long generationRate, long consumptionRate, EnergyType type) {
        this(0, capacity, maxInOut, maxInOut, generationRate, consumptionRate, type);
    }

    public EnergyComponent(long stored, long capacity, long maxInput, long maxOutput, long generationRate, long consumptionRate, EnergyType type) {
        this.stored = stored;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.generationRate = generationRate;
        this.consumptionRate = consumptionRate;
        this.type = type;
    }

    public long getGenerationRate() {
        return generationRate;
    }

    public void setGenerationRate(long generationRate) {
        this.generationRate = generationRate;
    }

    public long getConsumptionRate() {
        return consumptionRate;
    }

    public void setConsumptionRate(long consumptionRate) {
        this.consumptionRate = consumptionRate;
    }

    public EnergyType getType() {
        return type;
    }

    public void setType(EnergyType type) {
        this.type = type;
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getMaxInput() {
        return maxInput;
    }

    @Override
    public long getMaxOutput() {
        return maxOutput;
    }

    @Override
    public long insert(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long accepted = Math.min(maxInput, Math.min(amount, capacity - stored));
        if (!simulate) {
            stored += accepted;
        }
        return accepted;
    }

    @Override
    public long extract(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long extracted = Math.min(maxOutput, Math.min(amount, stored));
        if (!simulate) {
            stored -= extracted;
        }
        return extracted;
    }

    public void setStored(long stored) {
        this.stored = Math.min(stored, capacity);
    }

    @Override
    public Component clone() {
        return new EnergyComponent(stored, capacity, maxInput, maxOutput, generationRate, consumptionRate, type);
    }
}
