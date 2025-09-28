# Extra Mace Limiter

```
 _____     _              ___  ___                 _     _           _ _            
|  ___|   | |             |  \/  |                | |   (_)         (_) |           
| |____  _| |_ _ __ __ _  | .  . | __ _  ___ ___  | |    _ _ __ ___  _| |_ ___ _ __ 
|  __\ \/ / __| '__/ _` | | |\/| |/ _` |/ __/ _ \ | |   | | '_ ` _ \| | __/ _ \ '__|
| |___>  <| |_| | | (_| | | |  | | (_| | (_|  __/ | |___| | | | | | | | ||  __/ |   
\____/_/\_\\__|_|  \__,_| \_|  |_/\__,_|\___\___| \_____/_|_| |_| |_|_|\__\___|_|   
```

**Advanced mace management plugin with configurable limits and storage restrictions**

## 📋 Overview

Extra Mace Limiter is a comprehensive Minecraft plugin designed to manage and restrict mace usage on your server. Control where players can store maces, limit how many they can carry, and prevent automated systems from collecting them.

## ✨ Features

### 🎒 **Player Inventory Management**
- **Configurable mace limits** - Set maximum maces per player (default: 2)
- **Pickup prevention** - Block mace collection when at limit
- **Smart message system** - Customizable warning frequencies

### 🚫 **Storage Restrictions**
Block mace placement in:
- **Chests** (including chest boats and minecart chests)
- **Ender Chests** (personal storage)
- **Barrels** (compact storage)
- **Shulker Boxes** (portable containers)
- **Hoppers** (item transport)
- **Droppers** (item dispensing)
- **Dispensers** (automated dispensing)
- **Furnaces** (smelting equipment)
- **Blast Furnaces** (fast smelting)
- **Smokers** (food cooking)
- **Auto Crafters** (automatic crafting)
- **Item Frames** (display frames - regular and glow)
- **Animal Inventories** (mules, donkeys, llamas with chests)

### 🤖 **Automated System Protection**
- **Hopper blocking** - Prevent hoppers from collecting maces
- **Hopper minecart blocking** - Stop moving hoppers from pickup
- **Bundle protection** - Detect and block bundles containing maces
- **Complete automation prevention** - No loopholes for item collection

### 💬 **Intelligent Messaging**
- **Storage blocking messages** - Customizable frequency (default: every 5 attempts)
- **Pickup blocking messages** - Configurable intervals (default: every 100 attempts)
- **Color-coded warnings** - Eye-catching red and gray formatting

## 🛠️ Installation

1. **Download** the latest release
2. **Place** `ExtraMaceLimiter.jar` in your server's `plugins/` folder
3. **Restart** your server
4. **Configure** the plugin using `/eml reload` after editing `config.yml`

## ⚙️ Configuration

The plugin automatically generates a detailed `config.yml` file with extensive comments:

### **Basic Settings**
```yaml
# Maximum maces per player (-1 for unlimited)
max-maces-in-inventory: 2

# Block pickup when at max maces
stop-pickup-at-max-maces: true
```

### **Storage Blocking**
Each storage type can be individually configured:
```yaml
blocked-storages:
  chest: true           # Regular chests, chest boats, minecart chests
  ender-chest: true     # Personal ender chest storage
  barrel: true          # Barrel inventories
  shulker-box: true     # All colored shulker boxes
  hopper: true          # Hopper inventories
  dropper: true         # Dropper inventories
  dispenser: true       # Dispenser inventories
  furnace: true         # Smelting furnaces
  blast-furnace: true   # Fast ore smelting
  smoker: true          # Food cooking equipment
  crafter: true         # Automatic crafting blocks
```

### **Automated System Protection**
```yaml
# Prevent hoppers from picking up maces from ground
block-hopper-pickup: true

# Prevent hopper minecarts from collecting maces
block-hopper-minecart-pickup: true

# Prevent placing maces in item frames
block-item-frame-placement: true

# Prevent bundles containing maces from being placed
block-bundles-with-maces: true
```

### **Message Customization**
```yaml
messages:
  storage-blocking:
    frequency: 5        # Show every 5 attempts (1, 6, 11, 16, ...)
    text: "&c&lHey! &7You can't move a mace outside your inventory."
  
  pickup-blocking:
    frequency: 100      # Show every 100 attempts (1, 100, 200, ...)
    text: "&c&lHey! &7You can't carry more than 2 maces at once."
```

## 🎮 Commands

### **Main Command: `/extramacelimiter`**
**Aliases:** `/eml`, `/macelimiter`

| Command | Description |
|---------|-------------|
| `/eml reload` | Reload configuration from config.yml |
| `/eml version` | Show plugin version information |
| `/eml author` | Display author info and ASCII art |
| `/eml help` | Show command help menu |

### **Permissions**
- `extramacelimiter.admin` - Access to all commands (default: OP)

## 🔧 How It Works

### **Player Actions Blocked:**
1. **Direct placement** - Dragging maces into storage with cursor
2. **Shift-clicking** - Quick-moving maces to storage inventories  
3. **Hotkey swapping** - Using number keys (1-9) to place maces
4. **Item frame placement** - Right-clicking item frames with maces
5. **Bundle placement** - Placing bundles containing maces in storage
6. **Pickup at limit** - Collecting maces when already at maximum

### **Automated Systems Blocked:**
1. **Hopper collection** - Hoppers sucking maces from ground
2. **Hopper minecart pickup** - Moving hoppers collecting items
3. **Inventory transfers** - Any automated mace movement to blocked storage

### **Message System:**
- **Storage attempts**: Messages on 1st, 6th, 11th, 16th attempts (every 5)
- **Pickup attempts**: Messages on 1st, 100th, 200th, 300th attempts (every 100)
- **Configurable frequencies** and custom message text with color codes

## 📸 Screenshots

*Plugin in action showing blocked storage placement*

*Configuration file with ASCII art header*

*Command usage and reload functionality*

*Item frame blocking demonstration*

## 🎯 Use Cases

### **PvP Servers**
- **Limit mace hoarding** - Prevent players from stockpiling powerful weapons
- **Controlled distribution** - Ensure fair access to maces
- **Anti-storage** - Stop players from hiding maces in bases

### **Survival Servers** 
- **Balance gameplay** - Maintain weapon scarcity and value
- **Prevent automation** - Stop mace farms and automated collection
- **Display restrictions** - Control mace showcasing in item frames

### **Economy Servers**
- **Market control** - Maintain mace value by limiting storage
- **Trading focus** - Encourage mace trading rather than hoarding
- **Scarcity management** - Keep maces rare and valuable

## 🛡️ Technical Details

### **Compatibility**
- **Minecraft Version:** 1.21.1+
- **Server Software:** Paper (recommended), Spigot
- **Java Version:** 21+
- **Animal Support:** Works with mules, donkeys, and llamas with chest inventories

### **Performance**
- **Lightweight design** - Minimal server impact
- **Smart event handling** - Only processes relevant interactions
- **Efficient storage** - UUID-based player tracking
- **Memory conscious** - Automatic cleanup of unused data

### **Event Coverage**
- `InventoryClickEvent` - Manual player interactions
- `PlayerPickupItemEvent` - Ground item collection  
- `InventoryPickupItemEvent` - Hopper ground pickup
- `InventoryMoveItemEvent` - Inter-inventory transfers
- `PlayerInteractEntityEvent` - Item frame interactions

## 🐛 Troubleshooting

### **Common Issues**

**Q: Plugin not working after installation**
- Ensure you're running Paper 1.21.1+ or compatible Spigot
- Check console for error messages during startup
- Verify Java 21+ is installed

**Q: Config changes not applying**
- Use `/eml reload` command after editing config.yml
- Check console for configuration errors
- Ensure YAML formatting is correct (no tabs, proper indentation)

**Q: Messages not showing**
- Verify message frequencies aren't set too high
- Check if player has reached attempt threshold
- Ensure color codes are using `&` format

**Q: Some storage types still allow maces**
- Check individual storage type settings in config.yml
- Reload configuration with `/eml reload`
- Verify `blocked-storages` section formatting

## 📝 Support

### **Getting Help**
- **Discord:** [https://discord.gg/hBD2psrM6t](https://discord.gg/hBD2psrM6t)
- **Issues:** Report bugs and suggest features
- **Wiki:** Detailed documentation and examples

### **Contributing**
- Fork the repository
- Create feature branches
- Submit pull requests
- Follow coding standards

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🏷️ Version History

### **v1.0.1** (Latest)
- ✅ Full inventory blocking system
- ✅ Player mace limits with pickup prevention
- ✅ Hopper and hopper minecart protection
- ✅ Item frame placement blocking
- ✅ Configurable message system
- ✅ Command system with reload functionality
- ✅ Tab completion and permissions

---

**Made with ❤️ by notauthorised**

*Keep your server balanced and prevent mace chaos!*