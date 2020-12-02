# Wink Local Switch Control Driver
This Hubitat compatible driver allows the control of dimmable lights which are attached to a wink hub through the local
api of the wink hub. This requires that the wink hub be routed and that the authentication functionality of the wink
local control node service be commented out so that a valid wink token not be required.

The author used this driver to control devices that were not supported by the hubitat hub without the purchase of
additional hardware. i.e. Lutron devices etc...

This driver is neither endorsed by Hubitat or Wink.

## License

Copyright 2020 Louis-Philippe Querel

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Rooting and updating wink

To root the hub, follow the instructions of Matt Carrier: https://www.mattcarrier.com/post/hacking-the-winkhub-part-1/

Once rooted, the authentication of local requests to the wink hub needs to be disabled as we will not have a valid wink
issued token when running locally. Edit the file _/opt/local_control/aau/http.js_ and comment out the lines of the
function _AAU.prototype.authenticate_ (Around lines 56 to 64). Restart the hub and the wink local control api will no
longer require authentication.

## Setting up device

You will need the ip address of the wink hub and the wink id of the device to add the device to hubitat. The list of
wink devices can be obtained from the link _https://{wink_hub_address}:8888/devices_. You will want to note the value of
the _hub_device_id_ for the device which you want to use.

1. Add driver on Hubitat hub
2. Got to devices and click _Add Virtual Device_
3. Give the device a name and select the type _Wink local switch control_
4. Click _Save Device_
5. On the new device's page, scroll down to preferences and set the _Wink device ID_ and _IP of the wink hub_
6. Click _Save Preferences_

The device should now be available as a light which can be dimmed
