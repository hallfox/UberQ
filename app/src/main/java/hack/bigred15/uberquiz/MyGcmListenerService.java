package hack.bigred15.uberquiz;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;


/**
 * Created by rushil on 9/19/15.
 */
public class MyGcmListenerService extends GcmListenerService{
    private static final String TAG = "MyGcmListenerService";
    @Override
    public void onMessageReceived(String from, Bundle data){
        String message = data.getString("message");
        Log.d("MEssage", message);
        Log.d("From", from);
        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
