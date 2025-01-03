# XNull Remote Keys

An Android application that lets you use your phone as a wireless numpad and function keys controller for your Windows PC.

## Features

- Virtual numpad
- Function keys (F1-F12)
- Automatic server discovery on local network
- Material Design 3 UI
- Toggle between numpad and function keys
- Connection status indicators

## Requirements

- Android 6.0 (API level 23) or higher
- Device must be on the same network as the PC running the server

## Building

1. Clone the repository:
```
git clone https://github.com/xnull-eu/xnull-remote-keys.git
```

2. Open the project in Android Studio

3. Build the project:
   - Click `Build > Make Project`
   - Or use the keyboard shortcut `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (macOS)

## Usage

1. Install and run the Windows server application ([xnull-remote-keys-server](https://github.com/xnull-eu/xnull-remote-keys-server))
2. Launch XNull Remote Keys on your Android device
3. Tap "Scan for Devices" to find available servers
4. Select your server from the list
5. Use the numpad or function keys to control your PC

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Related Projects

- [xnull-remote-keys-server](https://github.com/xnull-eu/xnull-remote-keys-server) - The Windows server application for this Android client
