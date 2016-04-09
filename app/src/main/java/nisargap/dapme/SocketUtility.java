package nisargap.dapme;
import android.app.Activity;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by nisargap on 4/9/16.
 */
public class SocketUtility {

    private static final SocketUtility instance = new SocketUtility("http://45.55.146.149:80");

    private Socket mSocket;

    private String mHost;


    public SocketUtility(String hostName){


        mHost = hostName;

    }

    public void connect(){
        try {
            mSocket = IO.socket(mHost);
            mSocket.connect();
            Log.d("ME", "SOCKET CONNECTED BRO!");
        } catch (URISyntaxException e) {}

    }

    public void closeConnection(){

        try {
            mSocket.close();
            mSocket.disconnect();
            Log.d("ME", "SOCKET CONNECTED BRO!");

        }catch(NullPointerException e){

            Log.d("ME", e.getMessage());

        }

    }

    public static SocketUtility getInstance() {
        return instance;
    }

    public void listenOnUserData(Emitter.Listener callback){

        mSocket.on("user_data", callback);

    }

    public void stopListenOnUserData(){

        mSocket.off("user_data");
    }

    public void sendUserData(double lat, double lng, String uuid) throws JSONException {

        JSONObject dataToSend = new JSONObject();

        dataToSend.put("lat", lat);
        dataToSend.put("lng", lng);
        dataToSend.put("user", uuid);

        mSocket.emit("user_data", dataToSend);
        // Log.d("ME", "WE JUST EMITTED SON!");

    }


}
