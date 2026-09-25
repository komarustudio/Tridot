![GitHub License](https://img.shields.io/github/license/KomaruPRO/Tridot?style=for-the-badge&labelColor=yellow&color=white)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/KomaruPRO/Tridot/total?style=for-the-badge&logo=github&labelColor=black&color=white)

# What do we offer?
> **Tridot** offers utilities in almost every way needed for a developer;
> - Simplifying your calculations and data storing with custom structures
> - Providing useful rendering methods for "le beauty"
> - Fixing Minecraft Forge's modding experience for more robust additions
    > ...and more!

## For developers
Build locally and publish to your Maven local repository: (Optional)

```bash
./gradlew build publishToMavenLocal
```

Then in your mod's `build.gradle`:
```groovy
repositories {
    mavenLocal() // if you are building locally
    maven { url = "https://repo.komaru.studio/releases" }
}

dependencies {
    runtimeOnly fg.deobf("pro.komaru:Tridot:${minecraft_version}-${tridot_version}")
    compileOnly fg.deobf("pro.komaru:Tridot:${minecraft_version}-${tridot_version}:api")
}
```
---
# Features
### Screenshakes, even as an earthquake
> Uses trigonometry to make it natural and nice-looking
### Component `Style` replacement
> `DotStyle` adds new `DotStyleEffect` class which helps to modify text rendering
- Custom character effects, and has built-in ones.
### `SplashHandler` for adding custom splashes
> Add new customizable splashes to title screen using this class
- Language-specific splashes
- Translatable splashes
- Weight for controlling chance of a splash appear
### `Item` and `Armor` skins system
> Change Items' and Armor' models with a modifiable skin system
### Music Modifiers
> Changes music depending on some factors
- Biome-specific music
- Dungeon(structure)-specific music
### Attribute names modifiers
### Armor with percent-based protection
> Adds a new defense property for armor, which reduces taken damage in percents
- Removes vanilla 80% cap so armor can provide up to 100% protection
### Flexible, builder-based systems
> Robust modding with new systems will be even easier
- Armor Builders
- Command Builder
- Particle Builders
- Rendering Builders
- - cubes, beams, vertices, etc.
---
