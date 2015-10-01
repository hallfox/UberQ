package hack.bigred15.uberquiz;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class StartGameActivity extends Activity {

    public Context context;
    public int numPlayers = 1;
    public String joinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_start_game);
        Intent intent = getIntent();
        joinCode = intent.getStringExtra("join_code");
        TextView tView = (TextView)findViewById(R.id.access_code);
        tView.setText(joinCode);
        Button button = (Button)findViewById(R.id.start_game_button);
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
                                v.setBackgroundColor((Integer) animator.getAnimatedValue());
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
    protected void onResume(){
        super.onResume();
        context.registerReceiver(mQuestionReceiver, new IntentFilter(Constants.GCM_NEW_QUESTION));
        context.registerReceiver(mJoinReceiver, new IntentFilter(Constants.GCM_JOIN_GAME));

    }

    @Override
    protected void onPause(){
        super.onPause();
        context.unregisterReceiver(mJoinReceiver);
        context.unregisterReceiver(mQuestionReceiver);
    }

    private BroadcastReceiver mJoinReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView playersTextView = (TextView)findViewById(R.id.number_players);
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("MESSAGE", message);
            numPlayers ++;
            Log.d("MESSAGE", numPlayers + " players have joined!");
            playersTextView.setText(numPlayers + " players have joined!");
            //do other stuff here
        }
    };

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_game, menu);
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

    public void startGame(View view){
        EditText editText = (EditText)findViewById(R.id.name_input);
        TextView textView = (TextView)findViewById(R.id.access_code);
        String user = editText.getText().toString();
        String code = textView.getText().toString();
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        String gcmID = settings.getString("gcm_key", "");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.AWS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StartGameRequest gameRequest = retrofit.create(StartGameRequest.class);
        Call<StartGameResponse> gameResponse = gameRequest.startGame(user, gcmID, code);
        gameResponse.enqueue(new Callback<StartGameResponse>() {
            @Override
            public void onResponse(Response<StartGameResponse> response) {
                StartGameResponse awsResponse = response.body();
                if(awsResponse.result){

                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }
}
