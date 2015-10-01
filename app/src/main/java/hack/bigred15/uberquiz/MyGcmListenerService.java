package hack.bigred15.uberquiz;

import android.content.Intent;
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
        String type = data.getString("type");
        Intent intent = new Intent(type);
        if(type.equals(Constants.GCM_JOIN_GAME)){
            String message = data.getString("message");
            intent.putExtra("message", message);
            getApplication().sendBroadcast(intent);
        }
        if(type.equals(Constants.GCM_NEW_QUESTION)) {
            Log.d("Good", "New Question");
            String question = data.getString("question");
            String choice1 = data.getString("choice1");
            String choice2 = data.getString("choice2");
            String choice3 = data.getString("choice3");
            String choice4 = data.getString("choice4");

            intent.putExtra("question", question);
            intent.putExtra("choice1", choice1);
            intent.putExtra("choice2", choice2);
            intent.putExtra("choice3", choice3);
            intent.putExtra("choice4", choice4);
            getApplication().sendBroadcast(intent);
        }
        if(type.equals(Constants.GCM_END_GAME)){
            String winner = data.getString("winner");
            String dollarAmountOwed = data.getString("amountOwed");
            intent.putExtra("winner", winner);
            intent.putExtra("amountOwed", dollarAmountOwed);
            getApplication().sendBroadcast(intent);
        }



        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
