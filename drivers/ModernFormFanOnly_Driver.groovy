/*
* Driver for Simple Form Fans - FAN ONLY can be used as a dimmer
* - See https://github.com/gjunky/hubitat-ModernForms/blob/master/drivers/ModernFormsFan.groovy for combined driver of Fan and Light
*
* Allows basic control of the Fan part of Simple Form Fans
* 
*    Versions: 1.02   2019-12-10 - Fixed a bug in the sleep timer reporting 
 */
metadata {
    definition(name: "Modern Form Fan-Fan-Only", namespace: "gjunky", author: "RobJodh@gmail.com") {
        capability "Switch"
        capability "SwitchLevel"
        command "GetStatus"
    }
}

preferences {
    section("URIs") {
        input "fanIP", "text", title: "Fan IP Address", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}
def getMaxSpeed() {
	return 6
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
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

def on() {
    if (logEnable) log.debug "Sending Fan On Command "
    sendCommand("fanOn", true)
}

def off() {
    if (logEnable) log.debug "Sending Fan Off Command "
    sendCommand("fanOn", false)
}



def GetStatus() {
    if (logEnable) log.debug("Querying Fan")
    showStatus(sendCommand("queryDynamicShadowData", 1))
}


/*
 *    Set the fan speed (only 1-5 is supported in the capability). Convert Low-High --> 1-5
 *        Handle the On and Off "Speed" as on/off commands
 */
def setLevel(speed) {
    def speedInt = speed / (100 / getMaxSpeed())
    if (logEnable) log.debug "Sending Speed Command " + speedInt
    if (speedInt > 0) sendCommand("fanSpeed", speedInt)
    else sendCommand("fanOn", false)
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