# Energy API for Hytale
A lightweight, component-based energy API designed for Hytale mod developers.

## Features
- **Energy Component**: Manage energy capacity, generation rates, consumption rates, and IO limits in a single configurable component.
- **Energy Networks**: Automatically links adjacent energy blocks into unified networks.
- **Role Types**: Assign `PRODUCER`, `CONSUMER`, or `BOTH` using `EnergyType`.

## Usage

You can attach an `EnergyComponent` to any block to give it energy properties. 

### For Producers ⚡
To create an energy-generating block (like a generator or solar panel):
1. Assign an `EnergyComponent` to your block with the `EnergyType` set to `PRODUCER`.
2. Set the `GenerationRate` (target energy produced per tick).
3. The system will automatically generate energy up to the block's internal buffer capacity, and push it to the network.

<details>
<summary><b>IO Limits</b> (Click to expand)</summary>

- `MaxInput`: Limit how much of the `GenerationRate` enters the internal buffer per tick.
- `MaxOutput`: Limit how much energy leaves the internal buffer to enter the network per tick.
</details>

### For Consumers 🔌
To create an energy-consuming block (like a machine):
1. Assign an `EnergyComponent` to your block with the `EnergyType` set to `CONSUMER`.
2. Set the `ConsumptionRate` (target energy consumed per tick).
3. The system will automatically satisfy the required consumption amount from the block's internal buffer.

<details>
<summary><b>IO Limits</b> (Click to expand)</summary>

- `MaxInput`: Limit how much energy enters the internal buffer from the network per tick.
- `MaxOutput`: Limit how much energy leaves the internal buffer to fulfill the `ConsumptionRate` per tick.
</details>

### For Batteries 🔋
To create an energy-storing block (like a battery):
1. Assign an `EnergyComponent` to your block with the `EnergyType` set to `BOTH`.
2. Ensure both `GenerationRate` and `ConsumptionRate` are left at `0` (this is the default).
3. Configure the `Capacity`, `MaxInput`, and `MaxOutput` properties to limit transfer rates and define storage limits.
4. The system will naturally buffer energy, accepting it from producers and providing it to consumers on the network.

### Dynamic Adjustments
You can dynamically adjust power usage and production on the fly to match your machine's current state. For example, you can drop a machine's `ConsumptionRate` to `0` when it finishes processing an item, or increase a generator's `GenerationRate` only when it has enough coal to burn! 

Just grab the `EnergyComponent` from the block and update the rates in your own logic systems:

```java
EnergyComponent energy = commandBuffer.getComponent(blockRef, Main.get().getEnergyComponentType());
if (energy != null) {
    energy.setConsumptionRate(newConsumptionRate);
    energy.setGenerationRate(newGenerationRate);
}
```

### JSON Examples
*You can find full JSON block configuration examples for a Producer, Consumer, Cable, and Battery in `src/main/resources/Server/Item/Items/`.*
