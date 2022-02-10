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

public class ConfigsGetter {

    private static final String TAG = ConfigsGetter.class.getSimpleName();

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

    private ServiceState mServiceState;
    private TelephonyManager mTelephonyManager = TelephonyManager.getDefault();

    public JSONObject getConfigurationOfAp5001() throws JSONException {
        //get the datasubID
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();

        //9：NETWORK_MODE_LTE_GSM_WCDMA，/* LTE, GSM/WCDMA */
        //26：NETWORK_MODE_NR_LTE_GSM_WCDMA，/* NR 5G, LTE, GSM and WCDMA */
        int dataNetworkType = mTelephonyManager.getPreferredNetworkType(defaultDataSubId);

        //return config data referred
        int configValue = CONFIG_VALUE_INVALID;
        JSONObject configObject = new JSONObject();

        //judge the config (eg.5G/SA/..) to open or close
        configValue = (dataNetworkType == RILConstants.NETWORK_MODE_NR_LTE_GSM_WCDMA)
                ? CONFIG_VALUE_OPEN : CONFIG_VALUE_CLOSE;

        try {
            configObject.put("Name", "5G");
            configObject.put("Value", configValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return configObject;
    }

    public JSONObject getConfigurationOfAp5002(RadioInteractor radioInteractor) throws JSONException {
        //get the datasubID
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        Log.d(TAG, "getConfigurationOfAp5002 defaultDataSubId:" + defaultDataSubId);
        //get the dataPhoneID
        int defaultDataPhoneId = SubscriptionManager.getPhoneId(defaultDataSubId);
        Log.d(TAG, "getConfigurationOfAp5002 defaultDataPhoneId:" + defaultDataPhoneId);

        //return config data referred
        int configValue = CONFIG_VALUE_INVALID;
        JSONObject configObject = new JSONObject();

        try {
            if (radioInteractor != null) {
                //get the StandAlone Status： SA／NSA／SA+NSA
                int standAloneStatus = radioInteractor.getStandAlone(defaultDataPhoneId);
                //judge the config (eg.5G/SA/..) to open or close
                configValue = (standAloneStatus == READ_SA_ONLY)
                        ? CONFIG_VALUE_OPEN : CONFIG_VALUE_CLOSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            configObject.put("Name", "SA");
            configObject.put("Value", configValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return configObject;
    }

    public JSONObject getConfigurationOfAp5003() throws JSONException{
        //return config data referred
        int configValue = CONFIG_VALUE_INVALID;
        JSONObject configObject = new JSONObject();

        if (mServiceState != null) {
            boolean isCA = mServiceState.isUsingCarrierAggregation();
            //judge the config (eg.5G/SA/..) to open or close
            configValue = (isCA) ? CONFIG_VALUE_OPEN : CONFIG_VALUE_CLOSE;
        }

        try {
            configObject.put("Name", "CA");
            configObject.put("Value", configValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return configObject;
    }

    public JSONObject getConfigurationOfAp5005(ImsManager mImsManager) throws JSONException{
        //return config data referred
        int configValue = CONFIG_VALUE_INVALID;
        JSONObject configObject = new JSONObject();

        if (mImsManager != null) {
            boolean isVoNR = mImsManager.isEnhanced4gLteModeSettingEnabledByUser();
            //judge the config (eg.5G/SA/..) to open or close
            configValue = (isVoNR) ? CONFIG_VALUE_OPEN : CONFIG_VALUE_CLOSE;
        }

        try {
            configObject.put("Name", "VoNR");
            configObject.put("Value", configValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return configObject;
    }
}