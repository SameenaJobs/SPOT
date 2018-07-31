package com.etatransit.fragment;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.etatransit.R;
import com.etatransit.event.BusProvider;
import com.etatransit.event.CloseHelpFragmentEvent;
import com.etatransit.util.ListTagHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by mark on 11/4/14.
 */
public class HelpFragment extends Fragment
{
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_help, container );
    }


    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );
        TextView tv1 = (TextView) view.findViewById( R.id.bullet_point_text );
        tv1.setText( Html.fromHtml( getString( R.string.text_help_bullet_points ), null, new ListTagHandler() ) );

        ImageView feedbackIcon = (ImageView) view.findViewById( R.id.help_icon );
        Picasso.with(view.getContext()).load( R.drawable.ic_menu_help ).transform( new Transformation()
        {
            @Override
            public Bitmap transform( Bitmap source )
            {
                BitmapDrawable drawable = new BitmapDrawable( Resources.getSystem(), source );
                Bitmap result = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );
                Canvas canvas = new Canvas( result );
                drawable.setBounds( 0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight() );
                drawable.setColorFilter( Resources.getSystem().getColor( android.R.color.white ), PorterDuff.Mode.SRC_IN );
                drawable.draw( canvas );
                drawable.setColorFilter( null );
                drawable.setCallback( null );

                if( result != source ) {
                    source.recycle();
                }
                return result;
            }

            @Override
            public String key()
            {
                return "help";
            }
        }).into( feedbackIcon );

        view.findViewById( R.id.help_close_btn ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                BusProvider.getInstance().post( new CloseHelpFragmentEvent() );
            }
        });

        view.findViewById( R.id.container ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                //no-op
            }
        });
    }
}
