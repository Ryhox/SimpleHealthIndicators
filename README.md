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
/healthbar <bar|hearts|numeric>
/healthbar offset <0..5>
```

## Compatibility

- Minecraft **26.3**
- Java **25+**
- Fabric Loader **0.19.5+**
- Fabric API **0.161.0+26.3** tested
- Mod Menu **21.0.0** optional

## Building and testing

```sh
./gradlew build
./gradlew runClientGameTest
```

The client game test launches Minecraft 26.3 and captures bars, hearts, numeric
values, player poison/wither/absorption effects, transparency, and the
configuration screen. Screenshots are saved under
`build/run/clientGameTest/screenshots/`; test code is excluded from release jars.
Use `-PcompatModJar=/absolute/path/to/other-mod.jar` for a combined mod test.

Health and status displays use information supplied by the server. Vanilla does
not synchronize detailed effects and absorption for every remote mob/player.
