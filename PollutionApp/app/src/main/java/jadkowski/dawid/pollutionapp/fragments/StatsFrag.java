package jadkowski.dawid.pollutionapp.fragments;

/**
 * Created by dawid on 25/01/2016.
 */

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import jadkowski.dawid.pollutionapp.R;
import jadkowski.dawid.pollutionapp.formatters.MyValueFormatter;
import jadkowski.dawid.pollutionapp.formatters.MyYAxisValueFormatter;
import jadkowski.dawid.pollutionapp.helpers.Constants;
import jadkowski.dawid.pollutionapp.helpers.ResultResponse;
import jadkowski.dawid.pollutionapp.helpers.Utils;
import jadkowski.dawid.pollutionapp.interfaces.PollutionObjectAPI;
import jadkowski.dawid.pollutionapp.maps.MapFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class StatsFrag extends Fragment {
    boolean b;
    boolean isFragment2Visible = false;
    private Spinner bspinner, mspinner, yearspinner, compSpinnerFDay, compSpinnerFMonth,
            compSpinnerSDay, compSpinnerSMonth;
    private BarChart firstBarChart, secondBarChart;
    private String testValue, testValue2 = null;
    private String time = "0";
    private float[] camden_gas = new float[4], hackney_gas = new float[4],
            haringey_gas = new float[4], islington_gas = new float[4],
            westminster_gas = new float[4];
    //First gas = CO, Second gas = CH4, Third gas = LPG
    private ArrayList<String> xAxisValues = new ArrayList<>(), xAxisValues2 = new ArrayList<>(),
            compDataTimeDate1 = new ArrayList<>(), compDataTimeDate2 = new ArrayList<>(),
            compDataFirstGasDate1 = new ArrayList<>(), compDataFirstGasDate2 = new ArrayList<>(),
            compDataSecondGasDate1 = new ArrayList<>(), compDataSecondGasDate2 = new ArrayList<>(),
            compDataThirdGasDate1 = new ArrayList<>(), compDataThirdGasDate2 = new ArrayList<>(),
            avYears = new ArrayList<>();
    private ArrayList<BarEntry> yAxisValues = new ArrayList<>(), yAxisValues2 = new ArrayList<>();
    private float[][] camden_year = new float[12][4], hackney_year = new float[12][4],
            haringey_year = new float[12][4], islington_year = new float[12][4],
            westminster_year = new float[12][4];
    private ProgressDialog dataDialog;
    private ArrayList<LatLng> camdenArea = new ArrayList<>(), hackneyArea = new ArrayList<>(),
            haringeyArea = new ArrayList<>(), islingtionArea = new ArrayList<>(),
            westminsterArea = new ArrayList<>();
    // chart container
    private FrameLayout comparisonLayout;
    private TimeSeries timeAxisOne, timeAxisTwo;
    private RadioButton CO_radio, CH4_radio, LPG_radio;

    public StatsFrag() {
        // Required empty public constructor
    }

    //return new instance of this fragment
    public static StatsFrag newInstance() {
        return new StatsFrag();
    }

    //used for things to be loaded only when fragment is visible, because viewPager loads two fragments at a time
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isFragment2Visible = isVisibleToUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataDialog = new ProgressDialog(getActivity()); //initialise dialog
    }

    //method for reading the xml file of borough locations and adding them to array
    private void readArea(String[] areaArr, ArrayList<LatLng> area) {
        for (String object : areaArr) {
            String[] tmpArr = object.split(",");
            area.add(new LatLng(Double.parseDouble(tmpArr[0]), Double.parseDouble(tmpArr[1])));
        }
    }

    //update the graph
    //graph number - specifies which graph to update on Stats page
    public void getArduinoData(int graphNumber, String yr, String mon, String day) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PollutionObjectAPI service = retrofit.create(PollutionObjectAPI.class);

        if (firstBarChart != null) {
            firstBarChart.invalidate();
        }
        if (secondBarChart != null) {
            secondBarChart.invalidate();
        }

        if (graphNumber == 0) {
            Call<ResultResponse> call = service.getPollutionByYear(yr);
            call.enqueue(new Callback<ResultResponse>() {
                @Override
                public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                    try {
                        ResultResponse resultData = response.body();
                        for (int i = 0; i < resultData.getResult().size(); i++) {
                            testValue2 = String.valueOf(resultData.getResult()
                                    .get(i).getGas1_value()); //value used for testing whether some data has been received
                            time = resultData.getResult().get(i).getTime_value().substring(5, 7); //get only month from string

                            if (PolyUtil.containsLocation(new LatLng(resultData.getResult().get(i)
                                            .getLocation_lat_value(),
                                            resultData.getResult().get(i).getLocation_lng_value()),
                                    camdenArea, b)) {
                                switch (time) {
                                    case "01":
                                        camden_year[0][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[0][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[0][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[0][3] += 1;
                                        break;

                                    case "02":
                                        camden_year[1][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[1][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[1][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[1][3] += 1;
                                        break;

                                    case "03":
                                        camden_year[2][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[2][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[2][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[2][3] += 1;
                                        break;

                                    case "04":
                                        camden_year[3][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[3][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[3][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[3][3] += 1;
                                        break;

                                    case "05":
                                        camden_year[4][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[4][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[4][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[4][3] += 1;
                                        break;

                                    case "06":
                                        camden_year[5][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[5][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[5][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[5][3] += 1;
                                        break;

                                    case "07":
                                        camden_year[6][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[6][1] += resultData.getResult()
                                                .get(i).getGas2_value();
                                        camden_year[6][2] += resultData.getResult()
                                                .get(i).getGas3_value();
                                        camden_year[6][3] += 1;
                                        break;

                                    case "08":
                                        camden_year[7][0] += resultData.getResult()
                                                .get(i).getGas1_value();
                                        camden_year[7][1] += resultData.getResult()
                                                .get(i).getGas2_value();
                                        camden_year[7][2] += resultData.getResult()
                                                .get(i).getGas3_value();
                                        camden_year[7][3] += 1;
                                        break;

                                    case "09":
                                        camden_year[8][0] += resultData.getResult()
                                                .get(i).getGas1_value();
                                        camden_year[8][1] += resultData.getResult()
                                                .get(i).getGas2_value();
                                        camden_year[8][2] += resultData.getResult()
                                                .get(i).getGas3_value();
                                        camden_year[8][3] += 1;
                                        break;

                                    case "10":
                                        camden_year[9][0] += resultData.getResult()
                                                .get(i).getGas1_value();
                                        camden_year[9][1] += resultData.getResult()
                                                .get(i).getGas2_value();
                                        camden_year[9][2] += resultData.getResult()
                                                .get(i).getGas3_value();
                                        camden_year[9][3] += 1;
                                        break;

                                    case "11":
                                        camden_year[10][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[10][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[10][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[10][3] += 1;
                                        break;

                                    case "12":
                                        camden_year[11][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        camden_year[11][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        camden_year[11][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        camden_year[11][3] += 1;
                                        break;
                                }

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), hackneyArea, b)) {

                                switch (time) {
                                    case "01":
                                        hackney_year[0][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[0][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[0][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[0][3] += 1;
                                        break;

                                    case "02":
                                        hackney_year[1][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[1][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[1][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[1][3] += 1;
                                        break;

                                    case "03":
                                        hackney_year[2][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[2][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[2][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[2][3] += 1;
                                        break;

                                    case "04":
                                        hackney_year[3][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[3][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[3][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[3][3] += 1;
                                        break;

                                    case "05":
                                        hackney_year[4][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[4][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[4][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[4][3] += 1;
                                        break;

                                    case "06":
                                        hackney_year[5][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[5][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[5][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[5][3] += 1;
                                        break;

                                    case "07":
                                        hackney_year[6][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[6][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[6][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[6][3] += 1;
                                        break;

                                    case "08":
                                        hackney_year[7][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[7][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[7][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[7][3] += 1;
                                        break;

                                    case "09":
                                        hackney_year[8][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[8][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[8][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[8][3] += 1;
                                        break;

                                    case "10":
                                        hackney_year[9][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[9][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[9][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[9][3] += 1;
                                        break;

                                    case "11":
                                        hackney_year[10][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[10][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[10][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[10][3] += 1;
                                        break;

                                    case "12":
                                        hackney_year[11][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        hackney_year[11][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        hackney_year[11][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        hackney_year[11][3] += 1;
                                        break;
                                }

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), haringeyArea, b)) {

                                switch (time) {

                                    case "01":
                                        haringey_year[0][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[0][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[0][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[0][3] += 1;
                                        break;

                                    case "02":
                                        haringey_year[1][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[1][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[1][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[1][3] += 1;
                                        break;

                                    case "03":
                                        haringey_year[2][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[2][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[2][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[2][3] += 1;
                                        break;

                                    case "04":
                                        haringey_year[3][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[3][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[3][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[3][3] += 1;
                                        break;

                                    case "05":
                                        haringey_year[4][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[4][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[4][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[4][3] += 1;
                                        break;

                                    case "06":
                                        haringey_year[5][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[5][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[5][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[5][3] += 1;
                                        break;

                                    case "07":
                                        haringey_year[6][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[6][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[6][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[6][3] += 1;
                                        break;

                                    case "08":
                                        haringey_year[7][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[7][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[7][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[7][3] += 1;
                                        break;

                                    case "09":
                                        haringey_year[8][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[8][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[8][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[8][3] += 1;
                                        break;

                                    case "10":
                                        haringey_year[9][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[9][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[9][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[9][3] += 1;
                                        break;

                                    case "11":
                                        haringey_year[10][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[10][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[10][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[10][3] += 1;
                                        break;

                                    case "12":
                                        haringey_year[11][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        haringey_year[11][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        haringey_year[11][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        haringey_year[11][3] += 1;
                                        break;
                                }


                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), islingtionArea, b)) {

                                switch (time) {

                                    case "01":
                                        islington_year[0][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[0][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[0][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[0][3] += 1;
                                        break;

                                    case "02":
                                        islington_year[1][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[1][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[1][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[1][3] += 1;
                                        break;

                                    case "03":
                                        islington_year[2][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[2][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[2][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[2][3] += 1;
                                        break;

                                    case "04":
                                        islington_year[3][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[3][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[3][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[3][3] += 1;
                                        break;

                                    case "05":
                                        islington_year[4][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[4][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[4][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[4][3] += 1;
                                        break;

                                    case "06":
                                        islington_year[5][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[5][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[5][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[5][3] += 1;
                                        break;

                                    case "07":
                                        islington_year[6][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[6][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[6][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[6][3] += 1;
                                        break;

                                    case "08":
                                        islington_year[7][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[7][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[7][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[7][3] += 1;
                                        break;

                                    case "09":
                                        islington_year[8][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[8][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[8][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[8][3] += 1;
                                        break;

                                    case "10":
                                        islington_year[9][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[9][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[9][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[9][3] += 1;
                                        break;

                                    case "11":
                                        islington_year[10][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[10][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[10][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[10][3] += 1;
                                        break;

                                    case "12":
                                        islington_year[11][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        islington_year[11][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        islington_year[11][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        islington_year[11][3] += 1;
                                        break;
                                }

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), westminsterArea, b)) {

                                switch (time) {

                                    case "01":
                                        westminster_year[0][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[0][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[0][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[0][3] += 1;
                                        break;

                                    case "02":
                                        westminster_year[1][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[1][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[1][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[1][3] += 1;
                                        break;

                                    case "03":
                                        westminster_year[2][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[2][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[2][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[2][3] += 1;
                                        break;

                                    case "04":
                                        westminster_year[3][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[3][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[3][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[3][3] += 1;
                                        break;

                                    case "05":
                                        westminster_year[4][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[4][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[4][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[4][3] += 1;
                                        break;

                                    case "06":
                                        westminster_year[5][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[5][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[5][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[5][3] += 1;
                                        break;

                                    case "07":
                                        westminster_year[6][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[6][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[6][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[6][3] += 1;
                                        break;

                                    case "08":
                                        westminster_year[7][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[7][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[7][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[7][3] += 1;
                                        break;

                                    case "09":
                                        westminster_year[8][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[8][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[8][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[8][3] += 1;
                                        break;

                                    case "10":
                                        westminster_year[9][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[9][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[9][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[9][3] += 1;
                                        break;

                                    case "11":
                                        westminster_year[10][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[10][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[10][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[10][3] += 1;
                                        break;

                                    case "12":
                                        westminster_year[11][0] += resultData.getResult().get(i)
                                                .getGas1_value();
                                        westminster_year[11][1] += resultData.getResult().get(i)
                                                .getGas2_value();
                                        westminster_year[11][2] += resultData.getResult().get(i)
                                                .getGas3_value();
                                        westminster_year[11][3] += 1;
                                        break;
                                }
                            }
                        }
                        addMonthData();
                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResultResponse> call, Throwable t) {
                    Log.d("onFailure 1", t.toString());

                }
            });

        } else if (graphNumber == 1) {
            Call<ResultResponse> call = service.getPollutionByMonthYear(mon, yr);
            call.enqueue(new Callback<ResultResponse>() {
                @Override
                public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                    try {
                        ResultResponse resultData = response.body();
                        for (int i = 0; i < resultData.getResult().size(); i++) {
                            testValue = String.valueOf(resultData.getResult().get(i).getGas1_value()); //value used for testing whether some data has been received
                            time = resultData.getResult().get(i).getTime_value();
                            if (PolyUtil.containsLocation(new LatLng(resultData.getResult().get(i)
                                    .getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), camdenArea, b)) {
                                camden_gas[0] += resultData.getResult().get(i).getGas1_value();
                                camden_gas[1] += resultData.getResult().get(i).getGas2_value();
                                camden_gas[2] += resultData.getResult().get(i).getGas3_value();
                                camden_gas[3] += 1;

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), hackneyArea, b)) {
                                hackney_gas[0] += resultData.getResult().get(i).getGas1_value();
                                hackney_gas[1] += resultData.getResult().get(i).getGas2_value();
                                hackney_gas[2] += resultData.getResult().get(i).getGas3_value();
                                hackney_gas[3] += 1;

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), haringeyArea, b)) {
                                haringey_gas[0] += resultData.getResult().get(i).getGas1_value();
                                haringey_gas[1] += resultData.getResult().get(i).getGas2_value();
                                haringey_gas[2] += resultData.getResult().get(i).getGas3_value();
                                haringey_gas[3] += 1;

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), islingtionArea, b)) {
                                islington_gas[0] += resultData.getResult().get(i).getGas1_value();
                                islington_gas[1] += resultData.getResult().get(i).getGas2_value();
                                islington_gas[2] += resultData.getResult().get(i).getGas3_value();
                                islington_gas[3] += 1;

                            } else if (PolyUtil.containsLocation(new LatLng(resultData.getResult()
                                    .get(i).getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), westminsterArea, b)) {
                                westminster_gas[0] += resultData.getResult().get(i).getGas1_value();
                                westminster_gas[1] += resultData.getResult().get(i).getGas2_value();
                                westminster_gas[2] += resultData.getResult().get(i).getGas3_value();
                                westminster_gas[3] += 1;
                            }
                        }
                        addBoroughData();

                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResultResponse> call, Throwable t) {
                    Log.d("onFailure 2", t.toString());
                }
            });


        } else if (graphNumber == 2) {
            Call<ResultResponse> call = service.getPollutionByYear(yr);
            call.enqueue(new Callback<ResultResponse>() {
                @Override
                public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                    ArrayList<WeightedLatLng> locations = new ArrayList<>();
                    try {
                        ResultResponse resultData = response.body();
                        for (int i = 0; i < resultData.getResult().size(); i++) {
                            locations.add(new WeightedLatLng(new LatLng(resultData.getResult().get(i)
                                    .getLocation_lat_value(), resultData.getResult().get(i)
                                    .getLocation_lng_value()), resultData.getResult().get(i)
                                    .getGas1_value() + resultData.getResult().get(i)
                                    .getGas2_value() + resultData.getResult().get(i).getGas3_value())); //add pollution data to the map
                        }

                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }

                    if (locations.isEmpty()) //uf no results
                    {
                        MapFragment.mOverlay.clearTileCache();
                        locations.add(new WeightedLatLng(new LatLng(0, -0), 0)); //empty point
                        MapFragment.mProvider.setWeightedData(locations); //set the data to the map


                    } else {
                        MapFragment.mOverlay.clearTileCache();
                        MapFragment.mProvider.setWeightedData(locations); //set the data to the map
                    }

                }


                @Override
                public void onFailure(Call<ResultResponse> call, Throwable t) {
                    Log.d("onFailure 3", t.toString());
                }
            });


        } else if (graphNumber == 3) {
            Call<ResultResponse> call = service.getPollutionByMonthDayYear(mon, day, yr);
            call.enqueue(new Callback<ResultResponse>() {
                @Override
                public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                    try {
                        ResultResponse resultData = response.body();
                        for (int i = 0; i < resultData.getResult().size(); i++) {
                            time = resultData.getResult().get(i).getTime_value();
                            compDataTimeDate1.add(resultData.getResult().get(i).getTime_value());
                            compDataFirstGasDate1.add(String.valueOf(resultData.getResult()
                                    .get(i).getGas1_value()));
                            compDataSecondGasDate1.add(String.valueOf(resultData.getResult()
                                    .get(i).getGas2_value()));
                            compDataThirdGasDate1.add(String.valueOf(resultData.getResult()
                                    .get(i).getGas3_value()));
                        }
                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResultResponse> call, Throwable t) {
                    Log.d("onFailure 4", t.toString());
                }
            });


        } else if (graphNumber == 4) {
            Call<ResultResponse> call = service.getPollutionByMonthDayYear(mon, day, yr);
            call.enqueue(new Callback<ResultResponse>() {
                @Override
                public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                    try {
                        ResultResponse resultData = response.body();
                        for (int i = 0; i < resultData.getResult().size(); i++) {
                            time = resultData.getResult().get(i).getTime_value();
                            compDataTimeDate2.add(resultData.getResult().get(i).getTime_value());
                            compDataFirstGasDate2.add(String.valueOf(resultData.getResult()
                                    .get(i).getGas1_value()));
                            compDataSecondGasDate2.add(String.valueOf(resultData.getResult()
                                    .get(i).getGas2_value()));
                            compDataThirdGasDate2.add(String.valueOf(resultData.getResult()
                                    .get(i).getGas3_value()));
                        }
                        addComparisonData(); //create chart
                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResultResponse> call, Throwable t) {
                    Log.d("onFailure 5", t.toString());
                }
            });


        } else if (graphNumber == 5) {


            Call<ResultResponse> call = service.getPollutionAllData();
            call.enqueue(new Callback<ResultResponse>() {
                @Override
                public void onResponse(Call<ResultResponse> call, Response<ResultResponse> response) {
                    try {
                        ResultResponse resultData = response.body();
                        for (int i = 0; i < resultData.getResult().size(); i++) {
                            time = resultData.getResult().get(i).getTime_value();
                            String[] str = time.split("-");
                            if (!listContains(avYears, str[0])) {
                                avYears.add(str[0]);
                            }
                        }
                        addYearValues(avYears); //add year values to the spinner
                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResultResponse> call, Throwable t) {
                    Log.d("onFailure 6", t.toString());
                }
            });
        }
    }

    private boolean listContains(ArrayList<String> array, String str) {
        for (String a : array) {
            if (a.equals(str)) return true;
        }

        return false;
    }

    private void addYearValues(ArrayList<String> array) {
        Spinner spin = (Spinner) getActivity().findViewById(R.id.yearSpinner);
        int it = R.layout.spinner_item;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                it, array);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spin.setAdapter(adapter);
    }

    //comparison graph
    private void addComparisonData() {
        //if selected dates are not the same, can be added to prevent from showing one date, but sometimes is useful to show only one date, hence commented
        // if(!((compSpinnerFDay.getSelectedItemPosition() == compSpinnerSDay.getSelectedItemPosition()) && (compSpinnerFMonth.getSelectedItemPosition() == compSpinnerSMonth.getSelectedItemPosition()))){
        XYMultipleSeriesDataset chartDataSet;
        XYMultipleSeriesRenderer chartRendering;
        GraphicalView chartGraphicalView = null;

        if (timeAxisOne != null) {
            for (int i = 0; i < timeAxisOne.getItemCount(); i++) {
                timeAxisOne.remove(i);
            }
        }

        if (timeAxisTwo != null) {
            for (int i = 0; i < timeAxisTwo.getItemCount(); i++) {
                timeAxisTwo.remove(i);
            }
        }

        chartDataSet = new XYMultipleSeriesDataset();
        chartRendering = new XYMultipleSeriesRenderer();
        chartRendering.setAxisTitleTextSize(21);
        chartRendering.setLabelsTextSize(25);
        chartRendering.setLegendTextSize(20);
        chartRendering.setAxesColor(Color.BLACK);
        chartRendering.setLabelsColor(Color.BLACK);
        chartRendering.setXLabelsColor(Color.BLACK);
        chartRendering.setYLabelsColor(0, Color.BLACK);
        chartRendering.setAntialiasing(true);
        chartRendering.setFitLegend(true);
        chartRendering.setXTitle("(Time)");
        chartRendering.setYTitle("(Parts Per Million)");
        chartRendering.setLegendTextSize(20);
        chartRendering.setPointSize(6f);
        timeAxisOne = new TimeSeries("First");
        timeAxisTwo = new TimeSeries("Second");
        chartRendering.setPanEnabled(false);

        XYSeriesRenderer firstRender = new XYSeriesRenderer();
        firstRender.setColor(Color.rgb(54, 54, 54));
        firstRender.setPointStyle(PointStyle.CIRCLE);
        firstRender.setFillPoints(true);
        firstRender.setLineWidth(3f);
        firstRender.setDisplayChartValues(true);
        firstRender.setChartValuesTextSize(35f);
        firstRender.setChartValuesSpacing(8f);
        firstRender.setDisplayChartValuesDistance(3);
        chartRendering.addSeriesRenderer(0, firstRender);

        XYSeriesRenderer secondRender = new XYSeriesRenderer();
        secondRender.setColor(Color.rgb(54, 54, 54));
        secondRender.setPointStyle(PointStyle.TRIANGLE);
        secondRender.setFillPoints(true);
        secondRender.setLineWidth(3f);
        secondRender.setDisplayChartValues(true);
        secondRender.setChartValuesTextSize(35f);
        secondRender.setChartValuesSpacing(8f);
        secondRender.setDisplayChartValuesDistance(3);
        chartRendering.addSeriesRenderer(1, secondRender);

        chartRendering.setClickEnabled(false);
        chartRendering.setPanEnabled(true, true);
        chartRendering.setApplyBackgroundColor(false);
        chartRendering.setMarginsColor(Color.argb(0, 144, 195, 212));

        XYSeriesRenderer.FillOutsideLine fill = new XYSeriesRenderer
                .FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ABOVE);
        fill.setColor(Color.argb(150, 70, 126, 119));
        firstRender.addFillOutsideLine(fill);

        XYSeriesRenderer.FillOutsideLine fill2 = new XYSeriesRenderer
                .FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BOUNDS_ABOVE);
        fill2.setColor(Color.argb(150, 21, 82, 81));
        secondRender.addFillOutsideLine(fill2);

        try {
            fillData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        chartDataSet.addSeries(0, timeAxisOne);
        chartDataSet.addSeries(1, timeAxisTwo);

        chartRendering.setYAxisMin(timeAxisOne.getMinY() - 2);

        //increase the initial view area of the graph by increasing the maxY value by 5
        if (timeAxisOne.getMaxY() > timeAxisTwo.getMaxY()) {
            chartRendering.setYAxisMax(timeAxisTwo.getMaxY() + 5);
        } else {
            chartRendering.setYAxisMax(timeAxisTwo.getMaxY() + 5);
        }

        if (getContext() != null)
            chartGraphicalView = ChartFactory.getTimeChartView(getContext(),
                    chartDataSet, chartRendering, "HH:mm:ss");

        comparisonLayout.removeAllViews();
        comparisonLayout.addView(chartGraphicalView);
    }

    //first graph
    private void addMonthData() {

        if (testValue2 != null && !testValue2.isEmpty() && firstBarChart.isEmpty()) {

            for (int i = 0; i < 12; i++) {
                xAxisValues2.add(Constants.MONTHS_NAME[i % Constants.MONTHS_NAME.length]);
            }

            if (bspinner.getSelectedItem().toString().equals("Camden")) {

                for (int i = 0; i < 12; i++) {
                    yAxisValues2.add(new BarEntry(new float[]{(camden_year[i][0]) /
                            (camden_year[i][3]), (camden_year[i][1]) / (camden_year[i][3]),
                            (camden_year[i][2]) / (camden_year[i][3])}, i));
                }

            } else if (bspinner.getSelectedItem().toString().equals("Hackney")) {

                for (int i = 0; i < 12; i++) {
                    yAxisValues2.add(new BarEntry(new float[]{(hackney_year[i][0]) /
                            (hackney_year[i][3]), (hackney_year[i][1]) / (hackney_year[i][3]),
                            (hackney_year[i][2]) / (hackney_year[i][3])}, i));
                }

            } else if (bspinner.getSelectedItem().toString().equals("Haringey")) {

                for (int i = 0; i < 12; i++) {
                    yAxisValues2.add(new BarEntry(new float[]{(haringey_year[i][0]) /
                            (haringey_year[i][3]), (haringey_year[i][1]) / (haringey_year[i][3]),
                            (haringey_year[i][2]) / (haringey_year[i][3])}, i));
                }

            } else if (bspinner.getSelectedItem().toString().equals("Islington")) {

                for (int i = 0; i < 12; i++) {
                    yAxisValues2.add(new BarEntry(new float[]{(islington_year[i][0]) /
                            (islington_year[i][3]), (islington_year[i][1]) / (islington_year[i][3]),
                            (islington_year[i][2]) / (islington_year[i][3])}, i));
                }

            } else if (bspinner.getSelectedItem().toString().equals("Westminster")) {

                for (int i = 0; i < 12; i++) {
                    yAxisValues2.add(new BarEntry(new float[]{(westminster_year[i][0]) /
                            (westminster_year[i][3]), (westminster_year[i][1]) /
                            (westminster_year[i][3]), (westminster_year[i][2]) /
                            (westminster_year[i][3])}, i));
                }
            }

            ArrayList<Integer> coloursArr = new ArrayList<>();

            for (int c : Constants.DARK_COLOURS)
                coloursArr.add(c);

            BarDataSet barChartSet = new BarDataSet(yAxisValues2, "");
            barChartSet.setColors(coloursArr);
            barChartSet.setValueTextColor(Color.WHITE);
            barChartSet.setStackLabels(new String[]{"Carbon Monoxide", "Methane", "Liquefied Gas"});
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barChartSet);
            BarData theChartData = new BarData(xAxisValues2, dataSets);
            theChartData.setValueFormatter(new MyValueFormatter());
            firstBarChart.setData(theChartData);
            firstBarChart.invalidate();
            testValue2 = null;
            for (float[] row : camden_year)
                Arrays.fill(row, 0);
            for (float[] row2 : hackney_year)
                Arrays.fill(row2, 0);
            for (float[] row3 : haringey_year)
                Arrays.fill(row3, 0);
            for (float[] row4 : islington_year)
                Arrays.fill(row4, 0);
            for (float[] row5 : westminster_year)
                Arrays.fill(row5, 0);

        }

    }


    //second graph
    private void addBoroughData() {

        if (testValue != null && !testValue.isEmpty() && secondBarChart.isEmpty()) {

            for (int i = 0; i < 5; i++) {
                xAxisValues.add(Constants.BOROUGHS_NAME[i % Constants.BOROUGHS_NAME.length]);
            }

            yAxisValues.add(new BarEntry(new float[]{(camden_gas[0]) / (camden_gas[3]),
                    (camden_gas[1]) / (camden_gas[3]), (camden_gas[2]) / (camden_gas[3])}, 0));
            yAxisValues.add(new BarEntry(new float[]{(hackney_gas[0]) / (hackney_gas[3]),
                    (hackney_gas[1]) / (hackney_gas[3]), (hackney_gas[2]) / (hackney_gas[3])}, 1));
            yAxisValues.add(new BarEntry(new float[]{(haringey_gas[0]) / (haringey_gas[3]),
                    (haringey_gas[1]) / (haringey_gas[3]), (haringey_gas[2]) / (haringey_gas[3])}, 2));
            yAxisValues.add(new BarEntry(new float[]{(islington_gas[0]) / (islington_gas[3]),
                    (islington_gas[1]) / (islington_gas[3]), (islington_gas[2]) / (islington_gas[3])}, 3));
            yAxisValues.add(new BarEntry(new float[]{(westminster_gas[0]) / (westminster_gas[3]),
                    (westminster_gas[1]) / (westminster_gas[3]),
                    (westminster_gas[2]) / (westminster_gas[3])}, 4));

            ArrayList<Integer> colours = new ArrayList<>();

            for (int c : Constants.DARK_COLOURS)
                colours.add(c);

            BarDataSet barChartSet = new BarDataSet(yAxisValues, "");
            barChartSet.setColors(colours);
            barChartSet.setValueTextColor(Color.WHITE);
            barChartSet.setStackLabels(new String[]{"Carbon Monoxide", "Methane", "Liquefied Gas"});
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barChartSet);
            BarData theBarChartData = new BarData(xAxisValues, dataSets);
            theBarChartData.setValueFormatter(new MyValueFormatter());
            secondBarChart.setData(theBarChartData);
            secondBarChart.invalidate();
            testValue = null;
            Arrays.fill(camden_gas, 0);
            Arrays.fill(hackney_gas, 0);
            Arrays.fill(haringey_gas, 0);
            Arrays.fill(islington_gas, 0);
            Arrays.fill(westminster_gas, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //fill in arrays from XML files (coordinates defining London areas)
        readArea(getResources().getStringArray(R.array.camden), camdenArea);
        readArea(getResources().getStringArray(R.array.hackney), hackneyArea);
        readArea(getResources().getStringArray(R.array.haringey), haringeyArea);
        readArea(getResources().getStringArray(R.array.islington), islingtionArea);
        readArea(getResources().getStringArray(R.array.westminster), westminsterArea);
        //clear arrays, so no duplicates when oncreateview refreshes
        Arrays.fill(camden_gas, 0);
        Arrays.fill(hackney_gas, 0);
        Arrays.fill(haringey_gas, 0);
        Arrays.fill(islington_gas, 0);
        Arrays.fill(westminster_gas, 0);
        for (float[] row : camden_year)
            Arrays.fill(row, 0);
        for (float[] row2 : hackney_year)
            Arrays.fill(row2, 0);
        for (float[] row3 : haringey_year)
            Arrays.fill(row3, 0);
        for (float[] row4 : islington_year)
            Arrays.fill(row4, 0);
        for (float[] row5 : westminster_year)
            Arrays.fill(row5, 0);

        MapFragment.locations.add(new WeightedLatLng(new LatLng(51.527969, -0.102267), 0.1)); //city university // adding random location fixes Error inflating class fragment

        View rootView = inflater.inflate(R.layout.fragment_fragment2, container, false);


        comparisonLayout = (FrameLayout) rootView.findViewById(R.id.dayComparisonLayout);
        final Button compBtn = (Button) rootView.findViewById(R.id.compareBtn);

        yearspinner = (Spinner) rootView.findViewById(R.id.yearSpinner);
        ArrayAdapter adapter0; //year spiner adapter
        adapter0 = ArrayAdapter.createFromResource(getContext(), R.array.year, R.layout.spinner_item);
        adapter0.setDropDownViewResource(R.layout.spinner_dropdown_item);
        yearspinner.setAdapter(adapter0);
        yearspinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {

                        testValue = null;
                        secondBarChart.clear();
                        yAxisValues.clear();
                        xAxisValues.clear();
                        secondBarChart.invalidate();
                        testValue2 = null;
                        firstBarChart.clear();
                        yAxisValues2.clear();
                        xAxisValues2.clear();
                        firstBarChart.invalidate();
                        Arrays.fill(camden_gas, 0);
                        Arrays.fill(hackney_gas, 0);
                        Arrays.fill(haringey_gas, 0);
                        Arrays.fill(islington_gas, 0);
                        Arrays.fill(westminster_gas, 0);
                        for (float[] row : camden_year)
                            Arrays.fill(row, 0);
                        for (float[] row2 : hackney_year)
                            Arrays.fill(row2, 0);
                        for (float[] row3 : haringey_year)
                            Arrays.fill(row3, 0);
                        for (float[] row4 : islington_year)
                            Arrays.fill(row4, 0);
                        for (float[] row5 : westminster_year)
                            Arrays.fill(row5, 0);
                        getArduinoData(0, yearspinner.getSelectedItem().toString(), "", ""); //fetch the data from the server
                        getArduinoData(1, yearspinner.getSelectedItem().toString(),
                                String.valueOf(Utils.getMonth(mspinner.getSelectedItem().toString())), ""); //fetch the data from the server
                        getArduinoData(2, yearspinner.getSelectedItem().toString(), "", ""); //fetch the data from the server (map)
                        compBtn.callOnClick(); //fetch data when year changes by simulating onclick event on button
                        firstBarChart.animateXY(2000, 2000);
                        secondBarChart.animateXY(2000, 2000);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        bspinner = (Spinner) rootView.findViewById(R.id.firstSpinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.borough, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        bspinner.setAdapter(adapter);
        bspinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        testValue2 = null;
                        firstBarChart.clear();
                        yAxisValues2.clear();
                        xAxisValues2.clear();
                        firstBarChart.invalidate();
                        for (float[] row : camden_year)
                            Arrays.fill(row, 0);
                        for (float[] row2 : hackney_year)
                            Arrays.fill(row2, 0);
                        for (float[] row3 : haringey_year)
                            Arrays.fill(row3, 0);
                        for (float[] row4 : islington_year)
                            Arrays.fill(row4, 0);
                        for (float[] row5 : westminster_year)
                            Arrays.fill(row5, 0);
                        getArduinoData(0, yearspinner.getSelectedItem().toString(), "", ""); //fetch the data from the server
                        firstBarChart.animateXY(2000, 2000);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(getContext(),
                R.array.month_array, R.layout.spinner_item);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mspinner = (Spinner) rootView.findViewById(R.id.secondSpinner);
        mspinner.setAdapter(adapter2);
        mspinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        testValue = null;
                        secondBarChart.clear();
                        yAxisValues.clear();
                        xAxisValues.clear();
                        secondBarChart.invalidate();
                        Arrays.fill(camden_gas, 0);
                        Arrays.fill(hackney_gas, 0);
                        Arrays.fill(haringey_gas, 0);
                        Arrays.fill(islington_gas, 0);
                        Arrays.fill(westminster_gas, 0);
                        getArduinoData(1, yearspinner.getSelectedItem().toString(),
                                String.valueOf(Utils.getMonth(mspinner.getSelectedItem().toString())), ""); //fetch the data from the server
                        secondBarChart.animateXY(2000, 2000);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        ArrayAdapter adapter3 = ArrayAdapter.createFromResource(getContext(),
                R.array.day_array, R.layout.spinner_item);
        adapter3.setDropDownViewResource(R.layout.spinner_dropdown_item);
        compSpinnerFDay = (Spinner) rootView.findViewById(R.id.firstDaySpinnerDay);
        compSpinnerFDay.setAdapter(adapter3);

        ArrayAdapter adapter4 = ArrayAdapter.createFromResource(getContext(),
                R.array.month_array, R.layout.spinner_item);
        adapter4.setDropDownViewResource(R.layout.spinner_dropdown_item);
        compSpinnerFMonth = (Spinner) rootView.findViewById(R.id.firstDaySpinnerMonth);
        compSpinnerFMonth.setAdapter(adapter4);

        ArrayAdapter adapter5 = ArrayAdapter.createFromResource(getContext(),
                R.array.day_array, R.layout.spinner_item);
        adapter5.setDropDownViewResource(R.layout.spinner_dropdown_item);
        compSpinnerSDay = (Spinner) rootView.findViewById(R.id.secondDaySpinnerDay);
        compSpinnerSDay.setAdapter(adapter5);

        ArrayAdapter adapter6 = ArrayAdapter.createFromResource(getContext(),
                R.array.month_array, R.layout.spinner_item);
        adapter6.setDropDownViewResource(R.layout.spinner_dropdown_item);
        compSpinnerSMonth = (Spinner) rootView.findViewById(R.id.secondDaySpinnerMonth);
        compSpinnerSMonth.setAdapter(adapter6);

        //setting the current year and month for spinners
        Calendar cal = Calendar.getInstance();
        bspinner.setSelection(Utils.getYear(new SimpleDateFormat("yyyy").format(cal.getTime())) - 1); //minus 1 because position starts from  0
        mspinner.setSelection(Utils.getMonth(new SimpleDateFormat("MMM").format(cal.getTime())) - 1); //minus 1 because position starts from  0
        compSpinnerFDay.setSelection(Utils.getDay(new SimpleDateFormat("dd").format(cal.getTime())) - 1); //minus 1 because position starts from  0
        compSpinnerFMonth.setSelection(Utils.getMonth(new SimpleDateFormat("MMM").format(cal.getTime())) - 1); //minus 1 because position starts from  0
        compSpinnerSDay.setSelection(Utils.getDay(new SimpleDateFormat("dd").format(cal.getTime())) - 1); //minus 1 because position starts from  0
        compSpinnerSMonth.setSelection(Utils.getMonth(new SimpleDateFormat("MMM").format(cal.getTime())) - 1); //minus 1 because position starts from  0

        //button listener
        compBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove data from arrays
                compDataTimeDate1.clear();
                compDataTimeDate2.clear();
                compDataFirstGasDate1.clear();
                compDataFirstGasDate2.clear();
                compDataSecondGasDate1.clear();
                compDataSecondGasDate2.clear();
                compDataThirdGasDate1.clear();
                compDataThirdGasDate2.clear();
                //   System.out.println(Constants.host + "get_pollution_by_day.php?month=" + getMonth(compSpinnerFMonth.getSelectedItem().toString()) + "&day=" + compSpinnerFDay.getSelectedItem().toString() + "&year=" + getYear(yearspinner.getSelectedItem().toString())); //fetch the data from the server (comparison first date)
                getArduinoData(3, yearspinner.getSelectedItem().toString(),
                        String.valueOf(Utils.getMonth(compSpinnerFMonth.getSelectedItem().toString())),
                        compSpinnerFDay.getSelectedItem().toString()); //fetch the data from the server (comparison first date)
                getArduinoData(4, yearspinner.getSelectedItem().toString(),
                        String.valueOf(Utils.getMonth(compSpinnerSMonth.getSelectedItem().toString())),
                        compSpinnerSDay.getSelectedItem().toString()); //fetch the data from the server (comparison second date)
            }
        });

        //initialise radiobuttons
        CO_radio = (RadioButton) rootView.findViewById(R.id.CO_radio);
        CH4_radio = (RadioButton) rootView.findViewById(R.id.CH4_radio);
        LPG_radio = (RadioButton) rootView.findViewById(R.id.LPG_radio);

        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                addComparisonData();
            }
        });

        FrameLayout monthLayout = (FrameLayout) rootView.findViewById(R.id.monthLayout);
        firstBarChart = new BarChart(getContext());

        //add pie chart to mainLayout
        monthLayout.addView(firstBarChart, -1);
        monthLayout.setBackgroundColor(Color.rgb(78, 175, 156));
        firstBarChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        firstBarChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        firstBarChart.setPinchZoom(false);
        firstBarChart.animateXY(2000, 2000);
        firstBarChart.setDrawGridBackground(false);
        firstBarChart.setDrawBarShadow(false);
        firstBarChart.setDrawValueAboveBar(false);

        //  addMonthData();
        // change the position of the y-labels
        YAxis yAxisLabel = firstBarChart.getAxisLeft();
        yAxisLabel.setValueFormatter(new MyYAxisValueFormatter());
        firstBarChart.getAxisRight().setEnabled(false);

        XAxis xAxisLabel = firstBarChart.getXAxis();
        xAxisLabel.setPosition(XAxis.XAxisPosition.TOP);
        xAxisLabel.setDrawGridLines(false);
        yAxisLabel.setDrawGridLines(false);

        // setting data
        Legend chartLegend = firstBarChart.getLegend();
        chartLegend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        chartLegend.setFormSize(8f);
        chartLegend.setFormToTextSpace(4f);
        chartLegend.setXEntrySpace(6f);

        FrameLayout boroughLayout = (FrameLayout) rootView.findViewById(R.id.boroughLayout);
        secondBarChart = new BarChart(getContext());

        //add pie chart to mainLayout
        boroughLayout.addView(secondBarChart, -1);
        boroughLayout.setBackgroundColor(Color.rgb(78, 175, 156));
        secondBarChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        secondBarChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        secondBarChart.setPinchZoom(false);
        secondBarChart.animateXY(2000, 2000);
        secondBarChart.setDrawGridBackground(false);
        secondBarChart.setDrawBarShadow(false);
        secondBarChart.setDrawValueAboveBar(false);

        // change the position of the y-labels
        YAxis yAxisSecondLabel = secondBarChart.getAxisLeft();
        yAxisSecondLabel.setValueFormatter(new MyYAxisValueFormatter());
        secondBarChart.getAxisRight().setEnabled(false);

        XAxis xAxisSecondLabel = secondBarChart.getXAxis();
        xAxisSecondLabel.setPosition(XAxis.XAxisPosition.TOP);
        xAxisSecondLabel.setDrawGridLines(false);
        yAxisSecondLabel.setDrawGridLines(false);

        // setting data
        Legend secondChartLegend = secondBarChart.getLegend();
        secondChartLegend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        secondChartLegend.setFormSize(8f);
        chartLegend.setFormToTextSpace(4f);
        secondChartLegend.setXEntrySpace(6f);

        getArduinoData(2, yearspinner.getSelectedItem().toString(), "", ""); //fetch the data from the server (map)
        getArduinoData(5, "", "", ""); //get the available years in database

        compBtn.callOnClick(); //call on click to get the data of the comparison chart (not necessary however when not called would leave blank space without any graph untill a button is pressed to get the data, this will add an outline of a graph if there is no data or get the actual data if its available)

        return rootView;
    }

    private void fillData() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        //workaround in order to keep two graphs overlying for easy comparison between two dates (otherwise graphs are not overlaye because of the dates, but by keeping same date only differnt times it overlays nicely)
        //adding in data from first date (getting CO data first)
        for (int i = 0; i < compDataTimeDate1.size(); i++) {
            String formattedTime = "";
            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            calendar.setTime(formatDate.parse(compDataTimeDate1.get(i)));
            Date date = formatDate.parse(compDataTimeDate1.get(i));

            SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss");
            formattedTime = output.format(date);

            Date date2 = formatDate.parse("1970-01-01 " + formattedTime);

            if (CO_radio.isChecked()) {
                timeAxisOne.add(date2, Float.parseFloat(compDataFirstGasDate1.get(i)));
            } else if (CH4_radio.isChecked()) {
                timeAxisOne.add(date2, Float.parseFloat(compDataSecondGasDate1.get(i)));
            } else if (LPG_radio.isChecked()) {
                timeAxisOne.add(date2, Float.parseFloat(compDataThirdGasDate1.get(i)));
            }

        }
        //adding in data from first date (getting CO data first)
        for (int i = 0; i < compDataTimeDate2.size(); i++) {
            String formattedTime = "";
            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            calendar.setTime(formatDate.parse(compDataTimeDate2.get(i)));
            Date date = formatDate.parse(compDataTimeDate2.get(i));

            SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss");
            formattedTime = output.format(date);

            Date date2 = formatDate.parse("1970-01-01 " + formattedTime);

            if (CO_radio.isChecked()) {
                timeAxisTwo.add(date2, Float.parseFloat(compDataFirstGasDate2.get(i)));
            } else if (CH4_radio.isChecked()) {
                timeAxisTwo.add(date2, Float.parseFloat(compDataSecondGasDate2.get(i)));
            } else if (LPG_radio.isChecked()) {
                timeAxisTwo.add(date2, Float.parseFloat(compDataThirdGasDate2.get(i)));
            }
        }

    }
}