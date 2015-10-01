package hack.bigred15.uberquiz;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rushil on 9/20/15.
 */
public interface QuizRequest {
    @GET("/game/answer")
    Call<QuizResponse> submitAnswer(@Query("answer") String answer, @Query("gcm_id") String gcmID, @Query("joinCode") String joinCode);
}
