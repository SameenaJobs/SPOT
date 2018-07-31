package com.etatransit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.etatransit.R;

/**
 * Created by mark on 10/2/14.
 */
public class TriangleView extends View
{
    private Paint mPaint = new Paint();
    private Path mPath = new Path();



    public TriangleView(Context context) {
        this(context, null);
    }

    public TriangleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TriangleView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TriangleView);

        int color = a.getColor(R.styleable.TriangleView_triangleColor, 0xFFFFFF);
        mPaint.setColor( color );
        mPaint.setAntiAlias( true );
        mPaint.setStyle( Paint.Style.FILL );

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        mPath.reset();
        mPath.moveTo( 0, getHeight() );
        mPath.lineTo(getWidth() / 2, 0);
        mPath.lineTo( getWidth(), getHeight() );
        mPath.lineTo( 0, getHeight() );
        mPath.close();

        canvas.drawPath( mPath, mPaint );
    }
}
