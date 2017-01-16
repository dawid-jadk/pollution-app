package jadkowski.dawid.pollutionapp.interfaces;

/**
 * Created by dawid on 07/01/2017.
 */

import jadkowski.dawid.pollutionapp.helpers.ResultResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PollutionObjectAPI {

    @GET("get_latest_pollution.php")
    Call<ResultResponse> getPollutionAllData();

    @GET("get_latest_pollution.php")
    Call<ResultResponse> getLatestPollutionData();

    @GET("get_pollution_by_month_year.php?")
    Call<ResultResponse> getPollutionByMonthYear(@Query("month") String month,
                                         @Query("year") String year);

    @GET("get_pollution_by_day.php?")
    Call<ResultResponse> getPollutionByMonthDayYear(@Query("month") String month,
                                                 @Query("day") String day,
                                                 @Query("year") String year);

    @GET("get_pollution_by_year.php?")
    Call<ResultResponse> getPollutionByYear(@Query("year") String year);
}