# DroneCraft

DroneCraft is a Minecraft Forge mod for **Minecraft 1.20.1** that adds a fully controllable drone with an advanced upgrade system.

The drone can be remotely piloted using a controller and enhanced with multiple modules such as zoom, night vision, thermal vision, speed upgrades, range upgrades, return functionality, and self-destruction capabilities.

---

## Features

### 🚁 Remote Drone Control

* Place a drone in the world
* Connect using the Drone Controller
* Switch to the drone camera in real time
* Fly freely while respecting collisions
* Smooth drone movement with custom controls
* Automatic landing and return systems

### 🔧 Upgrade System

The drone supports multiple installable modules.

#### 📡 Range Modules

* Basic Range Module
* Advanced Range Module
* Elite Range Module
* Infinite Range Module

#### ⚡ Speed Modules

* Basic Speed Module (x1.5)
* Advanced Speed Module (x2)
* Elite Speed Module (x3)

#### 📷 Camera Modules

* Zoom Module
* Night Vision Module
* Thermal Camera Module

#### 🛠 Utility Modules

* Return Module
* Explosive Module

---

## Custom HUD

While piloting the drone, players have access to:

* Current range information
* Active module indicators
* Zoom controls
* Thermal vision status
* Night vision status
* Return controls
* Self-destruct controls
* Speed multiplier information

---

## Persistent Module System

All installed modules remain stored inside the drone.

Modules are preserved when:

* Recovering the drone
* Breaking the drone
* Replacing the drone
* Saving and loading the world

---

## Technologies Used

* Java
* Minecraft Forge 1.20.1
* Gradle
* NBT Data Storage
* Custom GUI Development
* Custom HUD Rendering
* Entity Rendering
* Client/Server Synchronization
* Keybind Systems
* Persistent Module Storage

---

## Controls

| Key | Action                |
| --- | --------------------- |
| V   | Exit Drone Mode       |
| Z   | Zoom                  |
| N   | Toggle Night Vision   |
| C   | Toggle Thermal Camera |
| R   | Return Drone          |
| B   | Self Destruct         |

---

## Available Modules

| Module                | Function                   |
| --------------------- | -------------------------- |
| Basic Range Module    | Range up to 64 blocks      |
| Advanced Range Module | Range up to 128 blocks     |
| Elite Range Module    | Range up to 256 blocks     |
| Infinite Range Module | Unlimited range            |
| Basic Speed Module    | x1.5 speed                 |
| Advanced Speed Module | x2 speed                   |
| Elite Speed Module    | x3 speed                   |
| Zoom Module           | Camera zoom levels         |
| Night Vision Module   | Night vision camera mode   |
| Thermal Camera Module | Highlights nearby entities |
| Return Module         | Recover drone instantly    |
| Explosive Module      | Self-destruct capability   |

---

## Installation

### Requirements

* Minecraft 1.20.1
* Minecraft Forge 1.20.1

### Steps

1. Install Minecraft Forge 1.20.1
2. Download the latest DroneCraft release
3. Place the `.jar` file inside:

```text
.minecraft/mods
```

4. Launch Minecraft using the Forge profile

---

## Screenshots

Add screenshots here:

* Drone in the world
* Drone module interface
* Thermal camera mode
* Night vision mode
* Zoom mode
* Drone flying
* Upgrade modules

---

## Future Plans

* Cargo Drone
* Entity Transport System
* Block Placement and Breaking
* Additional Drone Variants
* Improved Visual Effects
* More Upgrade Modules
* Multiplayer Enhancements

---

## Project Goals

The purpose of DroneCraft is to bring a fully controllable and upgradeable drone experience into Minecraft while remaining immersive, expandable, and easy to use.

The project was developed as a personal Minecraft Forge modding project focused on learning:

* Game Mod Development
* Forge API
* Custom Entities
* Rendering Systems
* GUI Development
* Networking
* Persistent Data Storage

---

## Author

Developed by **Manuel Tagua**

GitHub: https://github.com/ManuelTagua
