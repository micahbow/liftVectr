package com.example.liftvectr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class ChartDisplay extends AppCompatActivity {

    private Button returnBtn;
    private TextView exerciseType;

    private LineChart mpLineChart;
    private LineDataSet xAccLine;
    private LineDataSet yAccLine;
    private LineDataSet zAccLine;
    private LineDataSet xGyroLine;
    private LineDataSet yGyroLine;
    private LineDataSet zGyroLine;
    private LineDataSet xMagnLine;
    private LineDataSet yMagnLine;
    private LineDataSet zMagnLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_display);

        returnBtn = (Button) findViewById(R.id.return_button);
        exerciseType = (TextView) findViewById(R.id.exercise_type);
        mpLineChart = (LineChart) findViewById(R.id.line_chart);

        // Accessing the exercise/config data sent over from MainActivity
        Intent intent = getIntent();
        Exercise exercise = (Exercise) intent.getSerializableExtra("exercise");
        String config = (String) intent.getSerializableExtra("config");

        plotExercise(exercise, config);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToMainActivity();
            }
        });
    }

    public void extractXYZLineData(Exercise exercise)
    {
        ArrayList<Entry> xLinAcc_ms = new ArrayList<>();
        ArrayList<Entry> yLinAcc_ms = new ArrayList<>();
        ArrayList<Entry> zLinAcc_ms = new ArrayList<>();
        ArrayList<Entry> xAngVel_ms = new ArrayList<>();
        ArrayList<Entry> yAngVel_ms = new ArrayList<>();
        ArrayList<Entry> zAngVel_ms = new ArrayList<>();
        ArrayList<Entry> xMagField_ms = new ArrayList<>();
        ArrayList<Entry> yMagField_ms = new ArrayList<>();
        ArrayList<Entry> zMagField_ms = new ArrayList<>();

        ArrayList<IMUData> data = exercise.getExerciseData();
        for (int i = 0; i < data.size(); i++) {
            xLinAcc_ms.add(new Entry(data.get(i).micros, data.get(i).x_lin_acc));
            yLinAcc_ms.add(new Entry(data.get(i).micros, data.get(i).y_lin_acc));
            zLinAcc_ms.add(new Entry(data.get(i).micros, data.get(i).z_lin_acc));
            xAngVel_ms.add(new Entry(data.get(i).micros, data.get(i).x_ang_vel));
            yAngVel_ms.add(new Entry(data.get(i).micros, data.get(i).y_ang_vel));
            zAngVel_ms.add(new Entry(data.get(i).micros, data.get(i).z_ang_vel));
            xMagField_ms.add(new Entry(data.get(i).micros, data.get(i).x_mag_field));
            yMagField_ms.add(new Entry(data.get(i).micros, data.get(i).y_mag_field));
            zMagField_ms.add(new Entry(data.get(i).micros, data.get(i).z_mag_field));
        }

        xAccLine = new LineDataSet(xLinAcc_ms, "X_A");
        yAccLine = new LineDataSet(yLinAcc_ms, "Y_A");
        zAccLine = new LineDataSet(zLinAcc_ms, "Z_A");
        xGyroLine = new LineDataSet(xAngVel_ms, "X_G");
        yGyroLine = new LineDataSet(yAngVel_ms, "Y_G");
        zGyroLine = new LineDataSet(zAngVel_ms, "Z_G");
        xMagnLine = new LineDataSet(xMagField_ms, "X_M");
        yMagnLine = new LineDataSet(yMagField_ms, "Y_M");
        zMagnLine = new LineDataSet(zMagField_ms, "Z_M");
    }

    public void setPlotStyling(String config) {

        // Y-Axis(left), X-Axis(bottom), No gridlines, No labeled data values/circles, RGB Colors
        if (config.equals("default")) {

            xAccLine.setDrawCircles(true);
            yAccLine.setDrawCircles(true);
            zAccLine.setDrawCircles(true);
            xGyroLine.setDrawCircles(true);
            yGyroLine.setDrawCircles(true);
            zGyroLine.setDrawCircles(true);
            xMagnLine.setDrawCircles(true);
            yMagnLine.setDrawCircles(true);
            zMagnLine.setDrawCircles(true);

            xAccLine.setDrawValues(true);
            yAccLine.setDrawValues(true);
            zAccLine.setDrawValues(true);
            xGyroLine.setDrawValues(true);
            yGyroLine.setDrawValues(true);
            zGyroLine.setDrawValues(true);
            xMagnLine.setDrawValues(true);
            yMagnLine.setDrawValues(true);
            zMagnLine.setDrawValues(true);

            xAccLine.setColor(Color.rgb(153, 0, 0));
            yAccLine.setColor(Color.rgb(255, 0, 0));
            zAccLine.setColor(Color.rgb(255, 102, 102));
            xGyroLine.setColor(Color.rgb(0, 153, 0));
            yGyroLine.setColor(Color.rgb(0, 255, 0));
            zGyroLine.setColor(Color.rgb(102, 255, 102));
            xMagnLine.setColor(Color.rgb(0, 0, 153));
            yMagnLine.setColor(Color.rgb(0, 0, 255));
            zMagnLine.setColor(Color.rgb(102, 102, 255));

            mpLineChart.getAxisRight().setDrawGridLines(false);
            mpLineChart.getAxisRight().setEnabled(false);
            mpLineChart.getAxisLeft().setDrawGridLines(false);
            mpLineChart.getXAxis().setDrawGridLines(false);
            mpLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            Description desc = new Description();
            desc.setText("");
            mpLineChart.setDescription(desc);
        }
        // Option to add different styling configurations
    }

    public void plotExercise(Exercise exercise, String config)
    {
        exerciseType.setText(exercise.getType().toString());

        extractXYZLineData(exercise);
        setPlotStyling(config);

        ArrayList<ILineDataSet> lines = new ArrayList<>();
        lines.add(xAccLine);
        lines.add(yAccLine);
        lines.add(zAccLine);
        lines.add(xGyroLine);
        lines.add(yGyroLine);
        lines.add(zGyroLine);
        lines.add(xMagnLine);
        lines.add(yMagnLine);
        lines.add(zMagnLine);

        LineData data = new LineData(lines);
        mpLineChart.setData(data);
        mpLineChart.invalidate(); // Re-draw the chart
    }

    public void transitionToMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}