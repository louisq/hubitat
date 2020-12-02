/*
 * Copyright 2020 Louis-Philippe Querel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
metadata {
    definition(
            name: "Wink local switch control",
            namespace: "com.querel.ha.wlsc",
            author: "Louis-Philippe Querel",
            importUrl: "https://raw.githubusercontent.com/louisq/hubitat/master/drivers/wink_local_switch_control/" +
                    "wink_local_switch_control.groovy"
    ) {
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "SwitchLevel"
        capability "Light"
        capability "Bulb"
        capability "Refresh"
    }
}

preferences {
    section("Wink Device") {
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
        input "deviceId", "text", title: "Wink device ID", required: true
        input "winkIp", "text", title: "IP of the wink hub", required: true
    }
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def on() {
    setLightState(true)
}

def off() {
    setLightState(false)
}

def setLightState(boolean lightOn) {

    String state = "on" ? lightOn : "off"

    String url = getWinkDeviceUrl()
    Map body = [desired_state: [powered: lightOn, brightness: device.currentValue("level")/100], nonce: "123"]

    if (logEnable) log.debug "Sending PUT request to [${url}] to turn light ${state}"

    try {
        httpPut([uri: url, contentType: "application/json", body: body, ignoreSSLIssues: true]) { resp ->
            if (resp.success) {
                updateInternalState(state)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to on failed: ${e.message}"
    }

}

def setLevel(BigDecimal level, duration) {
    setLevel(level)
}

def setLevel(BigDecimal level) {
    String url = getWinkDeviceUrl()
    Map body = [desired_state: [brightness: level/100], nonce: "123"]


    if (logEnable) log.debug "Sending brightness change PUT request to [${url}]"

    try {
        httpPut([uri: url, contentType: "application/json", body: body, ignoreSSLIssues: true]) { resp ->
            if (resp.success) {
                sendEvent(name: "level", value: level, isStateChange: true)

                if (level == 0){
                    updateInternalState("off", level)
                } else {
                    updateInternalState("on", level)
                }
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to set brightness failed: ${e.message}"
    }
}

def refresh() {
    String url = getWinkDeviceUrl()
    if (logEnable) log.debug "Sending GET request to [${url}]"

    try {
        httpGet([uri: url, ignoreSSLIssues: true]) { resp ->
            if (resp.success &&
                    resp.data.containsKey("data") &&
                    resp.data.data.containsKey("last_reading") &&
                    resp.data.data.last_reading.containsKey("powered") &&
                    resp.data.data.last_reading.containsKey("brightness")) {

                if (resp.data) log.debug "${resp.data.data}"

                String state = "on" ? resp.data.data.last_reading.powered : "off"
                BigDecimal level = resp.data.data.last_reading.brightness * 100

                updateInternalState(state, level)
            } else {
                log.warn "Issue with request. Got response: ${resp}"
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to on failed: ${e.message}"
    }
}

def updateInternalState(String expectedSwitchState) {
    String currentSwitchState = device.currentValue("switch")
    if (expectedSwitchState != currentSwitchState){
        if (logEnable) log.debug "Updating internal switch state from ${currentSwitchState} to ${expectedSwitchState}"
        sendEvent(name: "switch", value: expectedSwitchState, isStateChange: true)
    }
}

def updateInternalState(String expectedSwitchState, BigDecimal expectedLevel) {
    updateInternalState(expectedSwitchState)

    int currentLevel = device.currentValue("level")
    if (expectedLevel != BigDecimal.valueOf(currentLevel)){
        if (logEnable) log.debug "Updating internal level value from ${currentLevel} to ${expectedLevel}"
        sendEvent(name: "level", value: expectedLevel, isStateChange: true)
    }
}

String getWinkDeviceUrl() {
    return "https://${settings.winkIp}:8888/device/${settings.deviceId}"
}
