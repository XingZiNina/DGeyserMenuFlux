[English](./README-en.md) | [中文](../zh/README-zh.md)

# 🌟 DGeyserMenuFlux - The Next-Gen Multi-Version Menu Plugin

> 🚀 **A revolutionary Minecraft menu plugin with seamless Java & Bedrock Edition support**

---

## ✨ Core Features

### 🎯 **Game-Changing Multi-Version Support**
- **Java Edition**: Native chest menus with traditional Minecraft UI
- **Bedrock Edition**: Native form menus optimized for mobile & console
- **Smart Detection**: Automatically serves the right menu for each player's version

### ⚡ **Blazing Performance & UX**
- **Lightweight Design**: Core features only, zero performance overhead
- **Hot-Reload Configs**: Changes apply instantly, no server restart needed
- **Massive Capacity**: Supports up to 6 rows (54 slots) for Java menus
- **Seamless Migration**: Perfect compatibility with DeluxeMenus configs

### 🛠 **Developer Friendly**
- **PAPI Support**: Full PlaceholderAPI variable integration
- **Beginner Friendly**: Simple setup, quick to master
- **Rich Examples**: Complete, ready-to-use sample menus included

### 🎁 **Smart Item System**
- **Auto Clock Management**: Detects missing menu clocks in inventory and auto-replaces
- **Death Protection**: Clocks never drop on death
- **Unique Identification**: NBT tags ensure clock uniqueness
- **Quick Access**: Right-click clock to instantly open main menu

---

## 🚀 Quick Start

### Installation Steps
1. Drop the plugin into your `plugins` folder
2. Restart your server
3. Check generated sample menus in `plugins/DGeyserMenuFlux/`
4. Modify configs to fit your needs

### 🕰️ Smart Clock System
Automatically provides every player with a **Menu Clock**:
- ✅ **Auto-Detection**: Checks inventory on login, replaces if missing
- ✅ **Death Protection**: Never drops on death, auto-restores on respawn
- ✅ **Quick Access**: Right-click to instantly open main menu
- ✅ **Permission Control**: Requires `dgeysermenu.use` permission

**Admin Commands**:
```bash
/getmenuclock  # Manually get a menu clock
```

### 📋 Base Commands
```bash
# Open specific menu
/dgeysermenu open <menu-name>

# Reload plugin configs
/dgeysermenu reload [all|java|bedrock]

# List available menus
/dgeysermenu list

# Get menu clock (admin)
/getmenuclock
```

### 📋 Shortcut Commands
```bash
# Open specific menu
/dgm open <menu-name>

# Reload plugin configs
/dgm reload [all|java|bedrock]

# List available menus
/dgm list
```

### 🔐 Permission Nodes
```yaml
# Basic usage permission
dgeysermenu.use

# Admin permissions
dgeysermenu.admin

# Specific menu access
dgeysermenu.menu.<menu-name>

# Reload permission
dgeysermenu.reload
```

---

## 📖 Configuration Guide

### Java Edition Menus (java_menus/)
```yaml
# Basic settings
menu_title: "&6&lMain Menu"  # Menu title (supports color codes)
rows: 6                       # Menu rows (1-6)

# Item configuration example
items:
  info_item:                  # Unique item ID
    slot: 11                  # Slot (0-53) or range "0-8"
    material: PAPER           # Material name
    display_name: "&e&lInfo"  # Display name
    lore:                     # Description text
      - "&7Click to view server info"
      - "&fOnline: &a%server_online%"

    # Command configuration
    left_click_commands:      # Left-click commands
      - "[message] &aWelcome to the menu!"
      - "[player] spawn"
    right_click_commands:     # Right-click commands
      - "[close]"
```

### Bedrock Edition Menus (bedrock_menus/)
```yaml
menu:
  title: "Main Menu"          # Form title
  subtitle: "Welcome"         # Subtitle
  footer: "Server Name"       # Footer text

  items:
    - text: "Server Info"     # Button text
      icon: "textures/items/paper"  # Icon path
      icon_type: "path"       # Icon type (path/url)
      command: "dgeysermenu open info"  # Command to execute
      execute_as: "player"    # Execution identity (player/console)
```

```yaml
# Configuration example - config.yml
settings:
  menu-clock:
    enabled: true             # Enable clock system
    auto-give: true           # Auto-give clocks
    death-protection: true    # Death protection
```

---

## 🎨 Sample Menu Breakdown

### 🏠 Main Menu Configuration

#### Java Edition Main Menu (main.yml)
```yaml
menu_title: "&6&lMain Menu &7| &fServer Name"
rows: 6

items:
  # Server info section
  server_info:
    slot: 10
    material: KNOWLEDGE_BOOK        # Knowledge book material for info
    display_name: "&e&lServer Info"  # Yellow highlight
    lore:                           # Dynamic server data display
      - "&7Click to view server info"
      - ""
      - "&fOnline Players: &a%server_online%&f/&a%server_max_players%"
      - "&fTPS: &a%server_tps%"
    left_click_commands:
      - "[message] &6=== Server Info ==="
      - "[message] &fOnline Players: &a%server_online%&f/&a%server_max_players%"

  # Teleport functionality
  teleport_spawn:
    slot: 12
    material: COMPASS               # Compass material for navigation
    display_name: "&a&lTeleport to Spawn"  # Green for safe teleport
    lore:
      - "&7Click to teleport to spawn"
      - ""
      - "&fInstantly teleport to safe spawn area"
    left_click_commands:
      - "[player] spawn"            # Execute teleport command
      - "[message] &aTeleported to spawn!"  # Feedback message

  # Decorative border system
  border_top:
    slot: 0-8                      # Range slots, top border
    material: BLACK_STAINED_GLASS_PANE  # Black stained glass pane
    display_name: " "               # Empty name, pure decoration
```

#### Bedrock Edition Main Menu (bedrock_main.yml)
```yaml
menu:
  title: "§6§lMain Menu"
  subtitle: "§7Welcome to our server!"
  footer: "§8Server Version 1.21.x"

  items:
    - text: "§e§lServer Info\n§7Click to view server status"
      icon: "textures/items/book_enchanted"  # Enchanted book icon
      icon_type: "path"
      command: "dgeysermenu open info"
  
    - text: "§a§lTeleport\n§7Quick travel to locations"
      icon: "textures/items/compass_item"    # Compass icon
      icon_type: "path"
      command: "dgeysermenu open warps"
```

---

## 🔄 Migrating from DeluxeMenus

### Configuration Comparison
| DeluxeMenus | DGeyserMenuFlux | Notes |
|-------------|-----------------|-------|
| `menu_title` | `menu_title` | Fully compatible |
| `rows` | `rows` | Fully compatible |
| `items.<id>.slot` | `items.<id>.slot` | Fully compatible |
| `items.<id>.material` | `items.<id>.material` | Fully compatible |
| `items.<id>.display_name` | `items.<id>.display_name` | Fully compatible |
| `items.<id>.lore` | `items.<id>.lore` | Fully compatible |
| `items.<id>.left_click_commands` | `items.<id>.left_click_commands` | Fully compatible |
| `items.<id>.right_click_commands` | `items.<id>.right_click_commands` | Fully compatible |

### 📊 Feature Comparison

| Feature | DGeyserMenuFlux | Traditional Menu Plugins |
|---------|-----------------|--------------------------|
| Auto Item Management | ✅ Smart Clock System | ❌ Manual distribution |
| Hot-Reload Support | ✅ Instant apply | ⚠️ Requires reload |
| Multi-Version Native Support | ✅ Java+Bedrock | ❌ Single version only |
| Migration Ease | ✅ Perfect compatibility | ❌ Reconfiguration needed |
| Performance | ✅ Lightweight & efficient | ⚠️ Heavy resource usage |

### Migration Steps
1. **Copy Config Files**: Copy DeluxeMenus YAML files to `java_menus/` folder
2. **Adjust Paths**: Ensure file paths are correct
3. **Test Functionality**: Use `/dgeysermenu open <menu-name>` to test
4. **Bedrock Adaptation**: Create corresponding Bedrock menus as needed

### Migration Example
**DeluxeMenus Config**:
```yaml
menu-title: "&cMain Menu"
menu-rows: 3
items:
  test_item:
    slot: 13
    material: DIAMOND
    display-name: "&bTest Item"
    left-click-commands:
      - "[player] say Test command"
```

**DGeyserMenuFlux Config**:
```yaml
menu_title: "&cMain Menu"
rows: 3
items:
  test_item:
    slot: 13
    material: DIAMOND
    display_name: "&bTest Item"
    left_click_commands:
      - "[player] say Test command"
```

---

## 💡 Pro Tips

### PAPI Variable Usage
```yaml
items:
  player_stats:
    slot: 22
    material: PLAYER_HEAD
    display_name: "&b%player_name%"
    lore:
      - "&fLevel: &e%player_level%"
      - "&fBalance: &6%vault_eco_balance%"
      - "&fPlaytime: &b%player_time_hours% hours"
```

### Conditional Command Execution
```yaml
left_click_commands:
  - "[message] &6=== Conditional Test ==="
  - "[console] effect give %player_name% speed 30 1"
  - "[message] &aSpeed effect applied!"
```

### Nested Menu System
```yaml
left_click_commands:
  - "[menu] shop"          # Open shop menu
  - "[message] &2Welcome to the shop!"
```

---

## 🛠 Troubleshooting

### Common Issues
**Q: Menus won't open**
- ✅ Check permission settings `dgeysermenu.use`
- ✅ Confirm menu files are in correct folders
- ✅ Verify YAML syntax is correct

**Q: Commands not executing**
- ✅ Ensure correct command format
- ✅ Check if player has permission to execute commands
- ✅ Review console error messages

**Q: Bedrock players can't see menus**
- ✅ Confirm Floodgate is properly installed
- ✅ Check Bedrock menu config paths

**Q: PAPI variables not displaying**
- ✅ Verify PlaceholderAPI is installed
- ✅ Check variable name spelling

### Debug Mode
Enable debug mode in `config.yml`:
```yaml
settings:
  debug: true
  hot-reload:
    enabled: true
```

## 🎉 Get Started

Download DGeyserMenuFlux now and bring revolutionary menu experiences to your server!

```bash
# Quick command tests
/dgeysermenu open main
/getmenuclock
/dgeysermenu reload
```

**🎯 Perfect multi-version support, blazing performance - every player gets the best interaction experience!**

---
*⭐ If this plugin helps your server, give us a Star!*
```