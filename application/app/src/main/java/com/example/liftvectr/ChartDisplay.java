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
    private LineDataSet xLinAccLine;
    private LineDataSet yLinAccLine;
    private LineDataSet zLinAccLine;

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

        ArrayList<IMUData> data = exercise.getExerciseData();
        for (int i = 0; i < data.size(); i++) {
            xLinAcc_ms.add(new Entry(data.get(i).millisec, data.get(i).x_lin_acc));
            yLinAcc_ms.add(new Entry(data.get(i).millisec, data.get(i).y_lin_acc));
            zLinAcc_ms.add(new Entry(data.get(i).millisec, data.get(i).z_lin_acc));
        }

        xLinAccLine = new LineDataSet(xLinAcc_ms, "X Linear Acceleration");
        yLinAccLine = new LineDataSet(yLinAcc_ms, "Y Linear Acceleration");
        zLinAccLine = new LineDataSet(zLinAcc_ms, "Z Linear Acceleration");
    }

    public void setPlotStyling(String config) {

        // Y-Axis(left), X-Axis(bottom), No gridlines, No labeled data values/circles, RGB Colors
        if (config.equals("default")) {

            xLinAccLine.setDrawCircles(false);
            yLinAccLine.setDrawCircles(false);
            zLinAccLine.setDrawCircles(false);

            xLinAccLine.setDrawValues(false);
            yLinAccLine.setDrawValues(false);
            zLinAccLine.setDrawValues(false);

            xLinAccLine.setColor(Color.RED);
            yLinAccLine.setColor(Color.GREEN);
            zLinAccLine.setColor(Color.BLUE);

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
        lines.add(xLinAccLine);
        lines.add(yLinAccLine);
        lines.add(zLinAccLine);

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