package com.strat.jose.stratusweather;

import com.strat.jose.stratusweather.data.WeatherOutputData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherForcast {
    @GET("/forcast/2.5/weather")
    public Call<WeatherOutputData> getWeatherInfo(
            @Query("lat")String lat,@Query("lon")String lon,@Query("units") String units, @Query("appid") String appId);
}
