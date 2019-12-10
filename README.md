# hubitat-ModernForms
First attempt on writing a device driver for hubitat. In this case for the Modern Form Fans

Note: Setting the speed will not change the on/off of the Fan. You need to set the Speed to "on" to turn it on. This allows you to change the speed without affecting the state of the fan

To Install:
You will need to install them in the Modern Forms app first as this will alow you to pair the fan with your WiFi network. This is cloud based but once that is done, further control through this driver will be local.

<ul>
  <li>In Hubtat --> Driver Code - New Driver</li>
  <li>Copy/Paste the ModernFormsFan.groovy content (RAW format)</li>
  <li>Click Save</li>
  <li>In Hubtat --> Devices --> Add Virtual Device</li>
  <li>Enter a Device Name (ie: Living Room Fan). This needs to be unique</li>
  <li>Select the Type --> (under user) Simple Forms Fan</li>
</ul>
You now have a new device. In the device settings, enter the IP address of the fan as xxx.xxx.xxx.xxx. You can probably find this in your routers device list. Mine show up with a device name of MXCHIP. If you have the fans registered in the Modern Forms App, you can find the IP there as well under the fans properties.

Repeat the virtual device steps above for any additional fans

Please let me know how this works for you.
