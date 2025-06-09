package org.arcade.atomcity.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TaikoServerService {
    @GET("PlayHistory/{{userNumber}}")
    Call<ResponseType> getUserProfile(@Path("userNumber") String userNumber);

    @GET("GameData/MusicDetails")
    Call<ResponseType> getMusicDetails();
}
