package com.cmcc.csu.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.RILConstants;
import com.android.sprd.telephony.RadioInteractor;
import com.cmcc.csu.service.model.ConfigResponse;
import com.cmcc.csu.service.ICsuService;
import java.util.Map;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ConfigsSetter {

    private static final String TAG = ConfigsSetter.class.getSimpleName();

    //SA/NSA values
    private static final int SET_SA_NSA = 132;
    private static final int SET_NSA_ONLY = 260;
    private static final int SET_SA_ONLY = 516;
    private static final int READ_SA_NSA = 66;
    private static final int READ_NSA_ONLY = 130;
    private static final int READ_SA_ONLY = 258;

    //config values
    private static final int CONFIG_VALUE_INVALID = -1;
    private static final int CONFIG_VALUE_OPEN = 1;
    private static final int CONFIG_VALUE_CLOSE = 0;

    //result success
    private static final int RESULT_SUCCESS = 0;

    private ServiceState mServiceState;
    private TelephonyManager mTelephonyManager = TelephonyManager.getDefault();

    public boolean postConfigurationUpdateOfAp5001(String configUpdateString) throws JSONException {
        try {
            //according to the DataBind in fastjson github, need use Class
            //here we use the map class in java directly
            Map<String, Object> ap5001_map = JSON.parseObject(configUpdateString, Map.class);
            Log.d(TAG, "check whether the name of ap5001 is 5G");
            Log.d(TAG, "the name of ap5001 is" + ap5001_map.get("Name"));
            Log.d(TAG, "the name of ap5001 is" + ap5001_map.get("Value"));

            String name = String.valueOf(ap5001_map.get("Name"));
            boolean temp = (name.equals("5G"));
            Log.d(TAG, "the boolen temp is : " + temp);
            if (name.equals("5G")) {
                Log.d(TAG, "the name of ap5001 is 5G");
                int dataNetworkType = CONFIG_VALUE_INVALID;
                if ((int)ap5001_map.get("Value") == CONFIG_VALUE_OPEN) {
                    dataNetworkType = RILConstants.NETWORK_MODE_NR_LTE_GSM_WCDMA;
                }
                else {
                    dataNetworkType = RILConstants.NETWORK_MODE_LTE_GSM_WCDMA;
                }
                //get the datasubID
                int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();

                //9：NETWORK_MODE_LTE_GSM_WCDMA，/* LTE, GSM/WCDMA */
                //26：NETWORK_MODE_NR_LTE_GSM_WCDMA，/* NR 5G, LTE, GSM and WCDMA */
                Log.d(TAG, "begin to set networkmode");
                if (mTelephonyManager.setPreferredNetworkType(defaultDataSubId, dataNetworkType)) {
                    Log.d(TAG, "successfully to set networkmode");
                    return true;
                }
                Log.d(TAG, "failed to set networkmode");
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean postConfigurationUpdateOfAp5002(String configUpdateString,
                                                   RadioInteractor radioInteractor) throws JSONException {
        try {
            //according to the DataBind in fastjson github, need use Class
            //here we use the map class in java directly
            Map<String, Object> ap5002_map = JSON.parseObject(configUpdateString, Map.class);

            String name = String.valueOf(ap5002_map.get("Name"));
            boolean temp = (name.equals("SA"));
            Log.d(TAG, "the boolen temp is : " + temp);
            if (name.equals("SA")) {
                Log.d(TAG, "the name of ap5002 is SA");

                //get the datasubID
                int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
                Log.d(TAG, "postConfigurationUpdateOfAp5002 defaultDataSubId:" + defaultDataSubId);
                //get the dataPhoneID
                int defaultDataPhoneId = SubscriptionManager.getPhoneId(defaultDataSubId);
                Log.d(TAG, "postConfigurationUpdateOfAp5002 defaultDataPhoneId:" + defaultDataPhoneId);

                int standAloneValue = (int)ap5002_map.get("Value");
                Log.d(TAG, "postConfigurationUpdateOfAp5002 standAloneValue:" + standAloneValue);
                int standAloneStatus = (standAloneValue == CONFIG_VALUE_OPEN)? SET_SA_ONLY:SET_SA_NSA;

                try {
                    //get the StandAlone Status： SA／NSA／SA+NSA
                    if (radioInteractor != null) {
                        int result = radioInteractor.setStandAlone(standAloneStatus, defaultDataPhoneId);
                        Log.d(TAG, "postConfigurationUpdateOfAp5002 standAloneValue complete" );
                        if (result == RESULT_SUCCESS) {
                            //mPowerManager.reboot("");
                            return true;
                        } else {
                            Log.d(TAG, "exception in set stand alone mode");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean postConfigurationUpdateOfAp5003(String configUpdateString) throws JSONException {
        try {
            //according to the DataBind in fastjson github, need use Class
            //here we use the map class in java directly
            Map<String, Object> ap_map = JSON.parseObject(configUpdateString, Map.class);

            String name = String.valueOf(ap_map.get("Name"));

            if (name.equals("CA")) {
                Log.d(TAG, "the name of ap5003 is CA");

                int config = (int)ap_map.get("Value");
                boolean setConfig = (config == CONFIG_VALUE_OPEN)? true:false;

                if (mServiceState != null) {
                    mServiceState.setIsUsingCarrierAggregation(setConfig);
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean postConfigurationUpdateOfAp5005(String configUpdateString, ImsManager mImsManager) throws JSONException {
        try {
            //according to the DataBind in fastjson github, need use Class
            //here we use the map class in java directly
            Map<String, Object> ap_map = JSON.parseObject(configUpdateString, Map.class);

            String name = String.valueOf(ap_map.get("Name"));

            if (name.equals("VoNR")) {
                Log.d(TAG, "the name of ap5005 is VoNR");

                int config = (int)ap_map.get("Value");
                boolean setConfig = (config == CONFIG_VALUE_OPEN)? true:false;

                if (mImsManager != null) {
                    mImsManager.setEnhanced4gLteModeSetting(setConfig);
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}