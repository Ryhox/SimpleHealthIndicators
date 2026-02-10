# Simple Health Indicators

A lightweight **Minecraft client-side mod** that shows entity health in a clean and customizable way.

You can choose between:
- A **custom textured health bar**
- **Vanilla-style hearts** (fully compatible with texture packs)
- A **numeric health indicator**

All modes render **above entities** and scale nicely with distance.

## Features

- Multiple health display modes
  - Bar
  - Hearts
  - Numbers
- Custom health bar textures
- Heart mode supports:
  - Hardcore hearts
  - Poison / Wither
  - Absorption hearts
  - Resource packs
- Client-side only
- Smooth rendering using Minecraft’s render pipeline
- Lightweight and performance friendly

## Health Display Modes

### Bar
- Uses a **custom texture**
- Clean and minimal
- Supports partial filling and absorption

### Hearts
- Uses **vanilla heart sprites**
- Fully compatible with texture packs
- Correctly displays:
  - Half hearts
  - Absorption hearts
  - Poisoned / Withered variants
  - Hardcore mode textures

### Numeric
- Displays exact values (e.g. `18.50/20.00 ♥`)
- Color adapts to status effects
- Includes absorption health

## Configuration

You can change the health indicator mode in multiple ways:

- **Mod Menu integration**
- **In-game command**

```text
/healthbar <type>
