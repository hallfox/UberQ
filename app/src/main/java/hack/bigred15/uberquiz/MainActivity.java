package hack.bigred15.uberquiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends Activity
{

    GoogleCloudMessaging gcm;
    String regid;
    public static final String PROJECT_NUMBER = "232607746977";
    public static final String AWS_URL = "http://uberq-dev.elasticbeanstalk.com";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String accessToken = "";
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void getUberAuthURL(View view){
        getRegID(this);

    }

    public void joinGame(View view){
        Intent intent = new Intent(this, JoinGameActivity.class);
        startActivity(intent);
    }

    public void getRegID(final MainActivity mainActivity){
        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return regid;
            }

            @Override
            protected void onPostExecute(String gcmID) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(AWS_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                AWSCheckRequest service = retrofit.create(AWSCheckRequest.class);
                Call<AWSCheckResponse> awsCall = service.getCheck(gcmID);
                awsCall.enqueue(new Callback<AWSCheckResponse>() {
                    @Override
                    public void onResponse(Response<AWSCheckResponse> response) {
                        AWSCheckResponse awsResponse = response.body();
                        if(awsResponse.result){
                            Intent intent = new Intent(getApplicationContext(), StartGameActivity.class);
                            Log.d("Check", "In here");
                            Log.d("CODE", awsResponse.joinCode);
                            intent.putExtra("join_code", awsResponse.joinCode);
                            startActivity(intent);
                        }else{
                            Intent webViewIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                            startActivity(webViewIntent);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("Error", t.getMessage());
                    }
                });
            }
        }.execute(null, null, null);
    }


}
