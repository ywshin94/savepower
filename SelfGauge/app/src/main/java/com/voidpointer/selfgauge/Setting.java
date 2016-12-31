package com.voidpointer.selfgauge;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by SHIN on 2016-08-16.
 */
public class Setting extends Dialog {
    Context mContext;

    public interface ISettingEventListener {
        public void customDialogEvent();
    }
    private ISettingEventListener onEventListener;

    public Setting(Context context) {
        super(context);
        this.mContext = context;
    }
    public Setting(Context context, int themeResId, ISettingEventListener listener) {
        super(context);
        this.mContext = context;
        this.onEventListener = listener;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting);
        setTitle("설정");

        Spinner spinner = (Spinner)findViewById(R.id.spinnerPowerType);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, R.array.powerType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);

    }
}
