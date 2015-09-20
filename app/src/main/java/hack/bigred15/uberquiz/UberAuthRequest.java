package hack.bigred15.uberquiz;


import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by rushil on 9/19/15.
 */
public interface UberAuthRequest {
    @GET("/uberauth")
    Call<OAuthResponse> getToken(@Query("authorization_code") String code, @Query("gcm_id") String gcmID);
}
