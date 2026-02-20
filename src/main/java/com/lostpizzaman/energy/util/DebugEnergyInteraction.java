package com.lostpizzaman.energy.util;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.lostpizzaman.energy.Main;
import com.lostpizzaman.energy.cable.CableComponent;
import com.lostpizzaman.energy.energy.EnergyComponent;
import com.lostpizzaman.energy.energy.EnergyNetwork;
import com.lostpizzaman.energy.energy.EnergyNetworkManager;

import javax.annotation.Nonnull;

public class DebugEnergyInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<DebugEnergyInteraction> CODEC = BuilderCodec.builder(DebugEnergyInteraction.class, DebugEnergyInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        var playerRef = interactionContext.getEntity();
        var store = playerRef.getStore();

        var player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        var world = player.getWorld();
        if (world == null) return;

        Vector3i targetPos = TargetUtil.getTargetBlock(playerRef, 8.0, store);
        if (targetPos == null) return;

        EnergyComponent energy = (EnergyComponent) BlockUtils.getBlockComponentAt(world, targetPos, Main.get().getEnergyComponentType());
        if (energy != null) {
            player.sendMessage(Message.raw("Targeted block energy: " + energy.getStored() + " / " + energy.getCapacity()));
        }

        CableComponent cable = (CableComponent) BlockUtils.getBlockComponentAt(world, targetPos, Main.get().getCableComponentType());
        if (cable != null) {
            EnergyNetwork net = EnergyNetworkManager.get().getNetworkAt(targetPos);
            player.sendMessage(Message.raw("Targeted cable network: " + net.getId()));
        }
    }
}
