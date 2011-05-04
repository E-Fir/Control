/**
 * PhoneGap plugin handling OSC communication in Control
 */
package com.charlieroberts.Control;

import java.io.File;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

//import de.sciss.net.*;
import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;

import com.illposed.osc.*;

import java.io.StringWriter;
import java.io.PrintWriter;

public class OSCManager extends Plugin {
	/* THIS LOOKS NASTY
    	webview.loadUrl("javascript:(function() { document.getElementsByTagName('body')[0].style.color = 'red';})()");  
	*/
	public boolean hasAddress = false; // send only after selecting ip address / port to send to
	public int receivePort; 
	public final Object        sync = new Object();
	
	public OSCPortIn receiver;
	public OSCPortOut sender;
	public OSCListener listener;
	public String ipAddress;
	
	public OSCManager() {
		//getLocalIpAddress();
		try {
			Log.d("OSCManager", "building client");
			//in = new OSCPortIn("10.0.2.15", 10005);
			receiver = new OSCPortIn(8080);
			listener = new OSCListener() {
	        	public void acceptMessage(java.util.Date time, OSCMessage message) {
        			System.out.println("Message received!");
        		}
        	};
        	//System.out.println("adding listener");
        	receiver.addListener("/test", listener);
        	receiver.startListening();
        	//System.out.println("starting listener");

		} catch (Exception e) {
			System.err.println("Error creating / binding OSC client");
		}
	}
	
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		PluginResult result = null;
		if (action.equals("send") && hasAddress) {
			//Log.d("OSCManager", "building message");
			String address = "";
			ArrayList<Object> values = new ArrayList<Object>();
			
			try {
				address = data.getString(0);
				for(int i = 2; i < data.length(); i++) {
				    if(Class.forName("java.lang.Double").isInstance(data.get(i))) { // doubles are returned from JSON instead of floatsbut not handled by the oscmsg class
				        values.add( new Float(((Double)data.get(i)).doubleValue()) );
				    }else{
    					values.add( data.get(i) );
    				}
					//Log.d("OSCManager", ""+data.get(i).getClass().toString());
				}
			} catch (Exception e) {
				System.err.println("Error creating JSON from js message");
			}
			
			OSCMessage msg = new OSCMessage( address, values.toArray() );

         	try {
                sender.send(msg);
	         }
	         catch( IOException e ) {
	            System.err.println("CRAP NetUtil osc sending isn't working!!!");

	            StringWriter sw = new StringWriter();
	            e.printStackTrace(new PrintWriter(sw));
	            System.err.println( sw.toString());
	         }
		}else if(action.equals("setIPAddressAndPort")){
			try {
			    ipAddress = data.getString(0);
				sender = new OSCPortOut( InetAddress.getByName(ipAddress), data.getInt(1) );
                        
				System.err.println("something");
	            //c.setTarget( new InetSocketAddress( data.getString(0), data.getInt(1) ));
				hasAddress = true;
			} catch (Exception e) {
				System.err.println("Error creating JSON from js message");
			}
		}else if(action.equals("setOSCReceivePort")){
			try {
			   	receiver = new OSCPortIn(data.getInt(0));
			} catch (Exception e) {
				System.err.println("Error creating JSON from js message");
			}
		}else{
			result = new PluginResult(Status.INVALID_ACTION);
		}
		return result;
	}
	
	public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        System.err.println(inetAddress.getHostAddress().toString());
                    }
                }
            }
        } catch (SocketException ex) {
            System.err.println( "can't get ip" );
        }
        return null;
    }
/*

- (void)pushInterface:(NSValue *)msgPointer;                                                    // NOT DONE
- (void)pushDestination:(NSValue *) msgPointer;                                                 // NOT DONE

- (void)setOSCReceivePort:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;    // DONE
- (void)setIPAddressAndPort:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;  // DONE
- (void)startReceiveThread:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;   // NOT NEEDED
- (void)send:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;                 // DONE
- (void)startPolling:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;         // NOT NEEDED
- (void)stopPolling:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;          // NOT NEEDED
*/

}