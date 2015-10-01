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
import android.widget.TextView;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class QuizActivity extends Activity {

    TextView questionView;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    public String joinCode;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionView = (TextView)findViewById(R.id.question);
        button1 = (Button)findViewById(R.id.first_option);
        button2 = (Button)findViewById(R.id.second_option);
        button3 = (Button)findViewById(R.id.third_option);
        button4 = (Button)findViewById(R.id.fourth_option);
        
        Intent intent = getIntent();
        String question = intent.getStringExtra("question");
        String choice1 = intent.getStringExtra("choice1");
        String choice2 = intent.getStringExtra("choice2");
        String choice3 = intent.getStringExtra("choice3");
        String choice4 = intent.getStringExtra("choice4");
        joinCode = intent.getStringExtra("joinCode");

        questionView.setText(question);
        button1.setText(choice1);
        button2.setText(choice2);
        button3.setText(choice3);
        button4.setText(choice4);
        context = getApplicationContext();
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
        button1.setOnTouchListener(onTouchListener);
        button2.setOnTouchListener(onTouchListener);
        button3.setOnTouchListener(onTouchListener);
        button4.setOnTouchListener(onTouchListener);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        context.registerReceiver(mQuestionReceiver, new IntentFilter(Constants.GCM_NEW_QUESTION));
        context.registerReceiver(mEndGameReceiver, new IntentFilter(Constants.GCM_END_GAME));
    }

    @Override
    protected void onPause(){
        super.onPause();
        context.unregisterReceiver(mQuestionReceiver);
        context.unregisterReceiver(mEndGameReceiver);
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

    private BroadcastReceiver mEndGameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent resultIntent = new Intent(getApplicationContext(), ResultsActivity.class);
            String winner = intent.getStringExtra("winner");
            String amountOwed = intent.getStringExtra("amountOwed");
            startActivity(resultIntent);
            finish();
        }
    };

    public void submitAnswer(View view){
        Button b = (Button)view;
        String answer = b.getText().toString();
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        String gcmID = settings.getString("gcm_key", "");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.AWS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        QuizRequest quizRequest = retrofit.create(QuizRequest.class);
        Call<QuizResponse> submitAnswer = quizRequest.submitAnswer(answer, gcmID, joinCode);
        submitAnswer.enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Response<QuizResponse> response) {
                QuizResponse awsResponse = response.body();
                if(awsResponse.result){

                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }
}
