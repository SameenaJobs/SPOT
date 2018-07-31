package com.etatransit.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by innovate on 7/4/17.
 */

public class PoorConnection extends PhoneStateListener{
    Context mContext;
    public int mSignalStrength = 0;
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        if (signalStrength.isGsm()) {
            if (signalStrength.getGsmSignalStrength() != 99)
                mSignalStrength = signalStrength.getGsmSignalStrength() * 2 - 113;
            else
                mSignalStrength = signalStrength.getGsmSignalStrength();
        }

    }
    public PoorConnection(Context mContext) {
        this.mContext = mContext;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public int checkConnectionSpeed() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
        NetworkInfo mobileCheck = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiCheck = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiCheck!=null) {
            if (wifiCheck.getTypeName().equalsIgnoreCase("WIFI")) {
                if (wifiCheck.isConnected())
                    haveConnectedWifi = true;
            }
        }
        if (mobileCheck!=null) {
            if (mobileCheck.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (mobileCheck.isConnected())
                    haveConnectedMobile = true;
            }
        }
        int strength =0;
        int linkSpeed =0;
        if (haveConnectedWifi == true) {
            WifiManager wManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
            WifiInfo wifiInfo = wManager.getConnectionInfo();
            if (wifiInfo != null) {
                Integer Speed = wifiInfo.getLinkSpeed(); //measured using WifiInfo.LINK_SPEED_UNITS
            }
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
            linkSpeed = wifiManager.getConnectionInfo().getRssi();
//            linkSpeed = -75;
        }
        else if (haveConnectedMobile == true){
            TelephonyManager telephonyManager = (TelephonyManager)this.mContext.getSystemService(mContext.TELEPHONY_SERVICE);
            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            telephonyManager.getNetworkType();
            if(cellInfos!=null){
                for (int i = 0 ; i<cellInfos.size(); i++) {
                    if (cellInfos.get(i).isRegistered()) {
                        if (cellInfos.get(i).isRegistered()) {
                            if (cellInfos.get(i) instanceof CellInfoLte) {
                                CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                                strength = Integer.parseInt(String.valueOf(cellSignalStrengthLte.getDbm()));
                                try {
                                    Method[] methods = android.telephony.SignalStrength.class.getMethods();
                                    for(Method mthd:methods){
                                        if(mthd.getName().equals("getLteRssi") || mthd.getName().equals("getLteSignalStrength") || mthd.getName().equals("getLteRsrp")){

                                            strength = (Integer) mthd.invoke(cellInfos, new Object[]{});
                                            break;
                                        }
                                    }
                                } catch (SecurityException e) {
                                    e.printStackTrace();

                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();

                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();

                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();

                                }
                                break;
                            } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                                CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                                strength = Integer.parseInt(String.valueOf(cellSignalStrengthGsm.getDbm()));
                            } else if (cellInfos.get(i) instanceof CellInfoWcdma) {
                                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                strength = Integer.parseInt(String.valueOf(cellSignalStrengthWcdma.getDbm()));

                            } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                                CellInfoCdma cellInfoCdma = (CellInfoCdma) telephonyManager.getAllCellInfo().get(0);
                                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                                strength = Integer.parseInt(String.valueOf(cellSignalStrengthCdma.getDbm()));
                            }
                        }
                    }
                }

            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder
//                .setTitle("Poor Network")
//                .setMessage("Poor Network Connection ." + String.valueOf(linkSpeed))
//                .setCancelable(false)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                        dialog.dismiss();
//                    }
//                });
//
//        AlertDialog alert = builder.create();
//        alert.show();
        if (linkSpeed < -90 || strength <-99 ) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
            builder
                    .setTitle("Poor Network")
                    .setMessage("Poor Network Connection ." + String.valueOf(strength))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.dismiss();
                        }
                    });

            AlertDialog alert1 = builder1.create();
            alert1.show();
//            Toast.makeText(mContext, "Poor Network Connection", Toast.LENGTH_LONG).show();
        }

        return strength;
    }

}