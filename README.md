# Network Monitor App

Network Monitor is an Android application that helps you manage your network connections by notifying you when to turn your hotspot on or off based on your current network status. The app monitors your mobile data connection (4G or 5G) and the status of your mobile hotspot, and provides appropriate notifications to guide you.

## Features

- **Network Status Monitoring**: Continuously monitors your network status to provide real-time notifications.
- **Hotspot Notifications**: Notifies you to turn on your hotspot when using 5G, and turn off your hotspot when using 4G.
- **Manual 5G Switch**: Provides instructions for manually switching to 5G if automatic switching is not supported.
- **User-Friendly Interface**: Simple and intuitive user interface with clear instructions and buttons.

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/network-monitor.git
    ```
2. Open the project in Android Studio.
3. Build and run the application on your Android device.

## Usage

1. **Start Monitoring**: Tap the "Start Monitoring" button to begin monitoring your network status.
2. **Stop Monitoring**: Tap the "Stop Monitoring" button to stop monitoring your network status.
3. **Open Hotspot Settings**: Tap the "Open Hotspot Settings" button to quickly access your device's hotspot settings.
4. **Force 5G Only**: Tap the "Force 5G Only" button to attempt to switch your network to 5G only mode. If your device does not support automatic switching, follow the provided instructions.

## Permissions

The app requires the following permissions:
- `ACCESS_NETWORK_STATE`
- `CHANGE_NETWORK_STATE`
- `ACCESS_WIFI_STATE`
- `CHANGE_WIFI_STATE`
- `ACCESS_COARSE_LOCATION`
- `ACCESS_FINE_LOCATION`
- `FOREGROUND_SERVICE`
- `POST_NOTIFICATIONS`
- `READ_PHONE_STATE`
- `MODIFY_PHONE_STATE`
- `WRITE_SECURE_SETTINGS`

These permissions are necessary for monitoring network status, changing network settings, and sending notifications.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request on GitHub if you have any improvements or new features to add.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

If you have any questions or suggestions, feel free to reach out to me at [rahilmaniar18@gmail.com](mailto:rahilmaniar18@gmail.com).

