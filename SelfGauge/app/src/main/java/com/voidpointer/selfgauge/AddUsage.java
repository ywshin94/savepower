package com.voidpointer.selfgauge;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by SHIN on 2016-08-16.
 */
public class AddUsage extends Dialog {
    Context mContextParent;
    static Context mContext;

    EditText mEditDate;
    EditText mEditTime;
    EditText mEditUsage;
    Button mBtnSave;
    Button mBtnHelp;
    Calendar mCalendar;

    int mMode = 0;   //0: add, 1: edit
    InfoClass mInfoNode;
    boolean mStartWithHowto = false;

    Dialog mHowtoDialog = null;

    public interface IAddUsageEventListener {
        public void customDialogEvent(Calendar calendar, int usage);
    }
    private IAddUsageEventListener onEventListener;

    public AddUsage(Context context) {
        super(context);
        this.mContextParent = context;
    }
    public AddUsage(Context context, IAddUsageEventListener listener) {
        super(context);
        this.mContextParent = context;
        this.onEventListener = listener;
        this.mMode = 0;  // add mode
    }

    public AddUsage(Context context, InfoClass infoNode, IAddUsageEventListener listener) {
        super(context);
        this.mContextParent = context;
        this.onEventListener = listener;
        this.mMode = 1;  // edit mode
        this.mInfoNode = infoNode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_usage);

        mContext = this.getContext();

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

            if( CustomAdapter.isStartImsiNode(this.mInfoNode)){
                mEditDate.setEnabled(false);
                mEditTime.setEnabled(false);
            }
        }

        setDateEdit(mCalendar, mEditDate);
        setTimeEdit(mCalendar, mEditTime);

        mEditDate.setOnTouchListener(new View.OnTouchListener(){   //터치 이벤트 리스너 등록(누를때와 뗐을때를 구분)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    if(mEditDate.getClass()==v.getClass()){
                        new DatePickerDialog(mContextParent, mDateSetListener,
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
                        new TimePickerDialog(mContextParent, mTimeSetListener,
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

                editFocusOff();
                dismiss();  // close dialog
            }
        });

        mBtnHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowGuide();
            }
        });
    }

    public void editFocusOn(){
        mEditUsage.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContextParent.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void editFocusOff(){
        InputMethodManager imm = (InputMethodManager)mContextParent.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditUsage.getWindowToken(), 0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mStartWithHowto){
            mEditUsage.requestFocus();
            timerStart();
        }
        else {
            editFocusOn();
        }
    }

    TimerTask mTimerTask = null;
    private final Handler handler = new Handler();

    public void timerStart() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                timerCall();
            }
        };
        Timer timer = new Timer();
        timer.schedule(mTimerTask, 100);
    }

    protected void timerCall() {
        Runnable updater = new Runnable() {
            public void run() {
                ShowGuide();
            }
        };
        handler.post(updater);
    }

    public void ShowGuide(){
        editFocusOff();

        mHowtoDialog = new Dialog(this.mContext);
        mHowtoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mHowtoDialog.setContentView(R.layout.howto);
        mHowtoDialog.setCancelable(true);
        mHowtoDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mHowtoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnHelp = (Button)mHowtoDialog.findViewById(R.id.buttonCheckHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog helpDlg = new Dialog(MainActivity.mContext);
                helpDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                helpDlg.setContentView(R.layout.help);
                helpDlg.setCancelable(true);
                helpDlg.show();
                WebView webView = (WebView)helpDlg.findViewById(R.id.helpWebview);
                webView.loadUrl("file:///android_asset/howtoguage/index.html");

                Button btnExit = (Button)helpDlg.findViewById(R.id.buttonHelpClose);
                btnExit.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        helpDlg.dismiss();
                    }
                });

                helpDlg.setOnDismissListener(new Dialog.OnDismissListener(){
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHowtoDialog.dismiss();
                    }
                });


            }
        });

        mHowtoDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                WindowManager.LayoutParams params = mHowtoDialog.getWindow().getAttributes();
                params.x = -4;
                params.y = -80;
                mHowtoDialog.getWindow().setAttributes(params);
            }
        });

        // Dialog Dismiss시 Event 받기
        mHowtoDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editFocusOn();
            }
        });
        mHowtoDialog.show();
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


}