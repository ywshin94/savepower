package com.voidpointer.selfgauge;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;

/**
 * Created by ywshin on 2017. 7. 8..
 */

public class ForecastDetail extends Dialog {
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

        //getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
