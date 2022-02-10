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

public class CsuService extends Service {

    private static final String TAG = CsuService.class.getSimpleName();

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

    //ap5001-5005 from cmcc Entitlement Server (ES) business specification file
    private static final String APPID_5G_5001 = "ap5001";
    private static final String APPID_SA_5002 = "ap5002";
    private static final String APPID_CA_5003 = "ap5003";
    private static final String APPID_VONR_5005 = "ap5005";

    //values used when parsing xml data to json data
    private static final String XML_CHARACTERISTIC = "characteristic";
    private static final String XML_PARM = "parm";
    private static final String XML_VALUE = "value";
    private static final String XML_NAME = "name";
    private static final int XML_PARM_ONE = 1;
    private static final int XML_PARM_TWO = 2;
    private static final int XML_PARM_ZERO = 0;

    private TelephonyManager mTelephonyManager = TelephonyManager.getDefault();

    private ConfigsGetter mConfigsGetter = new ConfigsGetter();
    private ConfigsSetter mConfigsSetter = new ConfigsSetter();

    private RadioInteractor mRadioInteractor = new RadioInteractor(this);

    @Override
    public IBinder onBind(Intent intent) {
        return new ICsuService.Stub() {
            @Override
            public ConfigResponse getConfiguration(String privateKey, String packageName)
                    throws RemoteException {

                Log.d(TAG, "CSU getConfiguration");
                ImsManager imsManager = ImsManager.getInstance(CsuService.this,
                        SubscriptionManager.getDefaultVoicePhoneId());

                ConfigResponse configResponse = new ConfigResponse();

                //obtain the ConfigInfos
                JSONObject jsonConfigAll = new JSONObject();

                //ap5001
                jsonConfigAll.put(APPID_5G_5001, mConfigsGetter.getConfigurationOfAp5001());

                //ap5002
                if (mRadioInteractor != null) {
                    jsonConfigAll.put(APPID_SA_5002,
                            mConfigsGetter.getConfigurationOfAp5002(mRadioInteractor));
                }

                //ap5003
                jsonConfigAll.put(APPID_CA_5003, mConfigsGetter.getConfigurationOfAp5003());

                //ap5005
                if (imsManager != null) {
                    jsonConfigAll.put(APPID_VONR_5005,
                            mConfigsGetter.getConfigurationOfAp5005(imsManager));
                }

                configResponse.setResult(RESULT_SUCCESS);
                configResponse.setConfigInfo(jsonConfigAll.toString());

                return configResponse;
            }

            @Override
            public ConfigResponse postConfigurationUpdate(String privateKey, String packageName,
                                                          String configInfoUpdate) throws RemoteException {

                ImsManager imsManager = ImsManager.getInstance(CsuService.this,
                        SubscriptionManager.getDefaultVoicePhoneId());

                ConfigResponse configResponse = new ConfigResponse();
                configResponse.setResult(RESULT_SUCCESS);

                JSONObject parseConfigAll = new JSONObject();
                try {
                    // json data
                    //obtain the ConfigInfos needed to update
                    parseConfigAll = JSON.parseObject(configInfoUpdate);
                } catch(Exception e) {
                    // xml data translate to json data
                    Log.d(TAG, "CSU Need parse xml data");

                    Document doc = null;
                    try {
                        doc = DocumentHelper.parseText(configInfoUpdate);
                    } catch (DocumentException f) {
                        f.printStackTrace();
                    }
                    // point to the root node  <characteristics>
                    Element characteristics = doc.getRootElement();
                    try {
                        List<Element> apList = characteristics.elements(XML_CHARACTERISTIC);
                        //read from characteristics type
                        //save into json data type
                        for (int i = 1; i < apList.size(); i++) {
                            Element ap = apList.get(i);
                            List<Element> parms = ap.elements(XML_PARM);
                            JSONObject config = new JSONObject();

                            config.put(parms.get(XML_PARM_ONE).attributeValue(XML_NAME),
                                    parms.get(XML_PARM_ONE).attributeValue(XML_VALUE));

                            config.put(parms.get(XML_PARM_TWO).attributeValue(XML_NAME),
                                    Integer.parseInt(parms.get(XML_PARM_TWO).attributeValue(XML_VALUE)));

                            parseConfigAll.put(parms.get(XML_PARM_ZERO).attributeValue(XML_VALUE), config);
                        }
                        Log.d(TAG, "get xml data " + parseConfigAll.toString());
                    } catch(Exception j) {
                        j.printStackTrace();
                    }
                }

                //obtain the updated results of expected ConfigInfos
                JSONObject configUpdateResultAll = new JSONObject();

                //GET-AP-CONFIGS : get the expected config of ap5001
                String ap5001_String = parseConfigAll.getString(APPID_5G_5001);
                Log.d(TAG, "get the expected config: " + ap5001_String);
                // do update of ap5001
                if ((ap5001_String != null)
                        && mConfigsSetter.postConfigurationUpdateOfAp5001(ap5001_String)) {
                    //if update successfully, obtain the config of AP5001 now
                    JSONObject jsonConfig_ap5001 = mConfigsGetter.getConfigurationOfAp5001();
                    configUpdateResultAll.put(APPID_5G_5001, jsonConfig_ap5001);
                    Log.d(TAG, "update 5G success : " + jsonConfig_ap5001.toString());
                }

                //GET-AP-CONFIGS : get the expected config of ap5002
                String ap5002_String = parseConfigAll.getString(APPID_SA_5002);
                Log.d(TAG, "get the expected config of ap5002: " + ap5002_String);
                // do update of ap5001
                if ((ap5002_String != null) && (mRadioInteractor != null)
                        && mConfigsSetter.postConfigurationUpdateOfAp5002(ap5002_String, mRadioInteractor)) {
                    //if update successfully, obtain the config of AP5002 now
                    JSONObject jsonConfig_ap5002 = mConfigsGetter.getConfigurationOfAp5002(mRadioInteractor);
                    configUpdateResultAll.put(APPID_SA_5002, jsonConfig_ap5002);
                    Log.d(TAG, "update SA success : " + jsonConfig_ap5002.toString());
                }

                //GET-AP-CONFIGS : get the expected config
                String ap5003_String = parseConfigAll.getString(APPID_CA_5003);
                Log.d(TAG, "get the expected config of ap5003: " + ap5003_String);
                // do update of ap5001
                if ((ap5003_String != null)
                        && mConfigsSetter.postConfigurationUpdateOfAp5003(ap5003_String)) {
                    //if update successfully, obtain the config
                    JSONObject jsonConfig_ap5003 = mConfigsGetter.getConfigurationOfAp5003();
                    configUpdateResultAll.put(APPID_CA_5003, jsonConfig_ap5003);
                    Log.d(TAG, "update CA success : " + jsonConfig_ap5003.toString());
                }

                //GET-AP-CONFIGS : get the expected config
                String ap5005_String = parseConfigAll.getString(APPID_VONR_5005);
                Log.d(TAG, "get the expected config of ap5005: " + ap5005_String);
                // do update of ap5001
                if ((ap5005_String != null)
                        && mConfigsSetter.postConfigurationUpdateOfAp5005(ap5005_String, imsManager)) {
                    //if update successfully, obtain the config
                    JSONObject jsonConfig_ap5005 = mConfigsGetter.getConfigurationOfAp5005(imsManager);
                    configUpdateResultAll.put(APPID_VONR_5005, jsonConfig_ap5005);
                    Log.d(TAG, "update VoNR success : " + jsonConfig_ap5005.toString());
                }

                configResponse.setConfigInfo(configUpdateResultAll.toString());
                Log.d(TAG, configUpdateResultAll.toString());

                return configResponse;
            }
        };
    }
}
