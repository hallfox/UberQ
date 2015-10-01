package hack.bigred15.uberquiz;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rushil on 9/19/15.
 */
public interface JoinGameRequest {
    @GET("/game/join")
    Call<JoinGameResponse> joinGame(@Query("venmoUser") String user, @Query("gcm_id") String gcmID, @Query("joinCode") String joinCode);
}
