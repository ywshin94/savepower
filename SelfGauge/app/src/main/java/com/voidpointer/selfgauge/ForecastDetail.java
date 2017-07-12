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

        view = (TextView) findViewById(R.id.text1);
        view.setText(String.format("기본요금 : %s원", getMoneyString(details[0])));

        view = (TextView) findViewById(R.id.text2);
        view.setText(String.format("전력양요금 : %s원", getMoneyString(details[1])));

        view = (TextView) findViewById(R.id.text3);
        view.setText(String.format("필수사용량 보장공제 : %s원", getMoneyString(details[2])));

        view = (TextView) findViewById(R.id.text4);
        view.setText(String.format("전기요금계 : %s원\n                     (%s + %s - %s)", getMoneyString(details[3]),
                getMoneyString(details[0]), getMoneyString(details[1]), getMoneyString(details[2])));

        view = (TextView) findViewById(R.id.text5);
        view.setText(String.format("부가가치세 : %s원", getMoneyString(details[4])));

        view = (TextView) findViewById(R.id.text6);
        view.setText(String.format("전력산업 기반기금 : %s원", getMoneyString(details[5])));

        view = (TextView) findViewById(R.id.text7);
        view.setText(String.format("청구금액 : %s원", getMoneyString(details[6])));

        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public static String getMoneyString(int money){
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(money);
    }
}
