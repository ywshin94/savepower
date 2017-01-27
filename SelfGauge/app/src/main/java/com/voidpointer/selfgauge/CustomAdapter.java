package com.voidpointer.selfgauge;

import android.content.Context;
import android.icu.text.IDNA;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by SHIN on 2016-08-16.
 */
public class CustomAdapter extends BaseAdapter {
    private ArrayList<InfoClass> mInfoList;
    private RecyclerView.ViewHolder mViewHolder;

    public CustomAdapter(){
        mInfoList = new ArrayList<InfoClass>();
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(InfoClass _node) {
        mInfoList.add(_node);
    }

    public void remove(int _position) {
        mInfoList.remove(_position);
    }

    public void clear() {
        mInfoList.clear();
    }

    public int getUsageThisMonth(int usage){
        return usage - mInfoList.get(0).usage;
    }

    @Override
    public int getCount() {
        return mInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return mInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public boolean isImsiNode( InfoClass infoNode ){
        return (infoNode._id < 0);
    }

    public boolean isStartImsiNode( InfoClass infoNode ){
        return (infoNode._id == -1);
    }

    public boolean isEndImsiNode( InfoClass infoNode ){
        return (infoNode._id == -2);
    }

    // 상황에 따라서 날짜만 표시할지, 시간까지 표시할지 선택하기 위해서 함수를 따로 만듬
    public void setDateString( InfoClass infoNode, View v ){
        boolean menuExpand = false;
        RelativeLayout menuLayout = (RelativeLayout)v.findViewById(R.id.group3);
        if( menuLayout.getVisibility() == View.VISIBLE ) {
            menuExpand = true;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(infoNode.datetime);

        TextView text1 = (TextView) v.findViewById(R.id.textDate);
        if( isStartImsiNode(infoNode) ) {
            text1.setText(MainActivity.getDateString(cal) + " (기준 검침일)");
        }
        else {
            if (menuExpand) {
                text1.setText(MainActivity.getDateTimeString(cal));
            } else {
                text1.setText(MainActivity.getDateString(cal));
            }
        }
    }

    public void setUsageString(InfoClass infoNode, View v){
        TextView text = (TextView) v.findViewById(R.id.textUsageTotal);

        if( isStartImsiNode(infoNode) ){
            text.setText("지난달 검침결과를 입력해 주세요.");
        }
        else {
            text.setText(String.format("%d kWh", infoNode.usage));
        }
    }

    public void setUsageThisMonthString(InfoClass infoNode, View v){
        TextView text = (TextView) v.findViewById(R.id.textUsage);

        if( isStartImsiNode(infoNode) ){
            text.setText("");
        }
        else if( isEndImsiNode(infoNode) ){
            text.setText(String.format("%d kWh / %d kWh", infoNode.usage,  getUsageThisMonth(infoNode.usage)));
        }
        else{
            text.setText(String.format("%d kWh", getUsageThisMonth(infoNode.usage)));
        }
    }

    public void setChargeString( InfoClass infoNode, View v){
        TextView text = (TextView) v.findViewById(R.id.textCharge);
        if( isStartImsiNode(infoNode) ){
            text.setText("");
        }
        else if( isEndImsiNode(infoNode) ){
            if( infoNode.usage > 0 ) {
                int charge = getElectricityBill(getUsageThisMonth(infoNode.usage));
                text.setText(String.format("예상전기요금 : %s원", getMoneyString(charge)));
            }
            else{
                text.setText(String.format("예상전기요금 : ______원"));
            }
        }
        else{
            int charge = getElectricityBill(getUsageThisMonth(infoNode.usage));
            text.setText(String.format("%s원", getMoneyString(charge)));
        }
    }

    public void setForecastComment( InfoClass infoNode, View v){
        TextView text = (TextView) v.findViewById(R.id.textComment);
        if( isEndImsiNode(infoNode) ){
            text.setText(infoNode.deleted);   //
        }
    }

    public void setChargeInfoString( InfoClass infoNode, View v){
        //TextView text = (TextView) v.findViewById(R.id.textInfo);
        //if( isEndImsiNode(infoNode) ){
        //    text.setText(infoNode.deleted);   //
        //}
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        final int pos = position;
        final InfoClass infoNode = mInfoList.get(pos);

        if( infoNode._id == -1 ) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_start, parent, false);

            setDateString( infoNode, convertView );
            setUsageString( infoNode, convertView );
        }
        else if( infoNode._id == -2 ) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_end, parent, false);

            setForecastComment( infoNode, convertView );

            //setDateString( infoNode, convertView );
            //setUsageString( infoNode, convertView );
            setUsageThisMonthString( infoNode, convertView );
            setChargeString( infoNode, convertView );
        }
        else {
            // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
            if (convertView == null) {
                Log.v("ywshin", String.format("convertView == null, position: %d", position));
                // view가 null일 경우 커스텀 레이아웃을 얻어 옴
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);
            }

            setDateString( infoNode, convertView );
            setUsageString( infoNode, convertView );
            setUsageThisMonthString( infoNode, convertView );
            setChargeString( infoNode, convertView );

            // 삭제 버튼
            Button btnDelete = (Button) convertView.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    //Toast.makeText(context, "삭제 클릭 : " + pos, Toast.LENGTH_SHORT).show();
                    RelativeLayout mainLayout = (RelativeLayout)((ViewGroup)v.getParent()).findViewById(R.id.group3);
                    mainLayout.setVisibility(View.GONE);
                    ((MainActivity)MainActivity.mContext).deleteData( infoNode._id );
                }
            });

            // 수정 버튼
            Button btnEdit = (Button) convertView.findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(context, "수정 클릭 : " + pos, Toast.LENGTH_SHORT).show();
                    ((MainActivity)MainActivity.mContext).editPowerUsage( infoNode );
                }
            });


            // 리스트 아이템을 터치 했을 때 이벤트 발생
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 터치 시 해당 아이템 이름 출력
                    //Calendar cal = Calendar.getInstance();
                    //cal.setTimeInMillis(mInfoList.get(pos).datetime);
                    //Toast.makeText(context, "리스트 클릭 : " + cal.toString(), Toast.LENGTH_SHORT).show();
                    RelativeLayout mainLayout=(RelativeLayout)v.findViewById(R.id.group3);
                    if( mainLayout.getVisibility() == View.VISIBLE ) {
                        mainLayout.setVisibility(View.GONE);
                    }else{
                        mainLayout.setVisibility(View.VISIBLE);
                    }

                    InfoClass infoNode = mInfoList.get(pos);
                    setDateString( infoNode, v );
                }
            });

            // 리스트 아이템을 길게 터치 했을 떄 이벤트 발생
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 터치 시 해당 아이템 이름 출력
                    //Calendar cal = Calendar.getInstance();
                    //cal.setTimeInMillis(mInfoList.get(pos).datetime);
                    //Toast.makeText(context, "리스트 롱~클릭 : " + cal.toString(), Toast.LENGTH_SHORT).show();
                    if( mInfoList.get(pos).selected ){
                        mInfoList.get(pos).selected = false;
                    }else{
                        mInfoList.get(pos).selected = true;
                    }

                    return true;
                }
            });
        }




        if (infoNode.selected) {
            convertView.setBackgroundResource(R.color.listBackSelect);
        } else{
            convertView.setBackgroundResource(R.color.listBack);
        }

        return convertView;
    }

    public int getElectricityBill(int usage) {
        int max_count = 6;
        int[] gibon = new int[6];
        double[] danwi = new double[6];

        int powerType = ((MainActivity)MainActivity.mContext).getPrefPowerType();
        if( powerType == 0 ) {
            Log.v("ywshin", "저압");
            // 저압
            gibon[0] = 410;
            gibon[1] = 910;
            gibon[2] = 1600;
            gibon[3] = 3850;
            gibon[4] = 7300;
            gibon[5] = 12940;

            danwi[0] = 60.7;
            danwi[1] = 125.9;
            danwi[2] = 187.9;
            danwi[3] = 280.6;
            danwi[4] = 417.7;
            danwi[5] = 709.5;
        }else{
            Log.v("ywshin", "고압");
            // 고압
            gibon[0] = 410;
            gibon[1] = 730;
            gibon[2] = 1260;
            gibon[3] = 3170;
            gibon[4] = 6060;
            gibon[5] = 10760;

            danwi[0] = 57.6;
            danwi[1] = 98.9;
            danwi[2] = 147.3;
            danwi[3] = 215.6;
            danwi[4] = 325.7;
            danwi[5] = 574.6;
        }

        int electotal = 0;
        int count = (int)((usage-1)/100) + 1;
        if(count>max_count){
            count = max_count;
        }

        int bokjihalin = 0;
        int sayongryo = 0;
        for(int group=0; group<count; group++) {
            int guganUsage;
            if(group==count-1){
                guganUsage = usage - 100*(count-1);
            }else {
                guganUsage = 100;
            }
            sayongryo+=(int)(danwi[group]*guganUsage+0.5);
        }

        electotal=gibon[count-1]+sayongryo-bokjihalin;

        int bugase=(int)(electotal*0.1+0.5);
        int gibangigeum=((int)(electotal*0.037/10.))*10;
        int yogeum=((int)((electotal+bugase+gibangigeum)/10.))*10;

        //_log(String.format("사용량(%d) - 기본료(%d), 사용료(%d), 전기요금계(%d), 부가세(%d), 기반기금(%d), 청구요금(%d)", usage, gibon[count-1], sayongryo, electotal, bugase, gibangigeum, yogeum));

        /*
        전기요금 청구액 계산방법(TV수신료 별도)
        ① 기본요금(원단위 미만 절사)
        ② 사용량요금(원단위 미만 절사)
        ③ 전기요금계 = ① + ② - 복지할인
        ④ 부가가치세(원단위 미만 4사5입) = ③ × 10%
        ⑤ 전력산업기반기금(10원 미만 절사) = ③ × 3.7%
        ⑥ 청구요금 합계(10원 미만 절사) = ③ + ④ + ⑤

        자동이체 할인 적용 시 계산(끝자리 수 처리는 상기와 동일)
        a. 상기 전기요금계(③) - (전월 납부 전기요금 × 0.01) [1,000원 한도]
        b. 당월 청구요금 = a + 부가가치세(a × 10%) + 전력산업기반기금(a × 3.7%)
        */
        return yogeum;
    }

    public String getMoneyString(int money){
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(money);
    }

}