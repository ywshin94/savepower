package com.voidpointer.selfgauge;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by SHIN on 2016-08-16.
 */
public class AddUsage extends Dialog {
    Context mContext;

    EditText mEditDate;
    EditText mEditTime;
    EditText mEditUsage;
    Button mBtnSave;
    Button mBtnHelp;
    Calendar mCalendar;

    int mMode = 0;   //0: add, 1: edit
    InfoClass mInfoNode;

    public interface IAddUsageEventListener {
        public void customDialogEvent(Calendar calendar, int usage);
    }
    private IAddUsageEventListener onEventListener;

    public AddUsage(Context context) {
        super(context);
        this.mContext = context;
    }
    public AddUsage(Context context, IAddUsageEventListener listener) {
        super(context);
        this.mContext = context;
        this.onEventListener = listener;
        this.mMode = 0;  // add mode
    }

    public AddUsage(Context context, InfoClass infoNode, IAddUsageEventListener listener) {
        super(context);
        this.mContext = context;
        this.onEventListener = listener;
        this.mMode = 1;  // edit mode
        this.mInfoNode = infoNode;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_usage);
        setTitle("Input Power Usage");

        mEditDate = (EditText)findViewById(R.id.editDate);
        mEditTime = (EditText)findViewById(R.id.editTime);
        mEditUsage = (EditText)findViewById(R.id.editUsage);
        mBtnSave = (Button)findViewById(R.id.buttonSave);
        mBtnHelp = (Button)findViewById(R.id.buttonHelp);

        mCalendar = Calendar.getInstance();
        long now = System.currentTimeMillis();
        mCalendar.setTimeInMillis(now);

        if( mMode == 1 ) {
            // edit mode
            mCalendar.setTimeInMillis(mInfoNode.datetime);
            int usage = mInfoNode.usage;
            if(usage>0) {
                String strUsage = String.format("%d", mInfoNode.usage);
                mEditUsage.setText(strUsage);
                mBtnSave.setText("수정");
            }
            else {
                mMode = 0;
            }
        }

        setDateEdit(mCalendar, mEditDate);
        setTimeEdit(mCalendar, mEditTime);

        mEditUsage.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        mEditDate.setOnTouchListener(new View.OnTouchListener(){   //터치 이벤트 리스너 등록(누를때와 뗐을때를 구분)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    if(mEditDate.getClass()==v.getClass()){
                        new DatePickerDialog(mContext, mDateSetListener,
                                mCalendar.get(Calendar.YEAR),
                                mCalendar.get(Calendar.MONTH),
                                mCalendar.get(Calendar.DAY_OF_MONTH)
                        ).show();
                    }
                }
                return true;
            }
        });

        mEditTime.setOnTouchListener(new View.OnTouchListener(){   //터치 이벤트 리스너 등록(누를때와 뗐을때를 구분)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    if(mEditTime.getClass()==v.getClass()){
                        new TimePickerDialog(mContext, mTimeSetListener,
                                mCalendar.get(Calendar.HOUR),
                                mCalendar.get(Calendar.MINUTE),
                                false
                        ).show();
                    }
                }
                return true;
            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if( mEditUsage.getText().toString().isEmpty() ) {
                    return;
                }

                int usage = Integer.parseInt(mEditUsage.getText().toString());
                Log.v("ywshin", "usage :" + usage);
                onEventListener.customDialogEvent(mCalendar, usage);  // call callback function

                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditUsage.getWindowToken(), 0);
                dismiss();  // close dialog
            }
        });

        mBtnHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowHowTo();
            }
        });
    }

    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDateEdit(mCalendar, mEditDate);
        }
    };

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            mCalendar.set(Calendar.HOUR, i);
            mCalendar.set(Calendar.MINUTE, i1);
            setTimeEdit(mCalendar, mEditTime);
        }
    };

    public void setDateEdit(Calendar cal, EditText edit){
        //현재 년도, 월, 일
        int year = cal.get ( cal.YEAR );
        int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;
        String yoil = cal.getDisplayName( cal.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

        String strDate = String.format("%d. %d. %d. (%s)", year, month, date, yoil);
        edit.setText(strDate);
    }

    public void setTimeEdit(Calendar cal, EditText edit){
        int hour = cal.get ( cal.HOUR ) ;
        int min = cal.get ( cal.MINUTE );
        String ampm = cal.getDisplayName(cal.AM_PM, Calendar.LONG, Locale.getDefault());

        String strTime = String.format("%s %d : %02d", ampm, hour, min);
        edit.setText(strTime);
    }

    @Override
    public void show() {
        super.show();
    }

    public void ShowHowTo(){
        Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(R.layout.howto);
        dialog.setTitle("이렇게 입력하세요");
        dialog.setCancelable(true);

        dialog.show();
    }
}