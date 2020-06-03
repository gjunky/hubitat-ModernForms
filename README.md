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
  <li>Select the Type --> (under user) Modern Forms Fan and pick the one you just installed.</li>
</ul>
You now have a new device. In the device settings, enter the IP address of the fan as xxx.xxx.xxx.xxx. You can probably find this in your routers device list. Mine show up with a device name of MXCHIP. If you have the fans registered in the Modern Forms App, you can find the IP there as well under the fans properties. If you have the capability to assign a static mapping to the fan (instructions vary by router) it will make your integration more stable. <br><br>

<b>Update 2019-12-11:</b> I added two separate devices in case you just want the Fan or just the Light. This might make it possible to integrate it into Google Home (not tested yet). Both work as a dimmer with a level from 0-100 (0 turns the fan off but leaves the prior speed setting). For the fan speed, the calculation is `speed / (100 / max speed )`. Thus setting speed to 33, sets the fan to speed 2 ` 2 = 33 / (100 / 6) ` as 6 is the max speed of the fans at the current time, and the answer is rounded down to the nearest integer. 

Repeat the virtual device steps above for any additional fans

Please let me know how this works for you.
