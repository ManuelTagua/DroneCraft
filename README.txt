# DroneCraft

DroneCraft is a Minecraft Forge mod for **Minecraft 1.20.1** that adds a fully controllable drone with an advanced upgrade system.

The drone can be remotely piloted using a controller and enhanced with multiple modules such as zoom, night vision, thermal vision, speed upgrades, range upgrades, return functionality and self-destruction capabilities.

## Features

### Remote Drone Control

* Place a drone in the world.
* Connect using the Drone Controller.
* Switch to the drone camera in real time.
* Fly freely while respecting collisions.

### Upgrade System

The drone supports multiple installable modules:

#### Range Modules

* Basic Range Module
* Advanced Range Module
* Elite Range Module
* Infinite Range Module

#### Speed Modules

* Basic Speed Module (x1.5)
* Advanced Speed Module (x2)
* Elite Speed Module (x3)

#### Camera Modules

* Zoom Module
* Night Vision Module
* Thermal Camera Module

#### Utility Modules

* Return Module
* Explosive Module

### Custom HUD

While piloting the drone, players have access to:

* Current range information
* Active modules
* Zoom controls
* Thermal vision status
* Night vision status
* Return and self-destruction controls

### Persistent Module System

All installed modules are stored inside the drone and remain saved when:

* Recovering the drone
* Breaking the drone
* Replacing the drone

## Technologies Used

* Java
* Minecraft Forge 1.20.1
* Gradle
* NBT Data Storage
* Custom GUI
* Custom HUD
* Entity Rendering
* Client/Server Synchronization

## Controls

| Key | Action          |
| --- | --------------- |
| V   | Exit drone mode |
| Z   | Zoom            |
| N   | Night Vision    |
| C   | Thermal Camera  |
| R   | Return Drone    |
| B   | Self Destruct   |

## Installation

1. Install Minecraft Forge 1.20.1.
2. Download the latest DroneCraft release.
3. Place the `.jar` file inside:

```text
.minecraft/mods
```

4. Launch Minecraft using the Forge profile.

## Screenshots

Add screenshots here:

* Drone in the world
* Module interface
* Thermal camera
* Night vision
* Zoom mode
* Drone flight

## Future Plans

* Cargo Drone
* Entity Transport System
* Block Placement and Breaking
* Additional Drone Variants
* Improved Visual Effects

## Author

Developed by Manuel Tagua as a personal Minecraft Forge modding project.
