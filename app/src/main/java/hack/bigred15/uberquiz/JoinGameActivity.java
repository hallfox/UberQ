package hack.bigred15.uberquiz;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class JoinGameActivity extends Activity {

    GoogleCloudMessaging gcm;
    String regid;
    Context context;
    public String joinCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        context = getApplicationContext();
        Button button = (Button)findViewById(R.id.join_game_button);
        View.OnTouchListener onTouchListener = new View.OnTouchListener(){
            boolean moveUp = true;
            @Override
            public boolean onTouch(final View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(moveUp){
                        moveUp = false;
                        float currentY = v.getY();
                        v.setY(currentY - 20);
                        Integer colorFrom = getResources().getColor(R.color.colorDeepBlue);
                        Integer colorTo = getResources().getColor(R.color.colorDarkBlue);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                v.setBackgroundColor((Integer)animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();
                    }
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(!moveUp){
                        moveUp = true;
                        float currentY = v.getY();
                        v.setY(currentY + 20);
                        Integer colorTo = getResources().getColor(R.color.colorDeepBlue);
                        Integer colorFrom = getResources().getColor(R.color.colorDarkBlue);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                v.setBackgroundColor((Integer) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();
                    }
                }
                return false;
            }
        };
        button.setOnTouchListener(onTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume(){
        super.onResume();
        context.registerReceiver(mQuestionReceiver, new IntentFilter(Constants.GCM_NEW_QUESTION));
    }

    @Override
    protected void onPause(){
        super.onPause();
        context.unregisterReceiver(mQuestionReceiver);
    }

    private BroadcastReceiver mQuestionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent quizIntent = new Intent(getApplicationContext(), QuizActivity.class);
            String question = intent.getStringExtra("question");
            String choice1 = intent.getStringExtra("choice1");
            String choice2 = intent.getStringExtra("choice2");
            String choice3 = intent.getStringExtra("choice3");
            String choice4 = intent.getStringExtra("choice4");
            quizIntent.putExtra("question", question);
            quizIntent.putExtra("choice1", choice1);
            quizIntent.putExtra("choice2", choice2);
            quizIntent.putExtra("choice3", choice3);
            quizIntent.putExtra("choice4", choice4);
            quizIntent.putExtra("joinCode", joinCode);
            startActivity(quizIntent);
        }
    };

    public void sendCodeJoinGame(View view){
        EditText textFieldCode = (EditText)findViewById(R.id.code_input);
        EditText textFieldName = (EditText)findViewById(R.id.code_input);
        String code = textFieldCode.getText().toString();
        joinCode = code;
        String name = textFieldName.getText().toString();
        getRegID(name, code);
    }

    public void getRegID(String name, String code){
        final String inputName = name;
        final String joinCode = code;
        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(Constants.PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return regid;
            }

            @Override
            protected void onPostExecute(String gcmID) {
                SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("gcm_key", gcmID);
                editor.commit();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.AWS_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                JoinGameRequest service = retrofit.create(JoinGameRequest.class);
                Call<JoinGameResponse> joinResponse = service.joinGame(inputName, gcmID, joinCode);
                joinResponse.enqueue(new Callback<JoinGameResponse>() {
                    @Override
                    public void onResponse(Response<JoinGameResponse> response) {
                        JoinGameResponse awsResponse = response.body();
                        if(awsResponse.result){
                            Toast.makeText(getApplicationContext(), "Joined the game. Waiting for owner to start", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Wrong code. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Throwable t) {

                    }
                });
            }
        }.execute(null, null, null);
    }
}
