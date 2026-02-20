package com.lostpizzaman.energy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.lostpizzaman.energy.cable.CableComponent;
import com.lostpizzaman.energy.cable.CableInitializer;
import com.lostpizzaman.energy.energy.EnergyComponent;
import com.lostpizzaman.energy.example.DemoEnergySystem;
import com.lostpizzaman.energy.energy.EnergyNetworkFormationSystem;
import com.lostpizzaman.energy.energy.EnergyNetworkTickingSystem;
import com.lostpizzaman.energy.util.DebugEnergyInteraction;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {
    private static Main instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ComponentType cableComponentType;
    private ComponentType energyComponentType;

    public static Main get() {
        return instance;
    }

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        instance = this;
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        this.getCodecRegistry(Interaction.CODEC).register("DebuggerEnergyInteraction", DebugEnergyInteraction.class, DebugEnergyInteraction.CODEC);

        this.energyComponentType = this.getChunkStoreRegistry().registerComponent(EnergyComponent.class, "EnergyComponent", EnergyComponent.CODEC);
        this.cableComponentType = this.getChunkStoreRegistry().registerComponent(CableComponent.class, "CableComponent", CableComponent.CODEC);

        this.getChunkStoreRegistry().registerSystem(new CableInitializer());
        this.getChunkStoreRegistry().registerSystem(new DemoEnergySystem.Initializer());
        this.getChunkStoreRegistry().registerSystem(new DemoEnergySystem.Ticking());

        this.getEntityStoreRegistry().registerSystem(new EnergyNetworkTickingSystem());
        this.getEntityStoreRegistry().registerSystem(new EnergyNetworkFormationSystem.BreakListener());
        this.getEntityStoreRegistry().registerSystem(new EnergyNetworkFormationSystem.PlaceListener());
    }


    public ComponentType getCableComponentType() {
        return cableComponentType;
    }

    public ComponentType getEnergyComponentType() {
        return energyComponentType;
    }
}
