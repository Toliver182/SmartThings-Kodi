# SmartThings-Kodi
This is a SmartApp which allows you to control Kodi, where HTTP control has been enabled.

Controlling Kodi through SmartThings has the additional benefit that you are also be to trigger events in SmartThings based on events in Kodi, or visa versa. Such as 'Someone is at the door, pause playback', or 'Kodi is playing, dim the living room lights'.

Install the 'Kodi Manager' SmartApp from your mobile device, specify your Kodi IP and Kodi login details.
This will then create a device for that kodi device.

Updates

Known Issues

Instructions

Add the Kodi device type to SmartThings

1) For UK go to: https://graph-eu01-euwest1.api.smartthings.com35
2) For US go to: https://graph.api.smartthings.com17
3) Click 'My Device Handlers'
4) Click 'New Device Handler' in the top right
5) Click the 'From Code' tab
6) Paste in the code from: https://github.com/Toliver182/SmartThings-Kodi/blob/master/devicetypes/toliver182/kodi-client.src/kodi-client.groovy
7) Click 'Create'
8) Click 'Publish -> For Me'

Add the 'Kodi Manager' SmartApp to SmartThings

1) For UK go to: https://graph-eu01-euwest1.api.smartthings.com35
2) For US go to: https://graph.api.smartthings.com17
3) Click 'My SmartApps'
4) Click the 'From Code' tab
5) Paste in the code from: https://github.com/Toliver182/SmartThings-Kodi/blob/master/smartapps/toliver182/kodi-manager.src/kodi-manager.groovy
6) Click 'Create'
7) Click 'Publish -> For Me'

Install the Kodi Manager SmartApp

1) Goto your mobile device and open SmartThings
2) Got the Marketplace tab at the bottom
3) Tap SmartApps'
4) Scroll to the bottom and tap My Apps'
5) Tap 'Kodi Manager'
6) Fill in the details that have been requested and click 'Done'
7) Go back to 'Things' and you will see your Kodi device there

If you are having any problems, please uninstall the SmartApp, goto the IDE (where you installed the devicetype and SmartApp code) and click the 'Logs' button. Reinstall the SmartApp via your mobile phone and post the logs here for me. Please ensure you check the logs for your username and password before posting!

Massive credit to @iBeech as this is heavily based on his Plex Manager

Let me know if you have any issues.
