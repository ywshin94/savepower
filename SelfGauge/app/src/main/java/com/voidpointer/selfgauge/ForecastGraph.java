package com.voidpointer.selfgauge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.webkit.DateSorter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.voidpointer.selfgauge.R.color.colorAccent;

/**
 * Created by ywshin on 2017-04-02.
 */

public class ForecastGraph extends View {
    class DataSet{
        long mDateTime;
        int mCharge;
        int mUsage;
        public DataSet(long datetime, int charge, int usage){
            mDateTime = datetime;
            mCharge = charge;
            mUsage = usage;
        }
    }

    Canvas mCanvas;
    Paint mPaint;

    private int mFontSize = getPx(12);
    private int mTextColor = Color.argb(255, 100, 100, 100);
    private int mBackColor= Color.argb(50, 220, 220, 220);
    private int mAxisColor = Color.argb(255, 100, 100, 100);
    private int mGridColor = Color.argb(255, 200, 200, 200);

    private int mPaddingTop = getPx(10);
    private int mPaddingBottom = getPx(22)+mFontSize;
    private int mPaddingLeft = getPx(20);
    private int mPaddingRight = getPx(20);

    private long mMaxXValue = 0;
    private int mMaxYValue = 0;
    private List mValueList = new ArrayList();
    private int[] mNoojin = new int[2];
    private String[] mNoojinText = new String[2];

    public ForecastGraph(Context context) {
        super(context);

        mPaint = new Paint();
        mPaint.setDither(true); // enable dithering
        mPaint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
    }

    public void setNoojin( int n1, int n2){
        mNoojin[0] = n1;
        mNoojin[1] = n2;
        mNoojinText[0] = "누진1구간(200kWh)";
        mNoojinText[1] = "누진2구간(400kWh)";
    }

    public void setData( long datetime, int charge, int usage ){
        DataSet data = new DataSet(datetime, charge, usage);
        mValueList.add(data);
    }

    public void setMaxXValue( long datetime ){
        mMaxXValue = datetime;
    }

    public void setMaxYValue( int charge ){
        mMaxYValue = (int)(charge*1.1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mFontSize);
        drawBack();
        drawGraphAll();
    }

    private void drawBack(){
        if(mMaxXValue==0) {
            return;
        }
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(mBackColor);
        drawable.setBounds(0, 0, getWidth(), getHeight());
        drawable.draw(mCanvas);

        mPaint.setColor(mAxisColor);

        int wndWidth = getWidth();
        int wndHeight = getHeight();
        //mCanvas.drawLine(mPaddingLeft, mPaddingTop, wndWidth-mPaddingRight, mPaddingTop, mPaint);
        mCanvas.drawLine(mPaddingLeft, wndHeight-mPaddingBottom, wndWidth-mPaddingRight, wndHeight-mPaddingBottom, mPaint);

        //mCanvas.drawLine(getXPx(0), mPaddingTop, getXPx(0), wndHeight-mPaddingTop, mPaint);
        //mCanvas.drawLine(getXPx(mMaxXValue), mPaddingTop, getXPx(mMaxXValue), wndHeight-mPaddingBottom+getPx(2), mPaint);
        mCanvas.drawLine(getXPx(mMaxXValue), wndHeight-mPaddingBottom, getXPx(mMaxXValue), wndHeight-mPaddingBottom+getPx(2), mPaint);

        //누진구간 표시
        for( int i=0; i<2; i++) {
            mCanvas.drawLine(mPaddingLeft, getYPx(mNoojin[i]), wndWidth - mPaddingRight, getYPx(mNoojin[i]), mPaint);
            mCanvas.drawText(mNoojinText[i], mPaddingLeft, getYPx(mNoojin[i])+mFontSize+getPx(1), mPaint);

            if( getYPx(mNoojin[i]) > mPaddingTop+mFontSize-getPx(1)) {
                drawMoneyString(mNoojin[i]);
            }
        }

        // 날짜표시
        for( int i=0; i<3; i++ ) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(MainActivity.mCalStart.getTimeInMillis());
            cal.add(Calendar.DATE, i*10);

            long xamount = cal.getTimeInMillis()-MainActivity.mCalStart.getTimeInMillis();
            int x = getXPx(xamount);
            drawDateString(x, cal);
            int y = getHeight()-mPaddingBottom;
            mCanvas.drawLine(x, y, x, y+getPx(2), mPaint);
        }

        drawDateString(wndWidth-mPaddingRight, MainActivity.mCalEnd);

    }

    private void drawDateString( int xvalue, Calendar date ){
        String dateStr = MainActivity.getDateStringShort(date);
        float width = mPaint.measureText(dateStr, 0, dateStr.length());
        mCanvas.drawText(dateStr, xvalue-width/2, getHeight()-mPaddingBottom+mFontSize+getPx(3), mPaint);
    }

    private void drawMoneyString( int money ){
        String value = CustomAdapter.getMoneyString(money) + "원";
        float width = mPaint.measureText(value, 0, value.length());
        mCanvas.drawText(value, getWidth() - mPaddingRight - width, getYPx(money) - 10, mPaint);
    }

    private void drawForecastString( int money, int usage ){
        String value = String.format("예상 : %s원 (%dkWh)", CustomAdapter.getMoneyString(money), usage);
        float width = mPaint.measureText(value, 0, value.length());
        //int x = (int)(getWidth() - mPaddingRight - width);
        int x =  (int)(getWidth()/2.5);
        int y = getYPx(money);

        int colorAccent = ContextCompat.getColor(getContext(), R.color.colorAccent);
        setDotLine(colorAccent, 1);
        drawDotLine(x, y, getWidth()-mPaddingRight, y);


        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(colorAccent);
        //mCanvas.drawRect(x,y,x+width,y-getPx(12), mPaint);

        //mPaint.setColor(ContextCompat.getColor(getContext(), R.color.listBack) );
        mCanvas.drawText(value, x, y-10, mPaint);
    }

    private int getDataSize(){
        return mValueList.size();
    }

    private DataSet getDataSet(int index){
        return (DataSet)(mValueList.get(index));
    }

    private void setDotLine(int color, int width){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(width);
        mPaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));
        mPaint.setColor(color);
    }

    private void setSolidLine(int color, int width){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(width);
        mPaint.setColor(color);
    }

    private void drawDotLine(int x1, int y1, int x2, int y2) {
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        mCanvas.drawPath(path, mPaint);
    }

    private void drawGraphAll(){
        if(mMaxXValue==0) {
            return;
        }

        if(getDataSize()<2){
            return;
        }

        setSolidLine(Color.BLACK, 3);
        int x1=0, y1=0, x2=0, y2=-1;
        for(int i=0; i<getDataSize()-1; i++){
            if(y2!=0) {
                x1 = getXPx(getDataSet(i).mDateTime);
                y1 = getYPx(getDataSet(i).mCharge);
            }

            x2 = getXPx(getDataSet(i+1).mDateTime);
            y2 = getYPx(getDataSet(i+1).mCharge);

            if( i==getDataSize()-2 ){
                int colorAccent = ContextCompat.getColor(getContext(), R.color.colorAccent);
                setDotLine(colorAccent, 4);
                drawDotLine(x1, y1, x2, y2);
            }
            else {
                mCanvas.drawLine(x1, y1, x2, y2, mPaint);
            }
        }

        setSolidLine(Color.BLACK, 3);
        for(int i=0; i<getDataSize(); i++){
            int x = getXPx(getDataSet(i).mDateTime);
            int y = getYPx(getDataSet(i).mCharge);

            if( i==getDataSize()-1 ){
                int colorAccent = ContextCompat.getColor(getContext(), R.color.colorAccent);
                setSolidLine(colorAccent, 4);
            }
            mCanvas.drawCircle(x, y, 8, mPaint);

            if(i==getDataSize()-1){
                drawForecastString(getDataSet(i).mCharge, getDataSet(i).mUsage);
            }
        }
    }

    private int getPx( int dp ){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    private float getStepHeight(){
        int h = getHeight()-mPaddingTop-mPaddingBottom;
        float res = (float)h/(float)(mMaxYValue-1);
        return res;
    }

    private int getXPx( long datetime ){
        int w = getWidth()-mPaddingRight-mPaddingLeft;
        float ratio = (float)((double)w/(double)mMaxXValue);

        return (int)(mPaddingLeft + ratio*datetime+ 0.5);
    }

    private int getYPx( int value ){
        return (int)(getHeight() - mPaddingBottom - getStepHeight()*value + 0.5);
    }
}
