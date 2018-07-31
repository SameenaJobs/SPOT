package com.etatransit.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.event.BusProvider;
import com.etatransit.event.CloseFeedbackFragmentEvent;
import com.splunk.mint.Mint;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 11/4/14.
 */
public class FeedbackFragment extends Fragment
{
    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private View mFeedbackContent;
    private TextView mFeedbackResponse;
    private EditText mEmail;
    private EditText mFeedback;
    public String email_text;
    public String feedback_text;
    public String endpointUrl = null;

    public  String regEx =
            "^(([w-]+.)+[w-]+|([a-zA-Z]{1}|[w-]{2,}))@"
                    +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9]).([0-1]?"
                    +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])."
                    +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9]).([0-1]?"
                    +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    +"([a-zA-Z]+[w-]+.)+[a-zA-Z]{2,4})$";

    private Handler mHandler = new Handler();


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_feedback, container );

    }


    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated(view, savedInstanceState);
        mScrollView = (ScrollView) view.findViewById( R.id.scroll_content );
        mProgressBar = (ProgressBar) view.findViewById( R.id.progress_bar );
        mFeedbackContent = mScrollView;
        mFeedbackResponse = (TextView) view.findViewById( R.id.feedback_response );
        mEmail = (EditText) view.findViewById( R.id.email_text );
        mFeedback = (EditText) view.findViewById( R.id.feedback_text );

        Button btn = (Button) view.findViewById( R.id.submit_button );
        btn.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ((!mEmail.getText().toString().equals("") && !mEmail.getText().toString().equals(regEx)) &&
                        (!mFeedback.getText().toString().equals("") && !mFeedback.getText().toString().equals(regEx))) {

                    if ((!mEmail.getText().toString().equals(""))) {
                        email_text = mEmail.getText().toString();
                        feedback_text = mFeedback.getText().toString();
                        int at_the_rate = email_text.indexOf("@");
                        int dot_com = email_text.indexOf(".");

                        if (!(email_text.contains("@")) || (!(email_text.contains(".")))) {

                            Toast.makeText(getActivity(),
                                    "Invalid Email", Toast.LENGTH_SHORT).show();

                        }else if ((at_the_rate <= 1) && (dot_com < 1)) {       /*dot_com_issue*/

                            Toast.makeText(getActivity(),
                                    "Invalid Email", Toast.LENGTH_SHORT).show();


                        }else if ((dot_com <= 1) && (at_the_rate < 1)) {     /*at_the_rate_issue*/

                            Toast.makeText(getActivity(),
                                    "Invalid Email", Toast.LENGTH_SHORT).show();


                        }else if ((email_text.length()-dot_com < 2)) {

                            Toast.makeText(getActivity(),
                                    "Invalid Email", Toast.LENGTH_SHORT).show();


                        }else if (!((at_the_rate == -1)) && !((dot_com == -1)) && !((dot_com + 2 >= email_text.length()))) {
                            showProgressBar();
                            hideKeyboard();
                            postData();
                        }
                    }
                }else if ((!mEmail.getText().toString().equals("") )) {
                    Toast.makeText(getActivity(),
                            "Feedback field is empty", Toast.LENGTH_SHORT).show();

                }else if ((!mFeedback.getText().toString().equals("")) ) {
                    Toast.makeText(getActivity(),
                            "Email field is empty", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(),
                            "Email and Feedback field are empty", Toast.LENGTH_SHORT).show();
                }
            }

        });


        ImageView feedbackIcon = (ImageView) view.findViewById( R.id.feedback_icon );
        Picasso.with( view.getContext() ).load( R.drawable.ic_menu_feedback ).transform( new Transformation()
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
                return "feedback";
            }
        }).into( feedbackIcon );

        view.findViewById( R.id.feedback_close_btn ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mEmail.setText("");
                mFeedback.setText("");
                hideKeyboard();
                mFeedbackResponse.setText("");
                BusProvider.getInstance().post( new CloseFeedbackFragmentEvent() );

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


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEmail.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(mFeedback.getWindowToken(), 0);
    }


    private void postData()
    {

        String serviceUrl = EtaAppController.getInstance().getSelectedAgency().serviceUrl;
        Mint.leaveBreadcrumb("FeedbackFragment postData serviceUrl"+serviceUrl );
        if (null != serviceUrl && serviceUrl.length() > 0 )
        {
            int endIndex = serviceUrl.lastIndexOf( "/" );
            if( endIndex != -1 )
            {
                endpointUrl = serviceUrl.substring( 0, endIndex );
            }
        }

        new AsyncTask<Void, Void, Boolean>()
        {

            @Override
            protected Boolean doInBackground( Void... params )
            {
                HttpClient httpclient = new DefaultHttpClient();
                try {
                    GetText();
                    HttpPost httppost = new HttpPost( endpointUrl.concat("/service.php") );
                    List<BasicNameValuePair> nameValuePairs = new ArrayList<>(9);
                    nameValuePairs.add( new BasicNameValuePair( "service", "save_rider_feedback" ) );
                    nameValuePairs.add( new BasicNameValuePair( "subject", endpointUrl));
                    nameValuePairs.add( new BasicNameValuePair( "email",email_text));
                    nameValuePairs.add( new BasicNameValuePair( "feedback",feedback_text));
                    nameValuePairs.add( new BasicNameValuePair( "rating", "5" ) );
                    nameValuePairs.add( new BasicNameValuePair( "location", "Could not find User GPS Location" ) );
                    nameValuePairs.add( new BasicNameValuePair( "actions","nil " ) );
                    nameValuePairs.add( new BasicNameValuePair( "platform", "Android" ) );
                    nameValuePairs.add( new BasicNameValuePair( "token", "TESTING" ) );
                    httppost.setEntity( new UrlEncodedFormEntity( nameValuePairs ) );

                    HttpResponse httpresponse = httpclient.execute( httppost );
                    //String userAgent = new WebView(this).getSettings().getUserAgentString();
                    //Log.d("Agent :", userAgent+ "");
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute( Boolean success )
            {
                super.onPostExecute(success);
                mScrollView.smoothScrollTo( 0, 0 );

                if( success )
                {
                    mFeedbackResponse.setText( getString( R.string.text_feedback_response ) );
                    mFeedbackResponse.setTextColor( getResources().getColor( R.color.eta_message_level_normal ) );
                }else
                {
                    mFeedbackResponse.setText( getString( R.string.text_feedback_error_response ) );
                    mFeedbackResponse.setTextColor( getResources().getColor( R.color.eta_message_level_severe ) );
                }
                mFeedbackResponse.setVisibility( View.VISIBLE );

                resetAndShowContent(success);
            }
        }.execute();
    }

    private void GetText()
    {
        email_text = mEmail.getText().toString();
        feedback_text = mFeedback.getText().toString();

    }


    private void showProgressBar()
    {
        if( !isAdded() )
            return;

        mProgressBar.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_in ) );
        mFeedbackContent.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_out ) );

        mProgressBar.setVisibility( View.VISIBLE );
        mFeedbackContent.setVisibility( View.GONE );
    }


    private void resetAndShowContent( boolean success )
    {
        mEmail.setText( "" );
        mFeedback.setText( "" );

        mProgressBar.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_out ) );
        mFeedbackContent.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_in ) );

        mProgressBar.setVisibility( View.GONE );
        mFeedbackContent.setVisibility( View.VISIBLE );
    }
}


