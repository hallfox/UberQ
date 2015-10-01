package hack.bigred15.uberquiz;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rushil on 9/19/15.
 */
public interface StartGameRequest {
    @GET("/game/start")
    Call<StartGameResponse> startGame(@Query("venmoUser") String user, @Query("gcm_id") String gcmID, @Query("joinCode") String joinCode);
}
