![](https://github.com/MineInAbyss/DeeperWorld/workflows/Java%20CI/badge.svg)

# DeeperWorld

DeeperWorld is a plugin used on the Mine in Abyss server to create the illusion of a world taller than Minecraft's 256 block limit. We achieve this by dividing our deep world into `sections`, which have an overlap of identical blocks from the bottom of one section, and top of the next.
The plugin relatively teleports players between these sections when reaching the top or bottom. 
# Features

## Syncing

Additionally, we must sync overlaps by listening to many Bukkit events.

#### Currently we support some of the following syncing features:
- Block place/break
- Chests and other inventories, by directly accessing the higher layer's inventory, preventing dupe exploits
- Crop growth

#### Notable features that don't get synced on overlaps:
- Water flow
- Shulkers
- Pistons (they will not push/pull blocks on intersections in order to prevent dupe exploits)
- Entities (would be possible with fancy packet manipulation, but not currently planned)

## Other features

The config has some additional features like damaging players outside managed sections.

# Setup

Please read the [project wiki](https://github.com/MineInAbyss/DeeperWorld/wiki) for more details on setup and configuration.

# Contributing

Please contact us in `#plugin-dev` on [Discord](https://discord.gg/QXPCk2y) if you'd like to help out. We are open to new features and contributions!
