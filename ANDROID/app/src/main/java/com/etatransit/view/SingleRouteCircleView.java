package com.etatransit.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.etatransit.util.Utils;

/**
 * Created by innovator on 5/4/2016.
 */
public class SingleRouteCircleView extends View {
    private static final String TAG = RouteCircleView.class.getSimpleName();

    private static final int CIRCLE_RADIUS_IN_DP = 5;
    private static final int SPACING_IN_DP = 8;


    private String mRoutes;

    private Paint mPaint;
    private float mRadius;
    private float mSpacing;



    public SingleRouteCircleView( Context context )
    {
        this(context, null);
    }

    public SingleRouteCircleView( Context context, AttributeSet attrs )
    {
        this(context, attrs, 0);
    }

    public SingleRouteCircleView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        mPaint = new Paint();
        mPaint.setAntiAlias( true );
        mPaint.setStyle( Paint.Style.FILL );

        Resources r = getContext().getResources();
        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_RADIUS_IN_DP, r.getDisplayMetrics());
        mSpacing = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, SPACING_IN_DP, r.getDisplayMetrics() );
    }


    @Override
    protected void onDraw( Canvas canvas )
    {
        super.onDraw(canvas);

        if(  mRoutes == null ){
            Log.e("in nulll", "null");
            return;
        }


        int x = (int) mRadius;
        int y = getHeight()/2;

        Log.e("in l", mRoutes);
        int color = Utils.convertColorStringToHex(mRoutes);

        mPaint.setColor( color );
        canvas.drawCircle( x, y, mRadius, mPaint );

        x += mSpacing + (mRadius * 2);

    }


    public void setData( String routeColor )
    {

        mRoutes = routeColor;
        invalidate();
    }
}

