# 🔮 CloneSMP

![Version](https://img.shields.io/badge/version-1.1.0-brightgreen)
![Bukkit API](https://img.shields.io/badge/bukkit--api-1.19-blue)
[![Download](https://img.shields.io/badge/download-CurseForge-orange)](https://www.curseforge.com/minecraft/bukkit-plugins/clone)

<p align="center">
  <img src="https://media.forgecdn.net/avatars/thumbnails/1232/746/64/64/638808261954412084.png" alt="CloneSMP Logo" width="200" height="200">
</p>

## 📝 Overview

CloneSMP is a survival plugin designed to enhance hardcore gameplay on SMP (Survival Multiplayer) servers. The plugin implements a limited lives system where players are banned after running out of lives, with several mechanics for revival and redemption.

**Features:**
- Custom lives system with configurable maximum
- Player heads drop on death
- Automatic banning after using all lives
- Mega Head crafting for unbanning players
- Life Crystal system for restoring lost lives

## ⚙️ Installation

1. Download the latest version from [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/clone)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Edit the `config.yml` file to customize settings (optional)
5. Reload the plugin with `/clonesmp reload`

## 🧩 Features

### Lives System

Every player starts with a configurable number of lives (default: 3). When a player dies, they lose one life. If a player loses all their lives, they are automatically banned from the server.

```
Lives remaining: 0/3
```

### Player Heads

When players die, they drop their head as an item. These heads can be used as crafting ingredients for special items like the Mega Head and Life Crystal.

### Mega Head

Teammates can craft a Mega Head to unban a player:

1. Collect 3 heads of the banned player
2. Place all 3 heads in the top row of a crafting table
3. Break the placed Mega Head with an Unbreaking III pickaxe to unban the player

### Life Crystal

Players can craft Life Crystals to restore a lost life:

1. Place a player head in the center of a crafting table
2. Surround it with 8 end crystals
3. Right-click with the Life Crystal to gain an extra life
4. Can only be used when at 1/3 or 2/3 of maximum lives

<!-- <p align="center">
  <img src="https://i.imgur.com/xK3D1gp.png" alt="Life Crystal Recipe" width="300">
</p> -->

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/clonesmp lives [player]` | Check remaining lives | clonesmp.command |
| `/clonesmp reset <player>` | Reset a player's lives | clonesmp.admin.reset |
| `/clonesmp reload` | Reload plugin configuration | clonesmp.admin.reload |
| `/clonesmp debug` | Send debug messages to console | clonesmp.admin.debug |
| `/clonesmp help` | Show command help | clonesmp.command |

## ⚙️ Configuration

```yaml
# Maximum number of lives before a player is banned
max-lives: 3

# Whether to broadcast death messages to all players
broadcast-deaths: true

# Whether to drop player heads on death
drop-head-on-death: true

# Maximum number of lives a player can have (with Life Crystals)
max-life-crystals: 5

# Life Crystal Settings
life-crystal:
  # Whether Life Crystals are enabled
  enabled: true
```

## 🛣️ Roadmap

Future features planned for CloneSMP:

- **Team System**: Create teams with shared lives and territory protection
- **Custom Death Messages**: Unique messages based on death type and remaining lives
- **Life Trading**: Allow players to give lives to each other
- **Challenge System**: Complete challenges to earn extra lives
- **Scoreboard Integration**: Display lives on the server scoreboard
- **Ban Appeal System**: Let banned players complete challenges to rejoin

## 🤝 Contributing

Contributions are welcome! If you have ideas for improvements or find any bugs, please open an issue or submit a pull request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📜 License

Distributed under the Custom License. See `LICENSE` for more information.

## 📞 Contact

Project Link: [https://github.com/LiypMC/Clonesmp](https://github.com/LiypMC/Clonesmp)

CurseForge: [https://www.curseforge.com/minecraft/bukkit-plugins/clone](https://www.curseforge.com/minecraft/bukkit-plugins/clone)