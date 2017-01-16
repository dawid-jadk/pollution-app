package jadkowski.dawid.pollutionapp.fragments;

/**
 * Created by dawid on 25/01/2016.
 */

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jadkowski.dawid.pollutionapp.R;
import jadkowski.dawid.pollutionapp.formatters.MyValueFormatter;
import jadkowski.dawid.pollutionapp.formatters.MyYAxisValueFormatter;
import jadkowski.dawid.pollutionapp.helpers.Constants;
import jadkowski.dawid.pollutionapp.helpers.ResultResponse;
import jadkowski.dawid.pollutionapp.interfaces.PollutionObjectAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RealTimeFrag extends Fragment {

    private LineChart theLineChart;
    private String lastDataTime = "0";
    private float gas1, gas2, gas3;
    private double lat, lng;
    private String time = "0";
    private Geocoder geo;
    private List<Address> addrss;

    public RealTimeFrag() {// Required empty constructor
    }

    public static RealTimeFrag newInstance() {
        return new RealTimeFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void getArduinoData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PollutionObjectAPI service = retrofit.create(PollutionObjectAPI.class);
        Call<ResultResponse> call = service.getLatestPollutionData();
        call.enqueue(new Callback<ResultResponse>() {
            @Override
            public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                try {
                    ResultResponse resultData = response.body();
                    for (int i = 0; i < resultData.getResult().size(); i++) {
                        gas1 = resultData.getResult().get(i).getGas1_value();
                        gas2 = resultData.getResult().get(i).getGas2_value();
                        gas3 = resultData.getResult().get(i).getGas3_value();
                        time = resultData.getResult().get(i).getTime_value();
                        lat = resultData.getResult().get(i).getLocation_lat_value();
                        lng = resultData.getResult().get(i).getLocation_lng_value();
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

    private void addEntry() {
        getArduinoData(); //fetch the data from the server

        if (!lastDataTime.equals(time)) {
            LineData chartData = theLineChart.getData();
            if (chartData != null) {
                LineDataSet set = (LineDataSet) chartData.getDataSetByIndex(0);
                LineDataSet set2 = (LineDataSet) chartData.getDataSetByIndex(1);
                LineDataSet set3 = (LineDataSet) chartData.getDataSetByIndex(2);

                if (set == null && set2 == null && set3 == null) {
                    set = createSet(0);
                    chartData.addDataSet(set);
                    set2 = createSet(1);
                    chartData.addDataSet(set2);
                    set3 = createSet(2);
                    chartData.addDataSet(set3);
                }
                //add values
                chartData.addXValue(""); //fix so the chart gets updated with new values and it's making the actual graph to work
                assert set != null;
                chartData.addEntry(new Entry(gas1, set.getEntryCount()), 0);
                chartData.addEntry(new Entry(gas2, set.getEntryCount()), 1);
                chartData.addEntry(new Entry(gas3, set.getEntryCount()), 2);
                TextView deviceLocation = (TextView) getActivity().findViewById(R.id.deviceLocation);

                //getting approx. address of where the device is located
                geo = new Geocoder(getContext(), Locale.getDefault());
                try {
                    addrss = geo.getFromLocation(lat, lng, 1); // 5 means max location returned
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String adr = "";
                if (addrss.size() > 0) {
                    for (int i = 0; i < addrss.get(0).getMaxAddressLineIndex();
                         i++) {
                        adr += addrss.get(0).getAddressLine(i) + " ";
                    }
                    deviceLocation.setText(getString(R.string.device_loc, adr));
                }

                //notify chart that data changed
                theLineChart.notifyDataSetChanged();
                //limit visible entries
                theLineChart.setVisibleXRange(0, 6);
                theLineChart.moveViewToX(chartData.getXValCount() - 7);
                lastDataTime = time;
            }
        }
    }

    private LineDataSet createSet(int number) {
        switch (number) {
            case 0:
                LineDataSet set = new LineDataSet(null, "Carbon Monoxide");
                set.setDrawCubic(false);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                set.setColor(Color.rgb(179, 166, 147));
                set.setCircleColor(Color.rgb(179, 166, 147));
                set.setLineWidth(2f);
                set.setCircleSize(4f);
                set.setFillColor(Color.rgb(179, 166, 147));
                set.setHighLightColor(Color.rgb(255, 160, 0));
                set.setValueTextSize(6f);
                set.setValueTextColor(Color.WHITE);
                return set;
            case 1:
                LineDataSet set2 = new LineDataSet(null, "Methane");
                set2.setDrawCubic(false);
                set2.setAxisDependency(YAxis.AxisDependency.LEFT);
                set2.setColor(Color.rgb(126, 104, 70));
                set2.setCircleColor(Color.rgb(126, 104, 70));
                set2.setLineWidth(2f);
                set2.setCircleSize(4f);
                set2.setFillColor(Color.rgb(126, 104, 70));
                set2.setHighLightColor(Color.rgb(255, 160, 0));
                set2.setValueTextSize(6f);
                set2.setValueTextColor(Color.WHITE);
                return set2;
            case 2:
                LineDataSet set3 = new LineDataSet(null, "Liquefied Gas");
                set3.setDrawCubic(false);
                set3.setAxisDependency(YAxis.AxisDependency.LEFT);
                set3.setColor(Color.rgb(82, 62, 21));
                set3.setCircleColor(Color.rgb(82, 62, 21));
                set3.setLineWidth(2f);
                set3.setCircleSize(4f);
                set3.setFillColor(Color.rgb(82, 62, 21));
                set3.setHighLightColor(Color.rgb(255, 160, 0));
                set3.setValueTextSize(6f);
                set3.setValueTextColor(Color.WHITE);
                return set3;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_fragment3, container, false);
        FrameLayout realtimeLayout = (FrameLayout) rootView.findViewById(R.id.realtimeLayout);

        //create line chart
        theLineChart = new LineChart(getContext());
        //add to realtime layout
        realtimeLayout.addView(theLineChart);
        //customise line chart
        theLineChart.setDescription(""); //needed to keep description empty otherwise default text will appear
        theLineChart.setNoDataTextDescription(getString(R.string.no_data));
        theLineChart.setNoDataText(getString(R.string.no_data));
        //enable value highlighting
        theLineChart.setHighlightPerTapEnabled(true);
        theLineChart.setHighlightPerDragEnabled(true);
        //enable touch gestures and scaling plus dragging
        theLineChart.setTouchEnabled(true);
        theLineChart.setDragEnabled(true);
        theLineChart.setScaleEnabled(true);
        theLineChart.setDrawGridBackground(false);
        //enable pinch to zoom for both axes at the same time
        theLineChart.setPinchZoom(true);

        //add data
        LineData lineGraphData = new LineData();
        lineGraphData.setValueTextColor(Color.WHITE);
        lineGraphData.setValueFormatter(new MyValueFormatter());
        //add data to line charts
        theLineChart.setData(lineGraphData);
        //get legend
        Legend lineGraphLegend = theLineChart.getLegend();
        //customise legend
        lineGraphLegend.setForm(Legend.LegendForm.LINE);
        lineGraphLegend.setTextColor(Color.WHITE);

        XAxis thexAxis = theLineChart.getXAxis();
        thexAxis.setTextColor(Color.WHITE);
        thexAxis.setDrawGridLines(true);
        thexAxis.setGridColor(Color.WHITE);
        thexAxis.setAvoidFirstLastClipping(true);

        YAxis theYaxis = theLineChart.getAxisLeft();
        theYaxis.setTextColor(Color.WHITE);
        theYaxis.setLabelCount(3, false);//changed from 10 to make it look like log?
        theYaxis.setValueFormatter(new MyYAxisValueFormatter());
        theYaxis.setDrawGridLines(true);
        theYaxis.setGridColor(Color.WHITE);

        YAxis y12 = theLineChart.getAxisRight();
        y12.setEnabled(false);
        return rootView;
    }

    //onresume is ran when after oncreate ; oncreate - onstart - onresume
    @Override
    public void onResume() {
        super.onResume();

        final Runnable updateChart = new Runnable() {
            @Override
            public void run() {
                addEntry();
                theLineChart.notifyDataSetChanged(); //let chart know that update has been done
            }
        };

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null) //prevents from application crash
                    return;
                getActivity().runOnUiThread(updateChart);
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 2000);
    }
}