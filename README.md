# Cloudcutter Android

Android app providing [tuya-cloudcutter](https://github.com/tuya-cloudcutter/tuya-cloudcutter) functionality.

This project is work-in-progress, and currently more of a PoC than an actual, working product.

Current state of the app:

- Only Lightleak profiles may be used, "Classic" profiles are not implemented yet.
- If it happens to work at all, the only available functionality is dumping flash contents.
- "Devices" and "My Dumps" tabs are pretty much useless.

How to use it:
- Download the .APK from [Releases](https://github.com/tuya-cloudcutter/cloudcutter-android/releases) and install it.
- Go to `App Info` (Android settings), choose `Permissions` and grant the `Location` permission (it's required for Wi-Fi connecting and scanning). **The app won't ask for it yet, so you have to grant it manually.**
- Before running the process, ensure you have both Wi-Fi and Location enabled (otherwise it'll just fail silently, waiting forever and timing out).
- Refer to the [Lightleak README](https://github.com/tuya-cloudcutter/lightleak/blob/master/README.md) for a usage guide.
