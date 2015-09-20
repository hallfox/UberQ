package hack.bigred15.uberquiz;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rushil on 9/19/15.
 */
public interface AWSCheckRequest {
    @GET("/check")
    Call<AWSCheckResponse> getCheck(@Query("gcm_id") String gcmID);
}
