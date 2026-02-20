package com.lostpizzaman.energy.energy;

public enum EnergyType {
    NONE,
    PRODUCER,
    CONSUMER,
    BOTH;

    public boolean isProducer() {
        return this == PRODUCER || this == BOTH;
    }

    public boolean isConsumer() {
        return this == CONSUMER || this == BOTH;
    }
}
