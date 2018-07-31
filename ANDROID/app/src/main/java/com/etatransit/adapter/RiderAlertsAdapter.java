package com.etatransit.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etatransit.R;
import com.etatransit.activity.CreateRiderAlert;
import com.etatransit.database.DatabaseHandler;
import com.splunk.mint.Mint;

/**
 * Created by innovator on 8/23/2016.
 */
public class RiderAlertsAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;
    Context context;
    String alertId;
    private Cursor cursor;

    // Default constructor
    public RiderAlertsAdapter(Context context, Cursor flags) {
        super(context, flags);
        this.context = context;
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public void bindView(View view, final Context context,  final Cursor cursor) {

        TextView textViewTitle = (TextView) view.findViewById(R.id.alertName);

        final TextView id = (TextView) view.findViewById(R.id.alertId);
        ImageView edit = (ImageView) view.findViewById(R.id.edit);
        ImageView delete = (ImageView) view.findViewById(R.id.delete);
        ImageView expired = (ImageView) view.findViewById(R.id.expired);
        TextView repeatText = (TextView) view.findViewById(R.id.repeatText);
        TextView repeat = (TextView) view.findViewById(R.id.repeat);

        if (cursor != null ) {
            String title = cursor.getString( cursor.getColumnIndex( DatabaseHandler.ALERT_NAME ) );
            String status = cursor.getString( cursor.getColumnIndex( DatabaseHandler.STATUS ) );
            String is_repeat = cursor.getString( cursor.getColumnIndex( DatabaseHandler.IS_REPEAT ) );
            String dbrepeat = cursor.getString( cursor.getColumnIndex( DatabaseHandler.REPEAT_TYPE ) );
            if(is_repeat.equals("true")){
                repeatText.setVisibility(View.VISIBLE);
                repeat.setVisibility(View.VISIBLE);
                repeat.setText(dbrepeat);
            }

            if(status.equals("expired")){
                expired.setVisibility(View.VISIBLE);
            }
            textViewTitle.setText(title);

            alertId = cursor.getString( cursor.getColumnIndex( DatabaseHandler.ALERT_ID ));
            id.setText(alertId);

        }
        view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateRiderAlert.class);
                intent.putExtra("source","edit");
                intent.putExtra("alertId",id.getText().toString());
                context.startActivity(intent);
            }
        });

        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog deleteAlert = new Dialog(context);
                deleteAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                deleteAlert.setContentView(R.layout.deletealert_dialog);

                final Button delete  = (Button)deleteAlert.findViewById(R.id.yes);
                Button cancel = (Button)deleteAlert.findViewById(R.id.no);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseHandler dba = new DatabaseHandler(context);
                        try{
                            dba.open();
                            dba.deleteRiderAlert(id.getText().toString());
                            Cursor mcursor = dba.fetchAllRiderAlerts();
                            swapCursor(mcursor);
                            notifyDataSetChanged();
                            dba.close();
                            deleteAlert.dismiss();
                        }catch (Exception e){
                            dba.close();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteAlert.dismiss();
                    }
                });
                deleteAlert.show();
            }
        });
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // R.layout.list_row is your xml layout for each row
        return cursorInflater.inflate(R.layout.view_rider_alert_list_item, parent, false);
    }
}
