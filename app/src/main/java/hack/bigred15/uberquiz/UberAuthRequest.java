package hack.bigred15.uberquiz;

import retrofit.http.Field;
import retrofit.http.GET;

/**
 * Created by rushil on 9/19/15.
 */
public interface UberAuthRequest {
    @GET("/oauth/authorize")
    void getLoginURL(@Field("response_type") String type, @Field("client_id") String clientId);
}
