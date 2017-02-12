package com.voidpointer.selfgauge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
//import static com.voidpointer.selfgauge.MainActivity.mContext;

/**
 * Created by SHIN on 2016-08-22.
 */
public class SettingActivity extends PreferenceActivity {
    public static Context mContext;
    private static final String TAG = "ywshin";
    void _log(String log){
        Log.v(TAG, log);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment() )
                .commit();

        mContext = this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        _log("SettingActivity... onOptionsItemSelected");
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        _log("SettingActivity... onBackPressed");
        super.onBackPressed();
    }

    // PreferenceFragment 클래스 사용
    public static class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener  {

        private static final String TAG = "ywshin";
        void _log(String log){
            Log.v(TAG, log);
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            _log( "MyPreferenceFragment... onCreate" );
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setting);
        }

        public boolean fileCopy(String source, String target) {
            try {
                File sourceFile = new File(source);
                File targetFile = new File(target);
                FileChannel src = new FileInputStream(sourceFile).getChannel();
                FileChannel dst = new FileOutputStream(targetFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
            catch (Exception e) {
                String errorMessage = e.getMessage();
                return false;
            }
        }

        public void importDB(){
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mContext);
            alert_confirm.setMessage("데이터 파일을 교체합니다. 기존의 데이터는 삭제됩니다. 계속 진행할까요?")
                    .setCancelable(false)
                    .setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    File download = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
                                    String target = DBHelper.getDbPath();
                                    String source = download + "\\" + DBHelper.getDbName();

                                    String message;
                                    if( fileCopy(source, target) ){
                                        message = "데이터 복구를 완료했습니다. 기존의 데이터는 삭제 되었습니다.";
                                    }else{
                                        message = "데이터 복구를 실패했습니다. 이 기기의 download 폴더에 복구할 selfgauge.db 파일이 있는지 다시한번 확인하세요.";
                                    }
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                }
                            })
                    .setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                }
                            });
            AlertDialog alert = alert_confirm.create();
            alert.show();
        }

        public void exportDB(){
            String DBPath = DBHelper.getDbPath();
            File file = new File(DBPath);
            if (!file.exists()) {
                return;
            }
            final Uri fileUri = Uri.fromFile(file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            //    shareIntent.putExtra(Intent.EXTRA_EMAIL,new String[] { "" });
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, String.format("요금폭탄 방지기 데이터 백업"));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(shareIntent, "E-Mail 보낼 앱을 선택하세요"));
        }

        public void mailToMe(String mailto){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_EMAIL,new String[] { mailto });
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, String.format("[요금폭탄 방지기] 질문 있습니다."));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(shareIntent, "E-Mail 보낼 앱을 선택하세요"));
        }

        public void deleteDB(){
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mContext);
            alert_confirm.setMessage("입력한 모든 데이터가 삭제됩니다. 다시한번 생각해 보세요. 계속 진행할까요?")
                    .setCancelable(false)
                    .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                ((MainActivity)MainActivity.mContext).deleteAllData();
                                ((MainActivity)MainActivity.mContext).setDatabaseToAdapterAfterAdd();
                            }
                        })
                    .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                            }
                        });
            AlertDialog alert = alert_confirm.create();
            alert.show();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            String mKey = preference.getKey();
            if(mKey.equals("exportDB")){
                _log("exportDB");
                exportDB();
            }
            else if(mKey.equals("importDB")){
                _log("importDB");
                importDB();
            }
            else if(mKey.equals("deleteDB")){
                _log("deleteDB");
                deleteDB();
            }
            else if(mKey.equals("mailDeveloper")){
                _log("sendMail");
                mailToMe("ywshin94@gmail.com");
            }
            else if(mKey.equals("mailDesigner")){
                _log("sendMail");
                mailToMe("shinsein1004@gmail.com");
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Preference preference = findPreference(s);
            if (s.equals("powerType")) {
                preference.setSummary(((ListPreference) preference).getEntry());
            }else if (s.equals("checkDay")) {
                preference.setSummary(((ListPreference) preference).getEntry());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            _log( "MyPreferenceFragment... onResume" );
            getPreferenceManager()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            _log( "MyPreferenceFragment... onPause" );
            getPreferenceManager()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
