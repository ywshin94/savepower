package com.voidpointer.selfgauge;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.Date;

import static java.lang.Integer.min;

public class MainActivity extends AppCompatActivity {

    public static Context mContext;

    private static final String TAG = "ywshin";
    private DBHelper mDbHelper;
    private Cursor mCursor;
    private InfoClass mInfoClass;

    private ListView mListView;
    private CustomAdapter mAdapter;

    public int mPowerType = -1;
    public int mCheckDay = -1;
    public static Calendar mCalStart;
    public static Calendar mCalEnd;
    public int mMonthShift = 0;

    Button mButtonPrev;
    Button mButtonNext;
    FloatingActionButton mFloatBtn;
    TextView mFloatBtnText;

    boolean mFirstCall = true;

    static final String mYoilString[] = {"일","월","화","수","목","금","토"};

    void _log(String log){
        Log.v(TAG, log);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _log("MainActivity... onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;

        mFloatBtnText = (TextView)findViewById(R.id.fabText);
        mFloatBtn = (FloatingActionButton) findViewById(R.id.fab);
        mFloatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPowerUsage();
            }
        });

        mButtonPrev = (Button)findViewById(R.id.buttonPrev);
        mButtonNext = (Button)findViewById(R.id.buttonNext);
        mButtonNext.setVisibility(View.INVISIBLE);

        mButtonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMonthShift -= 1;
                setDateRange();
                setDatabaseToAdapterAfterAdd();
                scroolLast();
                mButtonNext.setVisibility(View.VISIBLE);
                mFloatBtn.setVisibility(View.INVISIBLE);
                mFloatBtnText.setVisibility(View.INVISIBLE);

                LoadAd();
                ShowGuide();
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMonthShift += 1;
                setDateRange();
                setDatabaseToAdapterAfterAdd();
                scroolLast();

                if( mMonthShift==0 ){
                    mButtonNext.setVisibility(View.INVISIBLE);
                    mFloatBtn.setVisibility(View.VISIBLE);
                    mFloatBtnText.setVisibility(View.VISIBLE);

                    LoadAd();
                    ShowGuide();
                }
            }
        });

        //
        mPowerType = getPrefPowerType();
        mCheckDay = getPrefCheckDay();
        setDateRange();

        //
        // DB Create and Open
        mDbHelper = new DBHelper(this);

        // List용 CustomAdapter
        mAdapter = new CustomAdapter();
        // Xml에서 추가한 ListView 연결
        mListView = (ListView) findViewById(R.id.listview);
        // ListView에 어댑터 연결
        mListView.setAdapter(mAdapter);

        //setDatabaseToAdapter();
    }

    public void LoadAd(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if(mAdapter.getCount() >= 4) {
            if(mAdView.getVisibility() != View.VISIBLE) {
                mAdView.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().addTestDevice("985B8F7BFD0E460305E4FDBA57B9BE09").build();
                mAdView.loadAd(adRequest);
            }
        }
        else{
            mAdView.setVisibility(View.GONE);
        }

    }

    public boolean IsShowGuide1(){
        if(mMonthShift == 0 && mAdapter.getCount() == 0) {
            return true;
        }
        return false;
    }

    public boolean IsShowGuide2(){
        if(mAdapter.getCount()>1) {
            InfoClass startNode = (InfoClass) mAdapter.getItem(0);
            return mAdapter.isImsiNode(startNode);
        }
        return false;
    }

    public boolean IsShowGuideHowtoGuage(){
        // 지난달 데이터 확인
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(mCalEnd.getTimeInMillis());
        calStart.add(Calendar.MONTH, -6);

        mDbHelper.open();
        mCursor = mDbHelper.getRangeColumns(calStart, mCalEnd);
        boolean res = false;
        if(mMonthShift == 0 && mCursor.getCount()<2) {
            res = true;
        }
        mCursor.close();
        mDbHelper.close();

        return res;
    }

    public void ShowGuide(){
        //GuideDialog dialog = new GuideDialog(mContext);
        //dialog.show();
        if(IsShowGuide1()) {
            RelativeLayout imageView = (RelativeLayout) findViewById(R.id.guideimage);
            imageView.setVisibility(View.VISIBLE);
        }
        else if(IsShowGuide2()){
            /*
            final Dialog helpDlg = new Dialog(MainActivity.mContext);
            helpDlg.setContentView(R.layout.guidedlg);
            helpDlg.setCancelable(true);
            helpDlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            helpDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            helpDlg.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    WindowManager.LayoutParams params = helpDlg.getWindow().getAttributes();
                    params.y = -300;
                    helpDlg.getWindow().setAttributes(params);
                }
            });

            helpDlg.show();
            */
        }
        else{
            RelativeLayout imageView = (RelativeLayout) findViewById(R.id.guideimage);
            imageView.setVisibility(View.GONE);
        }
    }
    public void HideGuide(){
        RelativeLayout imageView = (RelativeLayout) findViewById(R.id.guideimage);
        imageView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        _log("MainActivity... onResume");
        super.onResume();

        if(mFirstCall){
            mFirstCall = false;
            if( getPermission() ) {
                setDatabaseToAdapter();
                mAdapter.notifyDataSetChanged();
                scroolLast();

                LoadAd();
                ShowGuide();
            }
            return;
        }

        int checkDay = getPrefCheckDay();
        int powerType = getPrefPowerType();

        if( mCheckDay != checkDay || mPowerType != powerType ){
            _log("reset");
            mCheckDay = checkDay;
            mPowerType = powerType;
            setDateRange();

            if( getPermission() ) {
                setDatabaseToAdapterAfterAdd();
            }
        }
        else {
            getPermission();
        }
        LoadAd();
    }

    public int getPrefPowerType(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int powerType = Integer.parseInt(pref.getString("powerType", "0"));
        return powerType;
    }

    public int getPrefCheckDay(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int checkDay = Integer.parseInt(pref.getString("checkDay", "18"));
        return checkDay;
    }

    public void setDateRange(){
        _log( "MainActivity... setDateRange" );

        // 현재 날짜,시간 받아옴
        mCalStart = Calendar.getInstance();
        if( mCalStart.get(Calendar.DAY_OF_MONTH) <= mCheckDay ) {
            mCalStart.add(Calendar.MONTH, -1);
        }

        _log( "mCheckDay : " + mCheckDay );
        if( mCheckDay == 100 ){ //매월 말일
            int day = mCalStart.getActualMaximum( Calendar.DAY_OF_MONTH );
            mCalStart.set(Calendar.DAY_OF_MONTH, day);
        }else{
            mCalStart.set(Calendar.DAY_OF_MONTH, mCheckDay);
        }

        mCalStart.set(Calendar.HOUR_OF_DAY, 0);
        mCalStart.set(Calendar.MINUTE, 0);
        mCalStart.set(Calendar.SECOND, 0);
        mCalStart.set(Calendar.MILLISECOND, 0);

        mCalEnd = Calendar.getInstance();
        mCalEnd.setTimeInMillis( mCalStart.getTimeInMillis() );   //mCalEnd = mCalStart  <-- 이렇게 하니까 주소가 복사되더라.
        mCalEnd.add(Calendar.MONTH, 1);

        if( mCheckDay == 100 ){
            int day = mCalEnd.getActualMaximum( Calendar.DAY_OF_MONTH );
            mCalEnd.set(Calendar.DAY_OF_MONTH, day);
        }
        //mCalEnd.add(Calendar.DAY_OF_YEAR, -1);
        mCalEnd.set(Calendar.HOUR_OF_DAY, 23);
        mCalEnd.set(Calendar.MINUTE, 59);
        mCalEnd.set(Calendar.SECOND, 59);
        mCalEnd.set(Calendar.MILLISECOND, 999);

        if( mMonthShift != 0 ){
            mCalStart.add(Calendar.MONTH, mMonthShift);
            mCalEnd.add(Calendar.MONTH, mMonthShift);
        }

        TextView textDateRange = (TextView) findViewById(R.id.textDateRange);
        textDateRange.setText(getDateString(mCalStart) +" ~ "+ getDateString(mCalEnd));
    }

    public static boolean isStartDay(long datetime){
        return getDateString(datetime).equals(getDateString(mCalStart));
    }

    public static boolean isEndDay(long datetime){
        return getDateString(datetime).equals(getDateString(mCalEnd));
    }

    public static String getYoilString(Calendar calendar){
        return mYoilString[calendar.get(Calendar.DAY_OF_WEEK)-1];
    }

    public static String getDateString(long datetime){
        Date d = new Date(datetime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return getDateString(cal);
    }

    public static String getDateString(Calendar calendar){
        return String.format("%04d.%02d.%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DAY_OF_MONTH),
                getYoilString(calendar));
    }

    public static String getDateStringShort(Calendar calendar){
        return String.format("%d.%02d",
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String getTimeString(Calendar calendar){
        return String.format("%02d:%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

    public static String getDateTimeString(Calendar calendar){
        return String.format("%04d.%02d.%02d - %02d:%02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

    enum FirstUsageGuessType{ _DidNotGuess, _WithCur2Value, _WithCur1Last1Value, _WithCur1Value};
    FirstUsageGuessType mFirstUsageGuessType = FirstUsageGuessType._DidNotGuess;
    public int guessFirstUsage(){
        int guess_usage = 0;
        mDbHelper.open();

        int curUsageFirst = 0;
        long curDateTimeFirst = 0;
        mCursor = mDbHelper.getRangeColumns(mCalStart, mCalEnd);
        if(mCursor.getCount()>=2){
            // 1. 이번달 데이터가 2개이상 있으면 계산한다.
            mCursor.moveToFirst();
            curUsageFirst = mCursor.getInt(mCursor.getColumnIndex("usage"));
            curDateTimeFirst = mCursor.getLong(mCursor.getColumnIndex("date"));
            mCursor.moveToLast();
            int usageLast= mCursor.getInt(mCursor.getColumnIndex("usage"));
            long datetimeLast = mCursor.getLong(mCursor.getColumnIndex("date"));
            float usage_datetime = (float) (usageLast-curUsageFirst) / (float) (datetimeLast-curDateTimeFirst);

            guess_usage = (int)(curUsageFirst - usage_datetime*(curDateTimeFirst-mCalStart.getTimeInMillis()) + 0.5);
            mFirstUsageGuessType = FirstUsageGuessType._WithCur2Value;

            //if(guess_usage>curUsageFirst){
            //    mFirstUsageGuessType = FirstUsageGuessType._DidNotGuess;
            //}
        }
        else if(mCursor.getCount()==1){
            // 2. 이번달 데이터가 1개 있으면 지난 3달 데이터가 있는지 확인해 본다.
            mCursor.moveToFirst();
            curUsageFirst = mCursor.getInt(mCursor.getColumnIndex("usage"));
            curDateTimeFirst = mCursor.getLong(mCursor.getColumnIndex("date"));

            // 지난달 데이터 확인
            Calendar calBeforeStart = Calendar.getInstance();
            calBeforeStart.setTimeInMillis(mCalStart.getTimeInMillis());
            calBeforeStart.add(Calendar.MONTH, -3);

            mCursor = mDbHelper.getRangeColumns(calBeforeStart, mCalStart);
            if(mCursor.getCount() >= 1){
                // 2-1. 지난 3달동안 데이터가 1개 이상 있는경우
                mCursor.moveToLast();
                int usageLast= mCursor.getInt(mCursor.getColumnIndex("usage"));
                long datetimeLast = mCursor.getLong(mCursor.getColumnIndex("date"));
                float usage_datetime = (float)(curUsageFirst-usageLast) / (float)(curDateTimeFirst-datetimeLast);

                guess_usage = (int)(curUsageFirst - usage_datetime*(curDateTimeFirst-mCalStart.getTimeInMillis()) + 0.5);
                mFirstUsageGuessType = FirstUsageGuessType._WithCur1Last1Value;
            }
            else{
                // 2-2. 지난 3달동안 데이터가 하나도 없다.
                guess_usage = (int)(curUsageFirst - (float)(curDateTimeFirst-mCalStart.getTimeInMillis())/1000.0/60.0/60.0/24.0*15 + 0.5);
                mFirstUsageGuessType = FirstUsageGuessType._WithCur1Value;
            }
        }
        else{
            mFirstUsageGuessType = FirstUsageGuessType._DidNotGuess;
        }

        mCursor.close();
        mDbHelper.close();
        return guess_usage;
    }
    /**
     * DB에서 받아온 값을 ArrayList에 Add
     */
    private void setDatabaseToAdapter(){
        _log("setDatabaseToAdapter()");
        mDbHelper.open();
        mCursor = mDbHelper.getRangeColumns(mCalStart, mCalEnd);

        int itemCount = mCursor.getCount();
        _log("COUNT = " + itemCount);

        boolean bFirst = true;
        boolean bGijoonGap = false;

        int startdate_usage = 0;
        int lastcheck_usage = 0;

        int id = -1;
        long datetime = 0;
        String type = "";
        int usage = 0;
        String deleted = "no";

        while (mCursor.moveToNext()) {
            id = mCursor.getInt(mCursor.getColumnIndex("_id"));
            datetime = mCursor.getLong(mCursor.getColumnIndex("date"));
            type = mCursor.getString(mCursor.getColumnIndex("type"));
            usage = mCursor.getInt(mCursor.getColumnIndex("usage"));
            deleted = mCursor.getString(mCursor.getColumnIndex("deleted"));

            _log("id : " + id + ", datetime : " + datetime + ", usage : " + usage);

            // 기준 검침일
            if( bFirst ){
                _log("getDateString(datetime) : " + getDateString(datetime) + " - getDateString(mCalStart) : " + getDateString(mCalStart) );
                if( !getDateString(datetime).equals(getDateString(mCalStart))){
                    //리스트의 첫번째 날짜가 지난달 검침일이 아닐 때
                    Calendar addCalendar = Calendar.getInstance();
                    addCalendar.setTimeInMillis(mCalStart.getTimeInMillis());
                    addCalendar.set(Calendar.HOUR_OF_DAY, 12);  // mCalStart 를 받아서 시간을 낮 12시로 고침
                    mInfoClass = new InfoClass( -1, addCalendar.getTimeInMillis(), type, 0, "no" );
                    mAdapter.add(mInfoClass);
                }
                else {
                    bGijoonGap = true;
                    startdate_usage = usage;
                }
                bFirst = false;
            }
            mInfoClass = new InfoClass( id, datetime, type, usage, deleted );
            mAdapter.add(mInfoClass);

            lastcheck_usage = usage;
        }

        mCursor.close();
        mDbHelper.close();

        // 첫 임시 노드 지침 예측

        if(!bGijoonGap){
            startdate_usage = guessFirstUsage();
            if(startdate_usage>0) {
                InfoClass firstNode = (InfoClass) mAdapter.getItem(0);
                firstNode.usage = startdate_usage;
                mAdapter.setItem(0, firstNode);
            }
        }

        // 예상 요금
        //if( itemCount>=2 && mMonthShift == 0) {
        if(startdate_usage > 0 ){
            int month_usage;
            String comment;

            int usage_shift = lastcheck_usage - startdate_usage;
            long datetime_shift = datetime - mCalStart.getTimeInMillis();

            float usage_datetime = (float) usage_shift / (float) datetime_shift;

            long remain_datetime = mCalEnd.getTimeInMillis() - datetime;
            int remain_days = (int) (remain_datetime / 1000. / 60. / 60. / 24.);
            int remain_usage = (int) (remain_datetime * usage_datetime);
            month_usage = lastcheck_usage + remain_usage;

            _log("lastcheck_usage : " + lastcheck_usage);
            _log("startdate_usage : " + startdate_usage);
            _log("usage_shift : " + usage_shift);

            if(bGijoonGap) {
                comment = String.format("이번달에는 하루에 %.1f(kWh) 정도 사용했습니다.\n검침일까지 %d일 남았고, %.1f(kWh) 정도 더 사용할 것 같아요.",
                        usage_datetime * 24 * 60 * 60 * 1000, remain_days, remain_datetime * usage_datetime);
            }
            else{
                if(mFirstUsageGuessType == FirstUsageGuessType._DidNotGuess) {
                    comment = "";
                }
                else {
                    if (mFirstUsageGuessType == FirstUsageGuessType._WithCur1Value) {
                        comment = String.format("하루에 %.1f(kWh) 정도 사용한 것으로 가정합니다.",
                                usage_datetime * 24 * 60 * 60 * 1000);
                    } else {
                        comment = String.format("이번달에는 하루에 %.1f(kWh) 정도 사용한 것 같습니다.",
                                usage_datetime * 24 * 60 * 60 * 1000);
                    }
                    comment = String.format("%s\n검침일까지 %d일 남았고, %.1f(kWh) 정도 더 사용할 것 같아요.",
                            comment, remain_days, remain_datetime * usage_datetime);
                    comment = String.format("%s\n지난달 검침결과를 입력하면 더욱 정확한 예상전기요금을 확인하실 수 있습니다.", comment);
                }
            }

            mInfoClass = new InfoClass(-2, mCalEnd.getTimeInMillis(), type, month_usage, comment);
            mAdapter.add(mInfoClass);
        }
    }

    public void setDatabaseToAdapterAfterAdd(){
        int count1 = mAdapter.getCount();
        boolean [] selected = new boolean[count1];
        for(int i=0; i<count1; i++){
            selected[i] = ((InfoClass)mAdapter.getItem(i)).selected;
        }

        //
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        setDatabaseToAdapter();

        //
        int count2 = mAdapter.getCount();
        int count = count1 < count2 ? count1 : count2;
        for(int i=0; i<count; i++){
            boolean sel = true;
            //if(i<count1) {
                sel = selected[i];
            //}
            ((InfoClass)mAdapter.getItem(i)).selected = sel;
        }
        //

        mAdapter.notifyDataSetChanged();
    }

    public void deleteData( int _id ){
        mDbHelper.open();
        mDbHelper.deleteColumn(_id);
        mDbHelper.close();

        setDatabaseToAdapterAfterAdd();
    }

    public void deleteAllData(){
        mCursor = null;

        mDbHelper.open();
        mCursor = mDbHelper.getAllColumns();

        while (mCursor.moveToNext()) {
            int id = mCursor.getInt(mCursor.getColumnIndex("_id"));
            mDbHelper.deleteColumn(id);
        }

        mCursor.close();
        mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        }/*else if (id == R.id.action_clear) {
            deleteAllData();
            setDatabaseToAdapter();
            mAdapter.clear();
        }
        */

        return super.onOptionsItemSelected(item);
    }

    public void scroolLast() {
        // 마직막 칸으로 스크롤
        int count = mListView.getCount();
        mListView.setSelection(count-1);
    }

    //Dialog custom;
    public void addPowerUsage(){
        if( !getPermission() ) {
            return;
        }

        AddUsage addusage = new AddUsage(this, new AddUsage.IAddUsageEventListener() {
            @Override
            public int customDialogEvent(Calendar calendar, int usage) {
                mDbHelper.open();

                //int lastUsage = mDbHelper.getLastUsage();
                //if(usage<=lastUsage){
                //    mDbHelper.close();
                //    return 0;
                //}

                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long datetime = calendar.getTimeInMillis();
                Cursor cursorCheck = mDbHelper.getMatch(datetime, "power");
                if( cursorCheck != null && cursorCheck.getCount() > 0 ) {
                    _log("중복->값 수정");
                    long id = cursorCheck.getInt(cursorCheck.getColumnIndex("_id"));
                    int oldusage = cursorCheck.getInt(cursorCheck.getColumnIndex("usage"));
                    mDbHelper.updateColumn(id, datetime, "power", usage, "no");

                }else {
                    _log("중복아님");
                    // 1. database
                    long id = mDbHelper.insertColumn(datetime, "power", usage, "no");
                }

                //
                mDbHelper.close();
                //
                setDatabaseToAdapterAfterAdd();

                scroolLast();
                LoadAd();
                ShowGuide();
                return 1;
            }
        });

        HideGuide();
        addusage.mStartWithHowto = IsShowGuideHowtoGuage();

        addusage.show();
    }

    public void addPowerUsage(final InfoClass infoNode){
        if( !getPermission() ) {
            return;
        }

        AddUsage addusage = new AddUsage(this, infoNode, new AddUsage.IAddUsageEventListener() {
            @Override
            public int customDialogEvent(Calendar calendar, int usage) {
                mDbHelper.open();

                //int lastUsage = mDbHelper.getLastUsage();
                //if(usage<=lastUsage){
                //   mDbHelper.close();
                //    return 0;
                //}

                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long datetime = calendar.getTimeInMillis();

                // 1. database
                long id = mDbHelper.insertColumn(datetime, "power", usage, "no");

                //
                mDbHelper.close();
                //
                setDatabaseToAdapterAfterAdd();

                return 1;

                //
                //if(getPermission()){
                //    mDbHelper.exportDB();
                //}
            }
        });

        addusage.show();
    }

    public void editPowerUsage(final InfoClass infoNode){
        AddUsage addusage = new AddUsage(this, infoNode, new AddUsage.IAddUsageEventListener() {
            @Override
            public int customDialogEvent(Calendar calendar, int usage) {
                mDbHelper.open();

                long datetime = calendar.getTimeInMillis();
                mDbHelper.updateColumn(infoNode._id, datetime, infoNode.type, usage, infoNode.deleted);

                //
                mDbHelper.close();
                //
                setDatabaseToAdapterAfterAdd();

                return 1;

                //
                //if(getPermission()){
                //    mDbHelper.exportDB();
                //}
            }
        });

        addusage.show();
    }

    public boolean getPermission(){
        boolean res = false;
        //
        // 사용자의 OS 버전이 마시멜로우 이상인지 체크한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 사용자 단말기의 권한이 허용되어 있는지 체크한다.
            int permissionResultRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int permissionResultWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // WRITE_EXTERNAL_STORAGE의 권한이 없을 때
            if (permissionResultRead == PackageManager.PERMISSION_DENIED ||
                    permissionResultWrite == PackageManager.PERMISSION_DENIED) {

                // 사용자가 권한 요구를 한번이라도 거부한 적이 있는지 조사한다.
                // 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("권한 좀..")
                            .setMessage("전기요금 폭탄 방지기를 사용하기 위해서는 단말기의 외부 메모리에 접근할 수 있는 권한이 꼭 필요합니다. 계속 하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivity.this, "권한 요청을 거부하셨네요. 다음에는 꼭 부탁드립니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }
                //최초로 권한을 요청할 때
                else {
                    // 권한을 Android OS 에 요청한다.
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                }
            }
            // 권한이 있을 때
            else {
                res = true;
            }
        }
        // 사용자의 OS 버전이 마시멜로우 이하일 때
        else {
            res = true;
        }

        return res;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            /* 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라
                내가 요청한 게 하나밖에 없기 때문에. 원래 같으면 for문을 돈다.*/
            int resultLength = grantResults.length;
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                int permissionResultRead = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                int permissionResultWrite = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionResultRead == PackageManager.PERMISSION_GRANTED &&
                        permissionResultWrite == PackageManager.PERMISSION_GRANTED) {
                    setDatabaseToAdapterAfterAdd();
                }
            }
            else {
                Toast.makeText(MainActivity.this, "권한 요청을 거부하셨네요. ㅠㅠ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
