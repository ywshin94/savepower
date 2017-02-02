package com.voidpointer.selfgauge;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import static android.R.attr.id;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
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
                mButtonNext.setVisibility(View.VISIBLE);
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMonthShift += 1;
                setDateRange();
                setDatabaseToAdapterAfterAdd();

                if( mMonthShift==0 ){
                    mButtonNext.setVisibility(View.INVISIBLE);
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

        setDatabaseToAdapter();
    }

    @Override
    protected void onResume() {
        _log("MainActivity... onResume");
        super.onResume();

        int checkDay = getPrefCheckDay();
        int powerType = getPrefPowerType();
        if( mCheckDay != checkDay || mPowerType != powerType ){
            _log("reset");
            mCheckDay = checkDay;
            mPowerType = powerType;
            setDateRange();
            setDatabaseToAdapterAfterAdd();
        }
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
        if( mCalStart.get(Calendar.DAY_OF_MONTH) < mCheckDay ) {
            mCalStart.add(Calendar.MONTH, -1);
        }

        _log( "mCheckDay : " + mCheckDay );
        if( mCheckDay == 100 ){
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
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String getDateTimeString(Calendar calendar){
        return String.format("%04d.%02d.%02d - %02d:%02d:%02d:%03d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MILLISECOND));
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
                    mInfoClass = new InfoClass( -1, mCalStart.getTimeInMillis(), type, 0, "no" );
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

        // 예상 요금
        if( itemCount>=2 && mMonthShift == 0) {
            int month_usage;
            String comment;

            if( bGijoonGap ) {
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

                comment = String.format("이번달에는 하루에 %.1f(kWh) 정도 사용하였습니다.\n다음 검침일까지 %d일 남았고,\n%.1f(kWh) 정도 더 사용할 예정입니다.",
                        usage_datetime * 24 * 60 * 60 * 1000, remain_days, remain_datetime * usage_datetime);
            }
            else{
                month_usage = 0;
                comment = "지난달 검침결과를 입력해야 예상전기요금을 확인할 수 있습니다.";
            }

            mInfoClass = new InfoClass(-2, mCalEnd.getTimeInMillis(), type, month_usage, comment);
            mAdapter.add(mInfoClass);
        }

        mCursor.close();
        mDbHelper.close();
    }

    private void setDatabaseToAdapterAfterAdd(){
        mAdapter.clear();
        int count = mAdapter.getCount();

        mAdapter.notifyDataSetChanged();

        setDatabaseToAdapter();

        int count2 = mAdapter.getCount();

        mAdapter.notifyDataSetChanged();
    }

    public void deleteData( int _id ){
        mDbHelper.open();
        mDbHelper.deleteColumn(_id);
        mDbHelper.close();

        setDatabaseToAdapterAfterAdd();
    }

    private void deleteAllData(){
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
        }else if (id == R.id.action_clear) {
            deleteAllData();
            setDatabaseToAdapter();
            mAdapter.clear();
        }

        return super.onOptionsItemSelected(item);
    }

    //Dialog custom;
    public void addPowerUsage(){
        AddUsage addusage = new AddUsage(this, new AddUsage.IAddUsageEventListener() {
            @Override
            public void customDialogEvent(Calendar calendar, int usage) {
                mDbHelper.open();

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

                if(getPermission()){
                    mDbHelper.exportDB();
                }
            }
        });

        addusage.show();
    }

    public void addPowerUsage(final InfoClass infoNode){
        AddUsage addusage = new AddUsage(this, infoNode, new AddUsage.IAddUsageEventListener() {
            @Override
            public void customDialogEvent(Calendar calendar, int usage) {
                mDbHelper.open();

                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long datetime = calendar.getTimeInMillis();

                // 1. database
                long id = mDbHelper.insertColumn(datetime, "power", usage, "no");

                //
                mDbHelper.close();
                //
                setDatabaseToAdapterAfterAdd();

                //
                if(getPermission()){
                    mDbHelper.exportDB();
                }
            }
        });

        addusage.show();
    }

    public void editPowerUsage(final InfoClass infoNode){
        AddUsage addusage = new AddUsage(this, infoNode, new AddUsage.IAddUsageEventListener() {
            @Override
            public void customDialogEvent(Calendar calendar, int usage) {
                mDbHelper.open();

                long datetime = calendar.getTimeInMillis();
                mDbHelper.updateColumn(infoNode._id, datetime, infoNode.type, usage, infoNode.deleted);

                //
                mDbHelper.close();
                //
                setDatabaseToAdapterAfterAdd();

                //
                if(getPermission()){
                    mDbHelper.exportDB();
                }
            }
        });

        addusage.show();
    }

    public boolean getPermission(){
        boolean res = false;
        //
        /* 사용자의 OS 버전이 마시멜로우 이상인지 체크한다. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* 사용자 단말기의 권한 중 "전화걸기" 권한이 허용되어 있는지 체크한다.
            *  int를 쓴 이유? 안드로이드는 C기반이기 때문에, Boolean 이 잘 안쓰인다.
            */
            int permissionResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            /* WRITE_EXTERNAL_STORAGE의 권한이 없을 때 */
            // 패키지는 안드로이드 어플리케이션의 아이디다.( 어플리케이션 구분자 )
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                /* 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                */
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 단말기의 \"전화걸기\" 권한이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }
                //최초로 권한을 요청할 때
                else {
                    // WRITE_EXTERNAL_STORAGE 권한을 Android OS 에 요청한다.
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                }
            }
            /* WRITE_EXTERNAL_STORAGE 권한이 있을 때 */
            else {
                res = true;
            }
        }
        /* 사용자의 OS 버전이 마시멜로우 이하일 떄 */
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    mDbHelper.exportDB();
                }
            }
            else {
                Toast.makeText(MainActivity.this, "권한 요청을 거부했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
