package com.lostpizzaman.energy.cable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;

public class CableComponent implements Component {
    public static final BuilderCodec CODEC;
    public Boolean enabled;

    static {
        CODEC = BuilderCodec.builder(CableComponent.class, CableComponent::new)
                .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), (c, v) -> c.enabled = v, (c) -> c.enabled).add()
                .build();
    }

    public CableComponent() {
    }

    @Override
    public Component clone() {
        return new CableComponent();
    }
}
