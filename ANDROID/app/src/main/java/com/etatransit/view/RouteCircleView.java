package com.etatransit.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.etatransit.EtaAppController;
import com.etatransit.EtaApplication;
import com.etatransit.model.Route;
import com.etatransit.model.RouteCoordinates;
import com.etatransit.util.Utils;

import java.util.List;

/**
 * Created by mark on 10/6/14.
 */
public class RouteCircleView extends View
{
    private static final String TAG = RouteCircleView.class.getSimpleName();

    private static final int CIRCLE_RADIUS_IN_DP = 5;
    private static final int SPACING_IN_DP = 8;


    private int mStopId = -1;
    private List<Route> mRoutes;

    private Paint mPaint;
    private float mRadius;
    private float mSpacing;



    public RouteCircleView( Context context )
    {
        this(context, null);
    }

    public RouteCircleView( Context context, AttributeSet attrs )
    {
        this(context, attrs, 0);
    }

    public RouteCircleView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        mPaint = new Paint();
        mPaint.setAntiAlias( true );
        mPaint.setStyle( Paint.Style.FILL );

        Resources r = getContext().getResources();
        mRadius = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, CIRCLE_RADIUS_IN_DP, r.getDisplayMetrics() );
        mSpacing = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, SPACING_IN_DP, r.getDisplayMetrics() );
    }


    @Override
    protected void onDraw( Canvas canvas )
    {
        super.onDraw(canvas);

        if( mStopId <= -1 || mRoutes == null )
            return;

        int x = (int) mRadius;
        int y = getHeight()/2;

        for( Route r : mRoutes )
        {
            if( r.stops.contains( String.valueOf( mStopId ) ) )
            {
                int color = Utils.convertColorStringToHex( r.color );

                mPaint.setColor( color );
                canvas.drawCircle( x, y, mRadius, mPaint );

                x += mSpacing + (mRadius * 2);
            }
        }

    }


    public void setData( int stopId, List<Route> routes )
    {
        mStopId = stopId;
        mRoutes = routes;
        invalidate();
    }
}
