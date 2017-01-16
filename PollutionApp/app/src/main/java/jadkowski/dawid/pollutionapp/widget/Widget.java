package jadkowski.dawid.pollutionapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jadkowski.dawid.pollutionapp.R;
import jadkowski.dawid.pollutionapp.helpers.Constants;
import jadkowski.dawid.pollutionapp.helpers.ResultResponse;
import jadkowski.dawid.pollutionapp.interfaces.PollutionObjectAPI;
import jadkowski.dawid.pollutionapp.maps.location.UserLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dawid on 14/04/2016.
 */
public class Widget extends AppWidgetProvider {

    private Context context;
    private LatLng uLoc;
    private float gas1, gas2, gas3;
    private ArrayList<LatLng> listLocations = new ArrayList<>();
    private boolean uLocReceived = false;
    private UserLocation userLocation;

    @Override
    public void onUpdate(Context contextNew, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //final int counter = appWidgetIds.length;
        context = contextNew;
        //loop this procedure for each widget created
        for (int widgetId : appWidgetIds) {
            //instantiate the RemoteViews object for the app widget layout.
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            //create an intent to update the widget on button click
            //attach on button touch/click listener
            Intent intent = new Intent(context, Widget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            //add the app widget ID to the intent extras
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
            //run an inner class to get the GPS location, then call another inner class to get the data based on the GPS location
            //then tell the AppWidgetManager to perform an update on current widget
            getUserGPSLocation x = new getUserGPSLocation(remoteViews, widgetId, appWidgetManager);
            x.execute(); //execute AsyncTask
        }
    }

    //method for returning the closest coordinates to user's location
    public LatLng getClosestData(LatLng userPoint, ArrayList<LatLng> lPoints) {
        double dist = -1;
        LatLng closest = new LatLng(-1, -1);
        Location userLocation = new Location("");
        Location sensorLocation = new Location("");
        userLocation.setLatitude(userPoint.latitude);
        userLocation.setLongitude(userPoint.longitude);
        for (LatLng point : lPoints) {
            sensorLocation.setLatitude(point.latitude);
            sensorLocation.setLongitude(point.longitude);
            if (dist == -1) {
                dist = userLocation.distanceTo(sensorLocation);
                closest = new LatLng(sensorLocation.getLatitude(), sensorLocation.getLongitude());
            } else if (userLocation.distanceTo(sensorLocation) < dist) {
                dist = userLocation.distanceTo(sensorLocation);
                closest = new LatLng(sensorLocation.getLatitude(), sensorLocation.getLongitude());
            }
        }
        return closest;
    }

    //method that passes the URL and widget properties
    //to the inner class to receive information
    public void getArduinoData(final String yr, final RemoteViews viewsGas, final int WidgetIDGas, final AppWidgetManager WidgetManagerGas) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PollutionObjectAPI service = retrofit.create(PollutionObjectAPI.class);
        Call<ResultResponse> call = service.getPollutionByYear(yr);
        call.enqueue(new Callback<ResultResponse>() {
            @Override
            public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                try {
                    ResultResponse resultData = response.body();
                    for (int i = 0; i < resultData.getResult().size(); i++) {
                        listLocations.add(new LatLng(resultData.getResult().get(i).getLocation_lat_value(), resultData.getResult().get(i).getLocation_lng_value()));
                    }

                    LatLng closest = getClosestData(new LatLng(uLoc.latitude, uLoc.longitude), listLocations);
                    for (int i = 0; i < resultData.getResult().size(); i++) {
                        if (closest.latitude == resultData.getResult().get(i).getLocation_lat_value()
                                && closest.longitude == resultData.getResult().get(i).getLocation_lng_value()) {
                            gas1 = resultData.getResult().get(i).getGas1_value();
                            gas2 = resultData.getResult().get(i).getGas2_value();
                            gas3 = resultData.getResult().get(i).getGas3_value();
                        }
                    }

                    //set widget text to gas values
                    viewsGas.setTextViewText(R.id.textView, "CO: " + gas1 + " PPM" + "\nCH4: " + gas2 + " PPM" + "\nLPG: " + gas3 + " PPM");
                    //according to the values set the appropriate image on the widget
                    if ((gas1 > 90) || (gas2 > 900) || (gas3 > 1100)) {
                        viewsGas.setImageViewResource(R.id.statusImageWidget, R.drawable.bad2);
                    } else if ((gas1 >= 20 && gas1 <= 90) || (gas2 >= 201 && gas2 <= 900) || (gas3 >= 201 && gas3 <= 900)) {
                        //moderate
                        viewsGas.setImageViewResource(R.id.statusImageWidget, R.drawable.warning);
                    } else if ((gas1 >= 0 && gas1 <= 19) || (gas2 >= 0 && gas2 <= 200) || (gas3 >= 0 && gas3 <= 200)) {
                        //low
                        viewsGas.setImageViewResource(R.id.statusImageWidget, R.drawable.ok);
                    }
                    //update the widget by passing in the changes
                    WidgetManagerGas.updateAppWidget(WidgetIDGas, viewsGas);

                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResultResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }

    //inner class to get user location
    //AsyncTask has been used, it has to be subclassed, hence the need for a class
    //AsyncTask parameters:
    // 1 type of parameters sent upon task execution
    // 2 type of progress units during background task
    // 3 type of the background execution result
    public class getUserGPSLocation extends AsyncTask<Void, Void, LatLng> {
        private RemoteViews viewsGPS;
        private int WidgetIDGPS;
        private AppWidgetManager WidgetManagerGPS;

        getUserGPSLocation(RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager) {
            this.viewsGPS = views;
            this.WidgetIDGPS = appWidgetID;
            this.WidgetManagerGPS = appWidgetManager;
        }

        //before execution
        protected void onPreExecute() {
            userLocation = new UserLocation(context);
        }

        protected LatLng doInBackground(Void... params) {       //keep getting location if it's null
            while (userLocation.getLocation() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return userLocation.getLocation();
        }

        //after background execution
        protected void onPostExecute(LatLng location) {   //if location has been received and it's not null
            // (need to be used since if BG execution spends too much time it cancels the execution and moves on to this method causing NPE)
            if (!uLocReceived && location != null) {
                uLoc = new LatLng(location.latitude, location.longitude);
                String currentYear = new SimpleDateFormat("yyyy").format(new Date()); //get current year
                getArduinoData(currentYear, viewsGPS, WidgetIDGPS, WidgetManagerGPS); //fetch the data from the server
                uLocReceived = true;
            }
        }
    }
}