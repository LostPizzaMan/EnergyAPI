package com.lostpizzaman.energy.util;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;
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
        Ref entityRef = interactionContext.getEntity();
        CommandBuffer commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) return;

        PlayerRef playerRef = (PlayerRef) commandBuffer.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = ((EntityStore) commandBuffer.getExternalData()).getWorld();

        Vector3i targetPos = TargetUtil.getTargetBlock(entityRef, 8.0, commandBuffer);
        if (targetPos == null) return;

        EnergyComponent energy = (EnergyComponent) BlockUtils.getBlockComponentAt(world, targetPos, Main.get().getEnergyComponentType());
        if (energy != null) {
            playerRef.sendMessage(Message.raw("Targeted block energy: " + energy.getStored() + " / " + energy.getCapacity()));
        }

        CableComponent cable = (CableComponent) BlockUtils.getBlockComponentAt(world, targetPos, Main.get().getCableComponentType());
        if (cable != null) {
            EnergyNetwork net = EnergyNetworkManager.get().getNetworkAt(targetPos);
            playerRef.sendMessage(Message.raw("Targeted cable network: " + net.getId()));
        }
    }
}
