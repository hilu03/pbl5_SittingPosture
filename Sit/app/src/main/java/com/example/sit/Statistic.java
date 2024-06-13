package com.example.sit;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Statistic extends BaseActivity {

    int correct, head_down, head_right, head_left, leaning_right, leaning_left, right_hand_up, left_hand_up;
    OkHttpClient client;
    Gson gson;
    private DatePickerDialog datePickerDialog;
    private Button dateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        client = new OkHttpClient();
        gson = new Gson();


        initDatePicker();
        dateButton = findViewById(R.id.datePickerButton);
        dateButton.setText(getTodaysDate());

//        statistic();
//        drawBarChart(5,10,10,10,10,100,10,10);
//        drawPieChart(20, 80);

        Home1();
        Play();

    }

    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                dateButton.setText(date);

                statistic(day, month, year);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

    }

    private String makeDateString(int day, int month, int year)
    {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        //default should never happen
        return "JAN";
    }

    public void openDatePicker(View view)
    {
        datePickerDialog.show();
    }


    private void statistic(int day, int month, int year) {
        // Flask
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("day",day);
            jsonObject.put("month", month);
            jsonObject.put("year", year);

            String json = gson.toJson(jsonObject);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

            // singleton IP
            String ipAddress = IPAddressSingleton.getInstance().getIPAddress();
            String url = "http://" + ipAddress + ":5000/statistic";
            Request request = new Request.Builder()
//                    .url("http://172.20.10.2:5000/statistic")
                    .url(url)
                    .post(requestBody) // Specify POST method here
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Statistic.this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string(); // lấy file json được gửi từ flask
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);

                            correct = jsonResponse.getInt("correct");
                            head_down = jsonResponse.getInt("head_down");
                            head_right = jsonResponse.getInt("head_right");
                            head_left = jsonResponse.getInt("head_left");
                            leaning_right = jsonResponse.getInt("leaning_right");
                            leaning_left = jsonResponse.getInt("leaning_left");
                            right_hand_up = jsonResponse.getInt("right_hand_up");
                            left_hand_up = jsonResponse.getInt("left_hand_up");

                            int sum = correct + head_down + head_right + head_left
                                    + leaning_left + leaning_right + right_hand_up + left_hand_up;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (sum == 0) {
                                        drawBarChart(0,0,0,0,0,0,0,0);
                                        drawPieChart(0,0);
                                    }
                                    else {
                                        drawBarChart(correct * 100 / sum, head_down * 100 / sum, head_right * 100 / sum,
                                                head_left * 100 / sum, leaning_right * 100 / sum, leaning_left * 100 / sum,
                                                right_hand_up * 100 / sum, left_hand_up * 100 / sum);

                                        drawPieChart(correct * 100 / sum, 100 - correct * 100 / sum);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // barChart
//    private void drawBarChart(int correct, int headDown, int headRight, int headLeft,
//                              int leaningRight, int leaningLeft, int rightHandUp, int leftHandUp) {
////        barChartView.invalidate();
//        AnyChartView barChartView = findViewById(R.id.barChart);
//
//        List<DataEntry> dataBAR = new ArrayList<>();
//
//        dataBAR.add(new ValueDataEntry("correct", correct));
//        dataBAR.add(new ValueDataEntry("head down", headDown));
//        dataBAR.add(new ValueDataEntry("head right", headRight));
//        dataBAR.add(new ValueDataEntry("head left", headLeft));
//        dataBAR.add(new ValueDataEntry("leaning right", leaningRight));
//        dataBAR.add(new ValueDataEntry("leaning left", leaningLeft));
//        dataBAR.add(new ValueDataEntry("right hand up", rightHandUp));
//        dataBAR.add(new ValueDataEntry("left hand up", leftHandUp));
//
//        Cartesian columnChart = AnyChart.column();
////        columnChart.palette(new String[]{"#3AA3A5"});
//        columnChart.xAxis(0).labels().rotation(-45);
//
//        columnChart.data(dataBAR);
//
//        // Định dạng trục y thành phần trăm
//        columnChart.yScale().minimum(0);
//        columnChart.yScale().maximum(100);
//
//        // Định dạng trục x là nhãn
//        columnChart.xAxis(0).title("Posture");
//        columnChart.yAxis(0).title("Percent");
//
//
//        // Định dạng tooltip
//        columnChart.tooltip()
//                .titleFormat("{%X}: {%Value}%")
//                .position(Position.CENTER_BOTTOM)
//                .anchor(Anchor.CENTER_BOTTOM)
//                .offsetX(0d)
//                .offsetY(5d)
//                .positionMode(TooltipPositionMode.POINT);
//
//        // Thiết lập dữ liệu cho biểu đồ cột
//        barChartView.setChart(columnChart);
//
////        barChartView.invalidate();
//    }

    private void drawBarChart(int correct, int headDown, int headRight, int headLeft,
                              int leaningRight, int leaningLeft, int rightHandUp, int leftHandUp) {
//      // Bar Chart
        // Bar Chart
        BarChart barChart = findViewById(R.id.barChart);

// Các cài đặt khác của biểu đồ

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0); // Đảm bảo trục Y bắt đầu từ 0

        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, correct));
        entries.add(new BarEntry(1, headDown));
        entries.add(new BarEntry(2, headRight));
        entries.add(new BarEntry(3, headLeft));
        entries.add(new BarEntry(4, leaningRight));
        entries.add(new BarEntry(5, leaningLeft));
        entries.add(new BarEntry(6, rightHandUp));
        entries.add(new BarEntry(7, leftHandUp));

//        entries.add(new BarEntry(0, 0f));
//        entries.add(new BarEntry(1, 40f));
//        entries.add(new BarEntry(2, 60f));
//        entries.add(new BarEntry(3, 80f));
//        entries.add(new BarEntry(4, 100f));
//        entries.add(new BarEntry(5, 60f));
//        entries.add(new BarEntry(6, 80f));
//        entries.add(new BarEntry(7, 100f));

        BarDataSet dataSet = new BarDataSet(entries, "Label");
        dataSet.setValueTextSize(14f);

        dataSet.setColor(Color.parseColor("#63b3f3")); // Thay đổi màu của cột

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f%%", value); // Định dạng giá trị là số phần trăm với dấu %
            }
        });


        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        barChart.setData(data);

        barChart.setFitBars(true);
        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        final String[] labels = new String[]{"correct", "head down", "head right", "head left", "leaning right", "leaning left", "right hand up",  "left hand up"};
//        final String[] labels = new String[]{"left hand up", "Label 2", "Label 3", "Label 4", "Label 5", "label6", "label7", "label8"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int intValue = Math.round(value);
                if (intValue >= 0 && intValue < labels.length) {
                    return labels[intValue];
                } else {
                    return "";
                }
            }
        });
        xAxis.setLabelRotationAngle(30);

        // Ẩn sọc và chỉ hiển thị cột
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true); // Hiển thị trục X

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawBorders(false);
        barChart.setTouchEnabled(false);

        // Thay đổi màu sắc trục X và Y
        xAxis.setTextColor(Color.BLACK);
        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisRight().setTextColor(Color.BLACK);
    }

    // pieChart
    private void drawPieChart(int correct, int wrong) {
        PieChart pieChart = findViewById(R.id.pieChart);
        pieChart.setHoleRadius(0f);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(correct)); // Sử dụng label là "25" để hiển thị giá trị
        pieEntries.add(new PieEntry(wrong));


        // Tạo mảng màu sắc tùy chỉnh
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#fbd34d")); // vàng
        colors.add(Color.parseColor("#63b3f3")); // xanh nhạt

        // Chuyển ArrayList<Integer> thành mảng int[]
        int[] colorsArray = new int[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            colorsArray[i] = colors.get(i);
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Pie Chart Data");
        pieDataSet.setColors(colors);

        // Chỉnh kích thước và màu sắc của giá trị phần trăm
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(16f);

        // Ẩn nhãn trên Pie Chart
        pieDataSet.setDrawValues(true);

        PieData pieData = new PieData(pieDataSet);

        // Định dạng giá trị hiển thị trên biểu đồ
        pieData.setValueFormatter(new PercentFormatter(pieChart)); // Sử dụng PercentFormatter

        pieChart.setData(pieData);
        pieChart.setDescription(null); // Xóa mô tả
        pieChart.invalidate(); // Refresh biểu đồ

        // Tạo Legend và thiết lập nhãn tùy chỉnh
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false); // Tắt Legend hoàn toàn
        String[] labels = {"A", "B", "C"};
        legend.setExtra(colorsArray, labels); // Thiết lập nhãn cho các ô vuông
    }

    private void Home1() {
        LinearLayout btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Statistic.this, Home1.class);
                startActivity(intent);
            }
        });
    }

    private void Play() {
        LinearLayout btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Statistic.this, Home.class);
                startActivity(intent);
            }
        });
    }
}

