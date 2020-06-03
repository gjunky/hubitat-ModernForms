/*
* Driver for Simple Form Fans
*
* Allows basic control of the Fan part of Simple Form Fans
* 
*    Versions: 1.0    Initial version with most capabilities included
*              1.01   Fixed a bug in the sleep timer reporting 
*              1.02   2019-12-10 - Removed extra speed reporting for on/off command
 */
metadata {
    definition(name: "Modern Form Fan", namespace: "gjunky", author: "RobJodh@gmail.com") {
        capability "FanControl"
        capability "SwitchLevel"
        command "GetStatus"
        command "FanOn"
        command "FanOff"
        command "LightOn"
        command "LightOff"
        command "Direction", [[name:"Fan Direction", type: "ENUM", description: "Pick an option", constraints: ["forward","reverse","Summer","Winter"] ] ]
    }
}

preferences {
    section("URIs") {
        input "fanIP", "text", title: "Fan IP Address", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
} 

def getSpeedValue(speed) {
    if (speed == "low") return 1
    else if (speed == "medium-low") return 2
    else if (speed == "medium") return 3
    else if (speed == "medium-high") return 4
    else if (speed == "high") return 5
    else return -1
}
    

def sendCommand(command, commandValue) {
    if (logEnable)  log.debug("------- in sendCommand --------")
    log.info("Fan IP : $fanIP, command: $command, commandValue: $commandValue")

def params = [
		uri: "http://" + fanIP,
		path: "/mf",
		contentType: "application/json",
		headers: ['something' : 'abc'],
		body: "{'$command' : $commandValue}"
	]
    
    if (logEnable) log.debug("params: $params")
    
    try {
        httpPostJson(params) { resp ->
            resp.headers.each {
                if (logEnable) log.debug "${it.name} : ${it.value}"
            }
            if (logEnable) log.debug("resp.data: $resp.data")
            if (logEnable) log.debug "response contentType: ${resp.contentType}"
            showStatus(resp.data)
		    return resp.data
        }
    } catch (e) {
        log.debug "something went wrong: $e"
        checkHttpResponse("error in $command", e.getResponse())
		return null
    }
}
/*
 * Command Section
 */

def FanOn() {
    if (logEnable) log.debug "Sending Fan On Command "
    sendCommand("fanOn", true)
}

def FanOff() {
    if (logEnable) log.debug "Sending Fan Off Command "
    sendCommand("fanOn", false)
}

def LightOn() {
    if (logEnable) log.debug "Sending Light On Command "
    sendCommand("lightOn", true)
}

def LightOff() {
    if (logEnable) log.debug "Sending Light Off Command "
    sendCommand("lightOn", false)
}

def Direction(direction) {
    if (logEnable) log.debug "Sending Fan Direction Command " + direction
    if (direction == "Summer") direction="forward"
    if (direction == "Winter") direction="reverse"
    sendCommand("fanDirection", "'$direction'")
    // Fan Direction doesn't seem to correctly report status, calling it manually
    // GetStatus()
}

def GetStatus() {
    if (logEnable) log.debug("Querying Fan")
    showStatus(sendCommand("queryDynamicShadowData", 1))
}


/*
 *    Set the fan speed (only 1-5 is supported in the capability). Convert Low-High --> 1-5
 *        Handle the On and Off "Speed" as on/off commands
 */
def setSpeed(speed) {
    if (logEnable) log.debug "Sending Speed Command " + speed
    def speedInt = getSpeedValue(speed)
    if (speedInt > 0) sendCommand("fanSpeed", speedInt)
    else if (speed == "on") sendCommand("fanOn", true)
        else if (speed == "off") sendCommand("fanOn", false)
}

/*
 *    Set the light's brightness
 */
def setLevel(level) {
    if (logEnable) log.debug "Sending Light Level " + level
    sendCommand("lightBrightness", level)
}

/*
 *    Show the current status in the device page
 */
def showStatus(retData) {
    if (retData) {
        if (logEnable) log.debug("Show Status: $retData")
        def sleepUntil = 0
        
        if (retData.fanOn) device.sendEvent(name: "fan", value: "On")
        else device.sendEvent(name: "fan", value: "Off")
        device.sendEvent(name: "fanSpeed", value: retData.fanSpeed)
        device.sendEvent(name: "fanDirection", value: retData.fanDirection)
        if (retData.lightOn) device.sendEvent(name: "light", value: "On")
        else device.sendEvent(name: "light", value: "Off")
        device.sendEvent(name: "lightBrightness", value: retData.lightBrightness)
        if (retData.fanSleepTimer != 0) {
            sleepUntil = new Date((retData.fanSleepTimer as long)*1000).format( 'M-d-yyyy HH:mm-ss' )
            device.sendEvent(name: "fanSleepTimer", value: sleepUntil)
        }
        if (retData.lightSleepTimer) {
            sleepUntil = new Date((retData.lightSleepTimer as long)*1000).format( 'M-d-yyyy HH:mm-ss' )
            device.sendEvent(name: "lightSleepTimer", value: retData.lightSleepTimer)
        }
    }
}

