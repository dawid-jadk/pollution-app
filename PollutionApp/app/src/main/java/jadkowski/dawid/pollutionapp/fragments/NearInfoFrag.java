package jadkowski.dawid.pollutionapp.fragments;

/**
 * Created by dawid on 25/01/2016.
 */

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

public class NearInfoFrag extends Fragment {

    private FrameLayout mainLayout;
    private PieChart thePieChart;
    private float[] yAxisData = new float[3];
    private String[] xAxisData = {"Carbon Monoxide", "Methane", "Liquefied Gas"};
    private ProgressDialog locationDialog;
    private String time = "0";
    private String testValue = null;
    private ArrayList<LatLng> listLocations = new ArrayList<>();
    private Geocoder geo;
    private List<Address> addrss, addrss2;
    private LatLng uLoc;
    private boolean uLocReceived = false;
    private UserLocation userLocation;

    public NearInfoFrag() {
        // Required empty public constructor
    }

    //returning an instance of a fragment
    public static NearInfoFrag newInstance() {
        return new NearInfoFrag();
    }

    //on fragment creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationDialog = new ProgressDialog(getActivity());
        getUserGPSLocation i = new getUserGPSLocation(); //running the inner class
        i.execute();                                    //to get the user's location details
    }

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

    public void getArduinoData(String year) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PollutionObjectAPI service = retrofit.create(PollutionObjectAPI.class);
        Call<ResultResponse> call = service.getPollutionByYear(year);
        call.enqueue(new Callback<ResultResponse>() {
            @Override
            public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                try {

                    ResultResponse resultData = response.body();

                    //loop through the JSON array
                    for (int i = 0; i < resultData.getResult().size(); i++) {
                        testValue = String.valueOf(resultData.getResult().get(0).getGas1_value()); //value used for testing whether some data has been received
                        listLocations.add(new LatLng(resultData.getResult().get(i)
                                .getLocation_lat_value(), resultData.getResult().get(i).getLocation_lng_value()));
                    }
                    LatLng closest = getClosestData(new LatLng(uLoc.latitude, uLoc.longitude), listLocations); //get closest location to user

                    for (int i = 0; i < resultData.getResult().size(); i++) {
                        //get pollution data from the closest location
                        //set pollution data to yAxisData array
                        if (closest.latitude == resultData.getResult().get(i).getLocation_lat_value()
                                && closest.longitude == resultData.getResult().get(i).getLocation_lng_value()) {
                            yAxisData[0] = resultData.getResult().get(i).getGas1_value();
                            yAxisData[1] = resultData.getResult().get(i).getGas2_value();
                            yAxisData[2] = resultData.getResult().get(i).getGas3_value();
                            time = String.valueOf(resultData.getResult().get(i).getTime_value());
                        }
                    }
                    //call method to add data to the chart
                    addData();
                    //getting the name of the street based on user's location
                    if (getActivity() != null) {
                        TextView collectedLocation = (TextView) getActivity()
                                .findViewById(R.id.collectedLocation);
                        String adr = "";
                        geo = new Geocoder(getContext(), Locale.getDefault());
                        try {
                            addrss = geo.getFromLocation(getClosestData(new LatLng(uLoc.latitude,
                                    uLoc.longitude), listLocations).latitude, getClosestData(
                                    new LatLng(uLoc.latitude, uLoc.longitude), listLocations)
                                    .longitude, 1); // 1 means max location returned
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (addrss != null && addrss.size() > 0) {
                            for (int i = 0; i < addrss.get(0).getMaxAddressLineIndex(); i++) {
                                adr += addrss.get(0).getAddressLine(i) + " ";
                            }
                            collectedLocation.setText(getString(R.string.closest_loc, adr));
                        }
                    }

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

    //method for setting the status of pollution
    private void setPollutionStatus(int id) {
        TextView statusText = (TextView) getActivity().findViewById(R.id.infoText);
        ImageView statusImage = (ImageView) getActivity().findViewById(R.id.infoImg);
        CardView statusCard = (CardView) getActivity().findViewById(R.id.infoCard);

        switch (id) {
            case 0: {
                mainLayout.setBackgroundColor(Color.rgb(124, 175, 78)); //set the bg colour of the layout on which the chart is drawn to green
                statusText.setText(getString(R.string.low_pollution));
                statusCard.setCardBackgroundColor(Constants.GREEN_CLOUR);
                statusImage.setImageResource(R.drawable.ok); //set status image to OK
                statusCard.refreshDrawableState();
                break;
            }
            case 1: {
                mainLayout.setBackgroundColor(Color.rgb(211, 197, 41)); // -//- to yellow
                statusText.setText(getString(R.string.medium_pollution));
                statusCard.setCardBackgroundColor(Constants.YELLOW_COLOUR);
                statusImage.setImageResource(R.drawable.warning);
                statusCard.refreshDrawableState();
                break;
            }
            case 2:
                mainLayout.setBackgroundColor(Color.rgb(153, 68, 46));// -//- to red
                statusText.setText(getString(R.string.high_pollution));
                statusCard.setCardBackgroundColor(Constants.RED_COLOUR);
                statusImage.setImageResource(R.drawable.bad);
                statusCard.refreshDrawableState();
                break;
        }
    }

    private void addData() {
        if (testValue != null && !testValue.isEmpty()) {

            ArrayList<Entry> yAxisValues = new ArrayList<>();

            for (int i = 0; i < yAxisData.length; i++)
                yAxisValues.add(new Entry(yAxisData[i], i));

            ArrayList<String> xAxisValues = new ArrayList<>();
            for (int i = 0; i < xAxisData.length; i++)
                xAxisValues.add(xAxisData[i]);

            //create pie data set
            PieDataSet pieChartDataSet = new PieDataSet(yAxisValues, "");
            pieChartDataSet.setSliceSpace(2);
            pieChartDataSet.setSelectionShift(4);

            //add colours
            ArrayList<Integer> colors = new ArrayList<>();

            if ((yAxisData[0] > 90) || (yAxisData[1] > 900) || (yAxisData[2] > 1100)) {
                //high
                if (getActivity() != null) setPollutionStatus(2);
                for (int c : Constants.RED_COLOURS) {
                    colors.add(c);
                }
            } else if ((yAxisData[0] >= 20 && yAxisData[0] <= 90) || (yAxisData[1] >= 201 && yAxisData[1] <= 900) || (yAxisData[2] >= 201 && yAxisData[2] <= 900)) {
                //moderate
                if (getActivity() != null) setPollutionStatus(1);
                for (int c : Constants.YELLOW_COLOURS) {
                    colors.add(c);
                }
            } else if ((yAxisData[0] >= 0 && yAxisData[0] <= 19) || (yAxisData[1] >= 0 && yAxisData[1] <= 200) || (yAxisData[2] >= 0 && yAxisData[2] <= 200)) {
                //low
                if (getActivity() != null) setPollutionStatus(0);
                for (int c : Constants.GREEN_COLOURS) {
                    colors.add(c);
                }
            }
            pieChartDataSet.setColors(colors);

            //initialise pie data object now
            PieData thePieChartData = new PieData(xAxisValues, pieChartDataSet);
            thePieChartData.setValueFormatter(new PercentFormatter());
            thePieChartData.setValueTextSize(10f);
            thePieChartData.setValueTextColor(Color.WHITE);

            thePieChart.setDescription(getString(R.string.collected_at, time));
            thePieChart.setData(thePieChartData);
            //undo all highlights
            thePieChart.highlightValue(null);
            //update pie chart
            thePieChart.invalidate();
            testValue = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_fragment1, container, false);
        final ImageView imgOk = (ImageView) rootView.findViewById(R.id.imgOk);
        final ImageView imgWarning = (ImageView) rootView.findViewById(R.id.imgWarning);
        final ImageView imgBad = (ImageView) rootView.findViewById(R.id.imgBad);
        final Animation animationRotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        //layout for the chart
        mainLayout = (FrameLayout) rootView.findViewById(R.id.mainLayout);
        thePieChart = new PieChart(getContext());
        //add pie chart to mainLayout
        mainLayout.addView(thePieChart, -1);
        mainLayout.setBackgroundColor(Constants.GRAY_COLOUR);
        //configure pie chart
        thePieChart.setUsePercentValues(true);
        thePieChart.setDescriptionPosition(850, 555);
        thePieChart.setDescriptionColor(Color.WHITE);
        //enable pie chart hole and configure
        thePieChart.setDrawHoleEnabled(true);
        thePieChart.setHoleColorTransparent(true);
        thePieChart.setHoleRadius(26);
        thePieChart.setTransparentCircleRadius(30);
        thePieChart.animateXY(2000, 2000); // animate horizontal and vertical 2000 milliseconds
        //enable rotation of the chart by touch
        thePieChart.setRotationAngle(0);
        thePieChart.setRotationEnabled(true);
        //on touch listener
        thePieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (e == null)
                    return;
                //show the snackbar with gas details on tap
                Snackbar snackbar = Snackbar
                        .make(mainLayout, xAxisData[e.getXIndex()] + ": " + e.getVal() + "PPM", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
        // customize legends
        Legend pieChartLegend = thePieChart.getLegend();
        pieChartLegend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
        pieChartLegend.setXEntrySpace(0);
        pieChartLegend.setYEntrySpace(12);
        pieChartLegend.setTextSize(11f);
        pieChartLegend.setTextColor(Color.WHITE);
        //animate images
        imgOk.startAnimation(animationRotate);
        imgWarning.startAnimation(animationRotate);
        imgBad.startAnimation(animationRotate);
        return rootView;
    }

    @Override
    public void onDestroy() {
        userLocation.disconnect();
        super.onDestroy();
    }

    public class getUserGPSLocation extends AsyncTask<Void, Void, LatLng> {
        protected void onPreExecute() {
            locationDialog.setCanceledOnTouchOutside(false); //stop user from using application when progress bar is shown
            locationDialog.setMessage(getString(R.string.getting_loc_msg));
            locationDialog.show();
            userLocation = new UserLocation(getContext());
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

        protected void onPostExecute(LatLng location) {
            if (locationDialog.isShowing()) {
                locationDialog.dismiss();
            }

            if (!uLocReceived && location != null) {
                uLoc = new LatLng(location.latitude, location.longitude);
                //prevent from throwing exception when activity is null
                if (getActivity() != null) {
                    TextView userLocation = (TextView) getActivity().findViewById(R.id.yourLocation);
                    //getting approx. address of where the device is located
                    geo = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        addrss2 = geo.getFromLocation(uLoc.latitude, uLoc.longitude, 1); // 1 means max location returned
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String adr2 = "";
                    if (addrss2 != null && addrss2.size() > 0) {
                        for (int i = 0; i < addrss2.get(0).getMaxAddressLineIndex();
                             i++) {
                            adr2 += addrss2.get(0).getAddressLine(i) + " ";
                        }
                        userLocation.setText(getString(R.string.your_loc, adr2));
                    }
                }
                String currentYear = new SimpleDateFormat("yyyy").format(new Date()); //get current year
                getArduinoData(currentYear);
                uLocReceived = true;
            }
            userLocation.disconnect();
        }
    }

}