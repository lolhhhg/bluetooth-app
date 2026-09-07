# Bluetooth Manager for Windows

Modern Bluetooth device manager with system tray icon for Windows.

## Features

- 📡 **Device Scanning** - Scan for nearby Bluetooth LE devices
- 🔗 **Connection Management** - Connect/disconnect devices easily
- 🔋 **Battery Monitoring** - Check battery levels of connected devices
- 🖥️ **System Tray Icon** - Runs in background with tray icon
- 🎨 **Modern Dark Interface** - Beautiful dark-themed UI
- 🔄 **Auto-refresh** - Automatically updates device list every 30 seconds

## Installation

### Prerequisites

- Python 3.8 or higher
- Windows 10/11 (for Bluetooth functionality)
- Bluetooth adapter

### Install Dependencies

```bash
pip install -r requirements.txt
```

Or manually:

```bash
pip install bleak pystray Pillow pyinstaller
```

## Usage

### Window Mode (Default)

```bash
python bluetooth_manager.py
```

Opens a modern dark-themed window with full device management controls.

### System Tray Mode

```bash
python bluetooth_manager.py --tray
```

Runs as a system tray icon with context menu.

### Create Executable (Portable EXE)

```bash
pyinstaller --onefile --windowed --name "BluetoothManager" --icon=bluetooth.ico bluetooth_manager.py
```

The executable will be created in the `dist` folder.

## Building the EXE

### Option 1: Using build script

```bash
build_exe.bat
```

### Option 2: Manual PyInstaller command

```bash
pyinstaller --onefile --windowed --name "BluetoothManager" bluetooth_manager.py
```

### Option 3: With custom icon

First, create or download a `.ico` file, then:

```bash
pyinstaller --onefile --windowed --name "BluetoothManager" --icon=myicon.ico bluetooth_manager.py
```

## Project Structure

```
bluetooth-manager/
├── bluetooth_manager.py    # Main application
├── requirements.txt        # Python dependencies
├── build_exe.bat          # Windows build script
├── README.md              # This file
└── dist/                  # Built executables (after building)
```

## Features Detail

### Device List
- Shows all discovered Bluetooth devices
- Displays connection status (connected/disconnected)
- Shows signal strength (RSSI)
- Battery level indicator

### Controls
- **Scan Devices** - Search for new Bluetooth devices
- **Connect** - Establish connection to a device
- **Disconnect** - Terminate connection
- **Check Battery** - Read battery level from device
- **Remove** - Forget/remove device from list

### System Tray
- Left-click: Show device list notification
- Right-click: Open context menu with options
- Icon changes color when devices are connected

## Troubleshooting

### No Bluetooth Adapter Found
Ensure your computer has a Bluetooth adapter and it's enabled.

### Permission Issues on Windows
Run as Administrator if you encounter permission errors.

### bleak Import Error
Make sure you have installed the correct version:
```bash
pip install --upgrade bleak bleak-winrt
```

### pystray Not Working
Ensure Pillow is installed:
```bash
pip install --upgrade Pillow pystray
```

## Demo Mode

If bleak is not installed, the app runs in demo mode with simulated devices for testing the UI.

## License

MIT License - Feel free to modify and distribute.

## Requirements

- Windows 10/11
- Python 3.8+
- Bluetooth 4.0+ (for BLE support)
