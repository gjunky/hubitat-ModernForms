/*
* Driver for Simple Form Fans
*
* Allows basic control of the Fan part of Simple Form Fans
* 
 */
metadata {
    definition(name: "Simple Form Fan", namespace: "gjunky", author: "RobJodh@gmail.com") {
        capability "FanControl"
//        command "FanOn"
//        command "FanOff"
        command "GetStatus"
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
		return false
    }
}

/*
def FanOn() {
               if (logEnable) log.debug "Sending On Command " + speed

               sendCommand("fanOn", true)
}

def FanOff() {
               if (logEnable) log.debug "Sending Off Command " + speed

               sendCommand("fanOn", false)
}
*/

def setSpeed(speed) {
               if (logEnable) log.debug "Sending Speed Command " + speed
               def speedInt = getSpeedValue(speed)
               if (speedInt > 0) sendCommand("fanSpeed", speedInt)
               else if (speed == "on") sendCommand("fanOn", true)
               else if (speed == "off") sendCommand("fanOn", false)
}

def showStatus(retData) {
    if (logEnable) log.debug("Show Status: $retData")
    if (retData) {
        if (retData.fanOn) device.sendEvent(name: "fan", value: "On")
        else device.sendEvent(name: "fan", value: "Off")
        device.sendEvent(name: "fanSpeed", value: retData.fanSpeed)
        device.sendEvent(name: "fanDirection", value: retData.fanDirection)
        if (retData.lightOn) device.sendEvent(name: "light", value: "On")
        else device.sendEvent(name: "light", value: "Off")
    }
}

def GetStatus() {
    if (logEnable) log.debug("Querying Fan")
    showStatus(sendCommand("queryDynamicShadowData", 1))
}

