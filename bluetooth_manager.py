#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Bluetooth Manager for Windows
Modern tray icon application for managing Bluetooth devices
"""

import sys
import asyncio
import threading
from datetime import datetime
from typing import Optional, List, Dict, Any

try:
    import tkinter as tk
    from tkinter import ttk
    TKINTER_AVAILABLE = True
except ImportError:
    TKINTER_AVAILABLE = False
    print("Warning: tkinter not available.")

try:
    from bleak import BleakScanner, BleakClient
    from bleak.backends.device import BLEDevice
    from bleak.backends.scanner import AdvertisementData
    BLEAK_AVAILABLE = True
except ImportError:
    BLEAK_AVAILABLE = False
    print("Warning: bleak not available. Running in demo mode.")

try:
    import pystray
    from pystray import Icon, MenuItem, Menu
    from PIL import Image, ImageDraw, ImageFont
    PYSTRAY_AVAILABLE = True
except ImportError:
    PYSTRAY_AVAILABLE = False
    print("Warning: pystray not available. Running in windowed mode.")


class BluetoothManager:
    """Manages Bluetooth device scanning and connections."""
    
    def __init__(self):
        self.devices: Dict[str, Dict[str, Any]] = {}
        self.connected_devices: set = set()
        self.scanning = False
        self._scan_callback = None
        self._loop: Optional[asyncio.AbstractEventLoop] = None
        self._thread: Optional[threading.Thread] = None
        
    def start_async_loop(self):
        """Start asyncio event loop in a separate thread."""
        def run_loop():
            self._loop = asyncio.new_event_loop()
            asyncio.set_event_loop(self._loop)
            self._loop.run_forever()
        
        self._thread = threading.Thread(target=run_loop, daemon=True)
        self._thread.start()
        
    def stop_async_loop(self):
        """Stop the asyncio event loop."""
        if self._loop and self._loop.is_running():
            self._loop.call_soon_threadsafe(self._loop.stop)
            
    async def _scan_devices(self, duration: float = 5.0):
        """Scan for Bluetooth LE devices."""
        if not BLEAK_AVAILABLE:
            # Demo mode - generate fake devices
            await asyncio.sleep(1)
            demo_devices = [
                {"address": "AA:BB:CC:DD:EE:01", "name": "Wireless Headphones", "rssi": -45},
                {"address": "AA:BB:CC:DD:EE:02", "name": "Smart Watch", "rssi": -60},
                {"address": "AA:BB:CC:DD:EE:03", "name": "Fitness Tracker", "rssi": -55},
                {"address": "AA:BB:CC:DD:EE:04", "name": "Keyboard", "rssi": -70},
                {"address": "AA:BB:CC:DD:EE:05", "name": "Mouse", "rssi": -65},
            ]
            for device in demo_devices:
                if self._scan_callback:
                    self._scan_callback(device["address"], device["name"], device["rssi"])
            return
            
        discovered = {}
        
        def detection_callback(device: BLEDevice, adv_data: AdvertisementData):
            if device.address not in discovered:
                discovered[device.address] = {
                    "name": device.name or "Unknown",
                    "rssi": device.rssi,
                    "address": device.address
                }
                if self._scan_callback:
                    self._scan_callback(device.address, device.name or "Unknown", device.rssi)
        
        scanner = BleakScanner(detection_callback=detection_callback)
        await scanner.start()
        await asyncio.sleep(duration)
        await scanner.stop()
        
    def scan_devices(self, duration: float = 5.0, callback=None):
        """Start scanning for devices."""
        self._scan_callback = callback
        self.scanning = True
        
        if self._loop:
            asyncio.run_coroutine_threadsafe(self._scan_devices(duration), self._loop)
        else:
            # Run synchronously if no loop
            if self._loop is None:
                self.start_async_loop()
                asyncio.run_coroutine_threadsafe(self._scan_devices(duration), self._loop)
                
    def stop_scan(self):
        """Stop scanning."""
        self.scanning = False
        
    def add_device(self, address: str, name: str, rssi: int = 0):
        """Add a device to the known devices list."""
        if address not in self.devices:
            self.devices[address] = {
                "name": name,
                "address": address,
                "rssi": rssi,
                "battery": None,
                "connected": False,
                "last_seen": datetime.now()
            }
        else:
            self.devices[address]["rssi"] = rssi
            self.devices[address]["last_seen"] = datetime.now()
            
    def remove_device(self, address: str):
        """Remove a device from the list."""
        if address in self.devices:
            del self.devices[address]
        if address in self.connected_devices:
            self.connected_devices.discard(address)
            
    async def _connect_device(self, address: str) -> bool:
        """Connect to a Bluetooth device."""
        if not BLEAK_AVAILABLE:
            await asyncio.sleep(0.5)
            return True
            
        try:
            async with BleakClient(address) as client:
                if client.is_connected:
                    return True
        except Exception as e:
            print(f"Connection error: {e}")
        return False
        
    def connect_device(self, address: str) -> bool:
        """Connect to a device."""
        if not self._loop:
            self.start_async_loop()
            
        future = asyncio.run_coroutine_threadsafe(self._connect_device(address), self._loop)
        try:
            result = future.result(timeout=10.0)
            if result:
                self.connected_devices.add(address)
                self.devices[address]["connected"] = True
            return result
        except Exception as e:
            print(f"Connection failed: {e}")
            return False
            
    async def _disconnect_device(self, address: str):
        """Disconnect from a device."""
        if BLEAK_AVAILABLE:
            try:
                async with BleakClient(address) as client:
                    if client.is_connected:
                        await client.disconnect()
            except Exception as e:
                print(f"Disconnect error: {e}")
                
    def disconnect_device(self, address: str):
        """Disconnect from a device."""
        if address in self.connected_devices:
            if self._loop:
                asyncio.run_coroutine_threadsafe(self._disconnect_device(address), self._loop)
            self.connected_devices.discard(address)
            if address in self.devices:
                self.devices[address]["connected"] = False
                
    async def _get_battery_level(self, address: str) -> Optional[int]:
        """Get battery level from a device."""
        if not BLEAK_AVAILABLE:
            import random
            await asyncio.sleep(0.3)
            return random.randint(20, 100)
            
        BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb"
        BATTERY_CHARACTERISTIC = "00002a19-0000-1000-8000-00805f9b34fb"
        
        try:
            async with BleakClient(address) as client:
                if client.is_connected:
                    battery = await client.read_gatt_char(BATTERY_CHARACTERISTIC)
                    return int(battery[0])
        except Exception as e:
            print(f"Battery read error: {e}")
        return None
        
    def get_battery_level(self, address: str) -> Optional[int]:
        """Get battery level for a device."""
        if not self._loop:
            self.start_async_loop()
            
        future = asyncio.run_coroutine_threadsafe(self._get_battery_level(address), self._loop)
        try:
            return future.result(timeout=5.0)
        except Exception as e:
            print(f"Failed to get battery: {e}")
        return None
        
    def get_device_list(self) -> List[Dict[str, Any]]:
        """Get list of all devices."""
        return list(self.devices.values())
        
    def get_connected_count(self) -> int:
        """Get count of connected devices."""
        return len(self.connected_devices)


class BluetoothTrayApp:
    """System tray application for Bluetooth management."""
    
    def __init__(self):
        self.bt_manager = BluetoothManager()
        self.icon: Optional[Icon] = None
        self.update_interval = 30  # seconds
        self._update_timer = None
        
    def create_icon_image(self, connected: int = 0):
        """Create a Bluetooth icon with connection indicator."""
        if not PYSTRAY_AVAILABLE:
            return None
            
        size = (64, 64)
        image = Image.new('RGBA', size, (0, 0, 0, 0))
        draw = ImageDraw.Draw(image)
        
        # Draw Bluetooth symbol
        points = [
            (32, 10), (20, 20), (32, 30), (20, 40), (32, 50),
            (44, 40), (32, 30), (44, 20), (32, 10)
        ]
        
        # Main color
        bt_color = (64, 169, 255) if connected == 0 else (100, 255, 100)
        
        draw.polygon(points, fill=bt_color, outline=bt_color)
        draw.line([(32, 10), (32, 30)], fill=bt_color, width=2)
        draw.line([(32, 30), (32, 50)], fill=bt_color, width=2)
        
        # Add connection indicator
        if connected > 0:
            draw.ellipse([48, 48, 60, 60], fill=(100, 255, 100), outline=(0, 100, 0))
            
        return image
        
    def on_show_menu(self, icon, item):
        """Show the main menu."""
        pass
        
    def build_menu(self):
        """Build the tray icon menu."""
        if not PYSTRAY_AVAILABLE:
            return None
        return Menu(
            MenuItem('📡 Scan for Devices', self.on_scan),
            MenuItem('─' * 20, enabled=False),
            MenuItem('📱 Device List', self.on_device_list),
            MenuItem('🔋 Check Batteries', self.on_check_batteries),
            MenuItem('─' * 20, enabled=False),
            MenuItem('⚙ Settings', self.on_settings),
            MenuItem('ℹ About', self.on_about),
            MenuItem('─' * 20, enabled=False),
            MenuItem('❌ Exit', self.on_exit)
        )
        
    def on_scan(self, icon, item):
        """Handle scan action."""
        def scan_thread():
            self.bt_manager.scan_devices(
                duration=5.0,
                callback=self.bt_manager.add_device
            )
            # Update icon after scan
            import time
            time.sleep(6)
            connected = self.bt_manager.get_connected_count()
            icon.icon = self.create_icon_image(connected)
            
        thread = threading.Thread(target=scan_thread, daemon=True)
        thread.start()
        
    def on_device_list(self, icon, item):
        """Show device list in a popup or log."""
        if not PYSTRAY_AVAILABLE:
            return
        devices = self.bt_manager.get_device_list()
        message = "Bluetooth Devices:\n\n"
        
        if not devices:
            message += "No devices found.\nClick 'Scan for Devices' to search."
        else:
            for device in devices:
                status = "🟢" if device["connected"] else "⚪"
                battery = f"{device['battery']}%" if device['battery'] else "?"
                message += f"{status} {device['name']}\n"
                message += f"   Address: {device['address']}\n"
                message += f"   Battery: {battery}\n"
                message += f"   Signal: {device['rssi']} dBm\n\n"
                
        # Show notification
        icon.notify(message, "Device List")
        
    def on_check_batteries(self, icon, item):
        """Check battery levels of connected devices."""
        if not PYSTRAY_AVAILABLE:
            return
        def check_thread():
            for address, device in self.bt_manager.devices.items():
                if device["connected"]:
                    battery = self.bt_manager.get_battery_level(address)
                    if battery is not None:
                        self.bt_manager.devices[address]["battery"] = battery
                        
            # Show results
            self.on_device_list(icon, item)
            
        thread = threading.Thread(target=check_thread, daemon=True)
        thread.start()
        icon.notify("Checking battery levels...", "Battery Check")
        
    def on_settings(self, icon, item):
        """Open settings dialog."""
        if not PYSTRAY_AVAILABLE:
            return
        icon.notify("Settings:\n- Auto-refresh: 30s\n- Scan duration: 5s", "Settings")
        
    def on_about(self, icon, item):
        """Show about dialog."""
        if not PYSTRAY_AVAILABLE:
            return
        icon.notify(
            "Bluetooth Manager v1.0\n\n"
            "Modern Bluetooth device manager\n"
            "for Windows system tray.\n\n"
            "Features:\n"
            "• Device scanning\n"
            "• Connection management\n"
            "• Battery monitoring\n"
            "• Auto-refresh",
            "About"
        )
        
    def on_exit(self, icon, item):
        """Exit the application."""
        self.bt_manager.stop_scan()
        self.bt_manager.stop_async_loop()
        icon.stop()
        
    def on_clicked(self, icon, item):
        """Handle icon click."""
        # Show device list on left click
        self.on_device_list(icon, item)
        
    def run(self):
        """Run the tray application."""
        if not PYSTRAY_AVAILABLE:
            print("pystray not available. Running in console mode.")
            self.run_console_mode()
            return
            
        # Create initial icon
        icon_image = self.create_icon_image(0)
        
        # Create tray icon
        self.icon = Icon(
            "BluetoothManager",
            icon_image,
            "Bluetooth Manager",
            self.build_menu()
        )
        
        # Set up click handler
        self.icon.run(detached=False)


class ModernWindow:
    """Modern dark-themed window interface."""
    
    def __init__(self, bt_manager: BluetoothManager):
        self.bt_manager = bt_manager
        self.window = None
        self.device_frame = None
        self.scan_button = None
        self.status_label = None
        
    def create_window(self):
        """Create the main window."""
        if not TKINTER_AVAILABLE:
            print("tkinter not available. Cannot create window.")
            return
            
        self.window = tk.Tk()
        self.window.title("Bluetooth Manager")
        self.window.geometry("500x600")
        self.window.resizable(True, True)
        
        # Dark theme colors
        bg_color = "#1e1e1e"
        fg_color = "#ffffff"
        accent_color = "#40a9ff"
        frame_bg = "#2d2d2d"
        
        self.window.configure(bg=bg_color)
        
        # Style configuration
        style = ttk.Style()
        style.theme_use('clam')
        
        style.configure("Dark.TFrame", background=frame_bg)
        style.configure("Dark.TLabel", background=frame_bg, foreground=fg_color, font=("Segoe UI", 10))
        style.configure("Header.TLabel", background=bg_color, foreground=accent_color, font=("Segoe UI", 14, "bold"))
        style.configure("Dark.TButton", background=accent_color, foreground="white", font=("Segoe UI", 10))
        style.map("Dark.TButton", background=[("active", "#69c0ff")])
        
        # Header
        header = tk.Label(
            self.window,
            text="📡 Bluetooth Manager",
            font=("Segoe UI", 16, "bold"),
            bg=bg_color,
            fg=accent_color
        )
        header.pack(fill=tk.X, padx=20, pady=15)
        
        # Status bar
        status_frame = tk.Frame(self.window, bg=frame_bg)
        status_frame.pack(fill=tk.X, padx=20, pady=5)
        
        self.status_label = tk.Label(
            status_frame,
            text="Ready - Click Scan to find devices",
            font=("Segoe UI", 9),
            bg=frame_bg,
            fg="#888888"
        )
        self.status_label.pack(side=tk.LEFT, pady=5)
        
        # Scan button
        self.scan_button = tk.Button(
            status_frame,
            text="🔍 Scan Devices",
            font=("Segoe UI", 10, "bold"),
            bg=accent_color,
            fg="white",
            relief=tk.FLAT,
            padx=15,
            pady=5,
            cursor="hand2",
            command=self.on_scan_clicked
        )
        self.scan_button.pack(side=tk.RIGHT, pady=5)
        
        # Device list frame
        list_frame = tk.Frame(self.window, bg=frame_bg)
        list_frame.pack(fill=tk.BOTH, expand=True, padx=20, pady=10)
        
        # Scrollable canvas for device list
        canvas = tk.Canvas(list_frame, bg=frame_bg, highlightthickness=0)
        scrollbar = ttk.Scrollbar(list_frame, orient=tk.VERTICAL, command=canvas.yview)
        
        self.device_frame = tk.Frame(canvas, bg=frame_bg)
        
        canvas_window = canvas.create_window((0, 0), window=self.device_frame, anchor=tk.NW)
        
        def configure_frame(event):
            canvas.configure(scrollregion=canvas.bbox("all"))
            canvas.itemconfig(canvas_window, width=event.width)
            
        self.device_frame.bind("<Configure>", configure_frame)
        
        canvas.configure(yscrollcommand=scrollbar.set)
        
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        # Footer
        footer = tk.Label(
            self.window,
            text="Auto-refresh every 30 seconds | Double-click device to connect",
            font=("Segoe UI", 8),
            bg=bg_color,
            fg="#666666"
        )
        footer.pack(fill=tk.X, padx=20, pady=10)
        
        # Bind double-click for connection
        self.window.bind("<Double-Button-1>", self.on_device_double_click)
        
        # Start auto-refresh
        self.schedule_refresh()
        
        return self.window
        
    def update_device_list(self):
        """Update the device list display."""
        if not self.device_frame:
            return
            
        # Clear existing widgets
        for widget in self.device_frame.winfo_children():
            widget.destroy()
            
        devices = self.bt_manager.get_device_list()
        
        if not devices:
            label = tk.Label(
                self.device_frame,
                text="No devices found.\nClick 'Scan Devices' to search.",
                font=("Segoe UI", 11),
                bg="#2d2d2d",
                fg="#888888",
                pady=30
            )
            label.pack(fill=tk.X, padx=10, pady=10)
            return
            
        for device in devices:
            self.create_device_card(device)
            
    def create_device_card(self, device: Dict[str, Any]):
        """Create a device card widget."""
        if not TKINTER_AVAILABLE:
            return
            
        card = tk.Frame(self.device_frame, bg="#3a3a3a", relief=tk.RAISED, borderwidth=1)
        card.pack(fill=tk.X, padx=10, pady=5)
        
        # Device info
        info_frame = tk.Frame(card, bg="#3a3a3a")
        info_frame.pack(fill=tk.X, padx=15, pady=10)
        
        # Status indicator
        status_color = "#4caf50" if device["connected"] else "#666666"
        status_dot = tk.Canvas(info_frame, width=12, height=12, bg="#3a3a3a", highlightthickness=0)
        status_dot.create_oval(2, 2, 10, 10, fill=status_color, outline=status_color)
        status_dot.pack(side=tk.LEFT, padx=(0, 10))
        
        # Name
        name_label = tk.Label(
            info_frame,
            text=device["name"],
            font=("Segoe UI", 12, "bold"),
            bg="#3a3a3a",
            fg="#ffffff"
        )
        name_label.pack(side=tk.LEFT, fill=tk.X, expand=True)
        
        # Battery
        battery_text = f"🔋 {device['battery']}%" if device['battery'] else "🔋 --"
        battery_label = tk.Label(
            info_frame,
            text=battery_text,
            font=("Segoe UI", 10),
            bg="#3a3a3a",
            fg="#4caf50" if device['battery'] and device['battery'] > 50 else "#ff9800"
        )
        battery_label.pack(side=tk.RIGHT, padx=(10, 0))
        
        # Details
        details_frame = tk.Frame(card, bg="#3a3a3a")
        details_frame.pack(fill=tk.X, padx=15, pady=(0, 10))
        
        address_label = tk.Label(
            details_frame,
            text=f"Address: {device['address']}",
            font=("Segoe UI", 9),
            bg="#3a3a3a",
            fg="#888888"
        )
        address_label.pack(side=tk.LEFT)
        
        signal_label = tk.Label(
            details_frame,
            text=f"Signal: {device['rssi']} dBm",
            font=("Segoe UI", 9),
            bg="#3a3a3a",
            fg="#888888"
        )
        signal_label.pack(side=tk.LEFT, padx=20)
        
        # Buttons
        btn_frame = tk.Frame(card, bg="#3a3a3a")
        btn_frame.pack(fill=tk.X, padx=15, pady=(0, 10))
        
        if device["connected"]:
            disconnect_btn = tk.Button(
                btn_frame,
                text="Disconnect",
                font=("Segoe UI", 9),
                bg="#f44336",
                fg="white",
                relief=tk.FLAT,
                padx=10,
                pady=3,
                cursor="hand2",
                command=lambda: self.on_disconnect(device["address"])
            )
            disconnect_btn.pack(side=tk.RIGHT)
        else:
            connect_btn = tk.Button(
                btn_frame,
                text="Connect",
                font=("Segoe UI", 9),
                bg="#40a9ff",
                fg="white",
                relief=tk.FLAT,
                padx=10,
                pady=3,
                cursor="hand2",
                command=lambda: self.on_connect(device["address"])
            )
            connect_btn.pack(side=tk.RIGHT)
            
        remove_btn = tk.Button(
            btn_frame,
            text="Remove",
            font=("Segoe UI", 9),
            bg="#555555",
            fg="white",
            relief=tk.FLAT,
            padx=10,
            pady=3,
            cursor="hand2",
            command=lambda: self.on_remove(device["address"])
        )
        remove_btn.pack(side=tk.RIGHT, padx=(0, 10))
        
        check_battery_btn = tk.Button(
            btn_frame,
            text="Check Battery",
            font=("Segoe UI", 9),
            bg="#555555",
            fg="white",
            relief=tk.FLAT,
            padx=10,
            pady=3,
            cursor="hand2",
            command=lambda: self.on_check_battery(device["address"])
        )
        check_battery_btn.pack(side=tk.RIGHT, padx=(0, 10))
        
    def on_scan_clicked(self):
        """Handle scan button click."""
        if self.scan_button:
            self.scan_button.config(text="Scanning...", state=tk.DISABLED)
        if self.status_label:
            self.status_label.config(text="Scanning for devices...")
            
        def scan_thread():
            self.bt_manager.scan_devices(
                duration=5.0,
                callback=self.bt_manager.add_device
            )
            import time
            time.sleep(6)
            
            # Update UI
            if self.window:
                self.window.after(0, self.scan_complete)
                
        thread = threading.Thread(target=scan_thread, daemon=True)
        thread.start()
        
    def scan_complete(self):
        """Handle scan completion."""
        if self.scan_button:
            self.scan_button.config(text="🔍 Scan Devices", state=tk.NORMAL)
        if self.status_label:
            count = len(self.bt_manager.get_device_list())
            self.status_label.config(text=f"Scan complete - {count} devices found")
        self.update_device_list()
        
    def on_connect(self, address: str):
        """Handle device connection."""
        if self.status_label:
            self.status_label.config(text=f"Connecting to {address}...")
            
        def connect_thread():
            result = self.bt_manager.connect_device(address)
            if self.window:
                self.window.after(0, lambda: self.connection_update(result, address))
                
        thread = threading.Thread(target=connect_thread, daemon=True)
        thread.start()
        
    def connection_update(self, success: bool, address: str):
        """Update UI after connection attempt."""
        device = self.bt_manager.devices.get(address, {})
        name = device.get("name", "Unknown")
        
        if success:
            if self.status_label:
                self.status_label.config(text=f"Connected to {name}")
        else:
            if self.status_label:
                self.status_label.config(text=f"Failed to connect to {name}")
        self.update_device_list()
        
    def on_disconnect(self, address: str):
        """Handle device disconnection."""
        self.bt_manager.disconnect_device(address)
        device = self.bt_manager.devices.get(address, {})
        name = device.get("name", "Unknown")
        
        if self.status_label:
            self.status_label.config(text=f"Disconnected from {name}")
        self.update_device_list()
        
    def on_remove(self, address: str):
        """Handle device removal."""
        device = self.bt_manager.devices.get(address, {})
        name = device.get("name", "Unknown")
        
        self.bt_manager.remove_device(address)
        
        if self.status_label:
            self.status_label.config(text=f"Removed {name}")
        self.update_device_list()
        
    def on_check_battery(self, address: str):
        """Handle battery check."""
        if self.status_label:
            self.status_label.config(text="Checking battery...")
            
        def battery_thread():
            battery = self.bt_manager.get_battery_level(address)
            if battery is not None:
                self.bt_manager.devices[address]["battery"] = battery
            if self.window:
                self.window.after(0, lambda: self.battery_update(address, battery))
                
        thread = threading.Thread(target=battery_thread, daemon=True)
        thread.start()
        
    def battery_update(self, address: str, battery: Optional[int]):
        """Update UI after battery check."""
        if battery is not None:
            if self.status_label:
                self.status_label.config(text=f"Battery: {battery}%")
        else:
            if self.status_label:
                self.status_label.config(text="Could not read battery level")
        self.update_device_list()
        
    def on_device_double_click(self, event):
        """Handle double-click on device."""
        # Find which device was clicked
        for widget in self.device_frame.winfo_children():
            if widget.winfo_containing(event.x_root, event.y_root) == widget:
                # Get the address from the widget
                pass
                
    def schedule_refresh(self):
        """Schedule automatic refresh."""
        if self.window:
            self.update_device_list()
            self.window.after(30000, self.schedule_refresh)
            
    def run(self):
        """Run the window application."""
        if not TKINTER_AVAILABLE:
            print("tkinter is not available. Cannot start GUI.")
            print("Please install tkinter or run in console mode.")
            return
        self.create_window()
        if self.window:
            self.update_device_list()
            self.window.mainloop()


def main():
    """Main entry point."""
    print("=" * 50)
    print("Bluetooth Manager for Windows")
    print("=" * 50)
    print()
    
    # Check dependencies
    if not BLEAK_AVAILABLE:
        print("⚠ Warning: bleak library not installed")
        print("  Install with: pip install bleak")
        print("  Running in demo mode...")
        print()
        
    if not PYSTRAY_AVAILABLE:
        print("⚠ Warning: pystray library not installed")
        print("  Install with: pip install pystray pillow")
        print("  Will use window mode instead...")
        print()
        
    # Create Bluetooth manager
    bt_manager = BluetoothManager()
    
    # Determine which mode to run
    if PYSTRAY_AVAILABLE and len(sys.argv) > 1 and sys.argv[1] == "--tray":
        # Run tray mode
        app = BluetoothTrayApp()
        app.run()
    else:
        # Run window mode
        print("Starting modern window interface...")
        window_app = ModernWindow(bt_manager)
        window_app.run()


if __name__ == "__main__":
    main()
