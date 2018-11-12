package com.guilherme.openweather.webservice;



import com.guilherme.openweather.model.WeatherRetrofit;

import java.util.List;
import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherService {


    @GET("/data/2.5/weather?")
    Call<WeatherRetrofit> getWeatherService(@Query("q") String city, @Query("units") String units, @Query("APPID") String appid);

    @GET("/img/w/{code}/{.png}")
    Call<WeatherRetrofit> getWeatherImage(@Path("code") String code, @Path(".png") String png);


}
