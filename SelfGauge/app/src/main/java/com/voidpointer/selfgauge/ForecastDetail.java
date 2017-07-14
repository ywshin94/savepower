package com.voidpointer.selfgauge;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by ywshin on 2017. 7. 8..
 */

public class ForecastDetail extends Dialog {
    int[] kwh = new int[2];
    int[] details = new int[7];

    public ForecastDetail(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.forecast_detail);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        TextView view;

        view = (TextView) findViewById(R.id.kwh1);
        view.setText(String.format("%skWh", getMoneyString(kwh[0])));

        view = (TextView) findViewById(R.id.kwh2);
        view.setText(String.format("%skWh", getMoneyString(kwh[1])));

        view = (TextView) findViewById(R.id.text1);
        view.setText(String.format("%s원", getMoneyString(details[0])));

        view = (TextView) findViewById(R.id.text2);
        view.setText(String.format("%s원", getMoneyString(details[1])));

        view = (TextView) findViewById(R.id.text3);
        if(details[2]>0){
            view.setText(String.format("- %s원", getMoneyString(details[2])));
        }else {
            view.setText(String.format("%s원", getMoneyString(details[2])));
        }

        view = (TextView) findViewById(R.id.text4);
        view.setText(String.format("%s원", getMoneyString(details[3])));

        view = (TextView) findViewById(R.id.text5);
        view.setText(String.format("%s원", getMoneyString(details[4])));

        view = (TextView) findViewById(R.id.text6);
        view.setText(String.format("%s원", getMoneyString(details[5])));

        view = (TextView) findViewById(R.id.text7);
        view.setText(String.format("%s원", getMoneyString(details[6])));

        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public static String getMoneyString(int money){
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(money);
    }
}
