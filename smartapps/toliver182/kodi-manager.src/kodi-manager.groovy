/**
 *  KODI Manager
 *
 *  forked from a plex version: https://github.com/iBeech/SmartThings/tree/master/PlexManager
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "KODI Manager",
    namespace: "toliver182",
    author: "toliver182",
    description: "Add kodi endpoints",
    category: "Safety & Security",
    iconUrl: "http://download.easyicon.net/png/1126483/64/",
    iconX2Url: "http://download.easyicon.net/png/1126483/128/",
    iconX3Url: "http://download.easyicon.net/png/1126483/128/")


preferences {

  section("Kodi Client"){
      	input "clientName", "text", "title": "Client Name", multiple: false, required: true
  		input "kodiIp", "text", "title": "Kodi IP", multiple: false, required: true
        input "kodiPort", "text", "title": "Kodi port", multiple: false, required: true
    	input "kodiUsername", "text", "title": "Kodi Username", multiple: false, required: true
    	input "kodiPassword", "password", "title": "Kodi Password", multiple: false, required: true
    	input "theHub", "hub", title: "On which hub?", multiple: false, required: true
  }
}

def installed() {
	
	log.debug "Installed with settings: ${settings}"
	initialize()
    
}

def initialize() {
subscribe(location, null, response, [filterEvents:false])   
checkKodi();
state.poll = true;
getActiveStatus();

}

def updated() {
log.debug "Updated with settings: ${settings}"
checkKodi();


    if(!state.poll){
    	state.poll = true;
    	getActiveStatus();
    } 
    getActiveStatus();

}


//Method for encoding username and password in base64
def basicAuthBase64() {
def s ="$settings.kodiUsername:$settings.kodiPassword"
def encoded = s.bytes.encodeBase64();
return encoded
}


def uninstalled() {

	state.poll = false;    
	def delete = getChildDevices()   
	delete.each {
		deleteChildDevice(it.deviceNetworkId)
	}   
}


def getActiveStatus(){

if(!state.poll) return;

	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetActivePlayers\", \"id\": 1}"
	executeRequest("/jsonrpc", "POST",command);
    
     runIn(10, getActiveStatus);
}

def getPlayingStatus(){
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetProperties\",  \"params\": {    \"properties\": [      \"speed\"    ],    \"playerid\": 1  },  \"id\": 1}"
	executeRequest("/jsonrpc", "POST",command);
}
def response(evt) {	 
    def msg = parseLanMessage(evt.description); 
 	evaluatePlaybackState(evt);

}

def evaluatePlaybackState(evt) {
def msg = parseLanMessage(evt.description);
def eventMap = stringToMap(evt.value)
		//First check if player is active.
      	if(msg && msg.body && msg.body.startsWith("{\"id\":1,\"jsonrpc\":\"2.0\",\"result\":[{\"playerid\":1,\"type\":\"video\"}]}"))
        {
      		log.debug "Active, getting playback state"
      		getPlayingStatus() //Player is active, get specific state
      	}
      // Player is not active, state = stopped
      if(msg && msg.body && msg.body.startsWith("{\"id\":1,\"jsonrpc\":\"2.0\",\"result\":[]}"))
      {
  
            
      		getChildDevices().each { kodiClient ->
            def netId = kodiClient.deviceNetworkId
            def ip = netId.replace("KodiClient:", "");
            
            def hexip = convertIPtoHex(ip).toUpperCase();
            if(hexip == eventMap.ip){
            log.debug "Setting playback state to stopped on " + netId
            def playbackState = "stopped";    
            kodiClient.setPlaybackState(playbackState);
            }
      		}
      }
      //Lazy coding, if the active state is true, specific state would have fired. check results.
      if(msg && msg.body && msg.body.startsWith("{\"id\":1,\"jsonrpc\":\"2.0\",\"result\":{\"speed\":1}}"))
      {
      		
            
           	getChildDevices().each { kodiClient ->
             def netId = kodiClient.deviceNetworkId
            def ip = netId.replace("KodiClient:", "");
            def hexip = convertIPtoHex(ip).toUpperCase();
            if(hexip == eventMap.ip){
            log.debug "Setting playback state to playing on " + netId
          	def playbackState = "playing";           
           	kodiClient.setPlaybackState(playbackState);
            }
      		}
      }
    if(msg && msg.body && msg.body.startsWith("{\"id\":1,\"jsonrpc\":\"2.0\",\"result\":{\"speed\":0}}"))
	{
    		
            getChildDevices().each { kodiClient ->
               def netId = kodiClient.deviceNetworkId
            def ip = netId.replace("KodiClient:", "");
            def hexip = convertIPtoHex(ip).toUpperCase();
            if(hexip == eventMap.ip){
            log.debug "Setting playback state to paused on" + netId
            def playbackState = "paused";           
            kodiClient.setPlaybackState(playbackState);
      		}
            }
    }
}


def switchChange(evt) {
    // We are only interested in event data which contains 
    if(evt.value == "on" || evt.value == "off") return;   
    
	log.debug "Kodi event received: " + evt.value;

    def kodiIP = getKodiAddress(evt.value);
    
    // Parse out the new switch state from the event data
    def command = getKodiCommand(evt.value);
   
    log.debug "state: " + state
    
    switch(command) {
    	case "next":
        	log.debug "Sending command 'next' to " + kodiIP
            next(kodiIP);
        break;
        
        case "previous":
        	log.debug "Sending command 'previous' to " + kodiIP
            previous(kodiIP);
        break;
        
        case "play":
        case "pause":
        	playpause(kodiIP);
        case "stop":
    		stop(kodiIP);
        break;
        
        case "scanNewClients":
        	getClients();
            
        case "setVolume":
        	def vol = getKodiVolume(evt.value);
            log.debug "Vol is: " + vol
        	setVolume(kodiIP, vol);
        break;
    }
    
    return;
}

def setVolume(kodiIP, level) {
	log.debug "Executing 'setVolume'"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Application.SetVolume\", \"params\": { \"volume\": "+ level + "}, \"id\": 1}"
    
    executeRequest("/jsonrpc", "POST",command)

}

def checkKodi() {

		log.debug "Checking to see if the client has been added"
    
    	def children = getChildDevices()  ;
  		def childrenEmpty = children.isEmpty();        
     	def KodiClient = children.find{ d -> d.deviceNetworkId.contains("$settings.kodiIp") }  
     
        if(!KodiClient){
        log.debug "No Devices found, adding device"
		KodiClient = addChildDevice("toliver182", "Kodi Client", "KodiClient:$settings.kodiIp" , theHub.id, [label:"$settings.clientName", name:"$settings.clientName"])
        log.debug "Added Device"
        }
        else
        {
        log.debug "Device Already Added"
        }
        subscribe(KodiClient, "switch", switchChange)
}


def playpause(kodiIP) {
	log.debug "playpausehere"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": 1 }, \"id\": 1}"
	executeRequest("/jsonrpc", "POST",command);
}

def next(kodiIP) {
	log.debug "Executing 'next'"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": 1, \"to\": \"next\" }, \"id\": 1}"
    executeRequest("/jsonrpc", "POST",command)
}

def stop(kodiIP){



}

def previous(kodiIP) {
	log.debug "Executing 'next'"
	def command = "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": 1, \"to\": \"previous\" }, \"id\": 1}"
    executeRequest("/jsonrpc", "POST",command)
}

def executeRequest(Path, method, command) {
		   log.debug "Querying $settings.kodiIp"
	log.debug "The " + method + " path is: " + Path;
     
  
	def headers = [:] 
    def basicAuth = basicAuthBase64();
    log.debug "base64 is: "+ basicAuth
	headers.put("HOST", "$settings.kodiIp:$settings.kodiPort")
	headers.put("Authorization", "Basic " + basicAuth )
    headers.put("Content-Type", "application/json")
	try {    
		def actualAction = new physicalgraph.device.HubAction(
		    method: method,
		    path: Path,
            body: command,
		    headers: headers)
		
		sendHubCommand(actualAction)        
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}
def String getKodiAddress(deviceNetworkId) {
def ip = deviceNetworkId.replace("KodiClient:", "");
	def parts = ip.tokenize('.');
	return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
}

def String getKodiCommand(deviceNetworkId) {
def command = deviceNetworkId.replace("KodiClient:", "");
	def parts = command.tokenize('.');
	return parts[4];
}
def String getKodiVolume(deviceNetworkId) {
def command = deviceNetworkId.replace("KodiClient:", "");
	def parts = command.tokenize('.');
	return parts[5];
}
