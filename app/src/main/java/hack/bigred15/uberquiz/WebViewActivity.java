package hack.bigred15.uberquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.graphics.Bitmap;
import android.os.Bundle;
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

public class WebViewActivity extends Activity {

    public static final String UBER_OAUTH_URL = "https://login.uber.com/oauth/authorize?client_id=SL6ETlvv7NQlcHLopkukvdQ_IUYDjsQw&response_type=code&scope=profile";
    public static final String UBER_LOGIN_URL = "https://login.uber.com/login";
    public static final String AWS_URL = "http://uberq-dev.elasticbeanstalk.com/uberauth";

    GoogleCloudMessaging gcm;
    String regid;
    String uberID;
    public static final String PROJECT_NUMBER = "232607746977";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toast.makeText(getApplicationContext(), "Please login to your Uber Account and then try again", Toast.LENGTH_LONG);
        final WebView webView = (WebView) findViewById(R.id.uber_login);
        webView.loadUrl(UBER_OAUTH_URL);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("URL", url);
                String fragment = "?code=";
                int start = url.indexOf(fragment);
                if (start != -1) {
                    webView.stopLoading();
                    uberID = url.substring(start + fragment.length(), url.length());
                    getRegID();
                    Log.d("OAUTH", uberID);
                }else {
                    String denied = "access_denied";
                    if (url.contains(denied)) {
                        finish();
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void getRegID(){
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
                sendIDsAWS(uberID, gcmID);
            }
        }.execute(null, null, null);
    }

    public void sendIDsAWS(String uberCode, String gcmID){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AWS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UberAuthRequest service = retrofit.create(UberAuthRequest.class);
        Call<OAuthResponse> oauthCall = service.getToken(uberCode, gcmID);
        oauthCall.enqueue(new Callback<OAuthResponse>() {
            @Override
            public void onResponse(Response<OAuthResponse> response) {
                OAuthResponse oauth = response.body();
                String joinCode = oauth.joinCode;
                Intent intent = new Intent(getApplicationContext(), StartGameActivity.class);
                intent.putExtra("join_code", joinCode);
                startActivity(intent);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }
}
