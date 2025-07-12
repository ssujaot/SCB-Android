package com.onetrust.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.onetrust.otpublishers.headless.Public.DataModel.OTProfileSyncParams;
import com.onetrust.otpublishers.headless.Public.Keys.OTBroadcastServiceKeys;
import com.onetrust.otpublishers.headless.Public.OTCallback;
import com.onetrust.otpublishers.headless.Public.OTPublishersHeadlessSDK;
import com.onetrust.otpublishers.headless.Public.Response.OTResponse;
import com.onetrust.otpublishers.headless.Public.DataModel.OTSdkParams;
import com.onetrust.otpublishers.headless.Public.DataModel.OTUXParams;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OneTrust extends CordovaPlugin{
    OTPublishersHeadlessSDK ot;
    String storageLocation;
    String domainId;
    String languageCode;
    JSONObject paramsJson;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        ot = new OTPublishersHeadlessSDK(cordova.getContext());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action){
            case "startSDK":
                storageLocation = args.getString(0);
                domainId = args.getString(1);
                languageCode = args.getString(2);
                JSONObject initParams = args.optJSONObject(3);
                startSDK(storageLocation, domainId, languageCode, initParams, callbackContext);
                return true;
            case "showBannerUI":
                showBannerUI();
                return true;
            case "showPreferenceCenterUI":
                showPreferenceCenterUI();
                return true;
            case "showConsentUI":
                callbackContext.success(-1);
                return true;
            case "getConsentStatusForCategory":
                String categoryId = args.getString(0);
                getConsentStatusForCategory(categoryId, callbackContext);
                return true;
            case "shouldShowBanner":
                shouldShowBanner(callbackContext);
                return true;
            case "isBannerShown":
                isBannerShown(callbackContext);
                return true;
            case "observeChanges":
                String catId = args.getString(0);
                observeChanges(catId);
                return true;
            case "addCustomStyles":
                callbackContext.error("Deprecated Method. Add your JSON as androidUXParams in the params object of startSDK.");
            case "getCachedIdentifier":
                getCachedIdentifier(callbackContext);
                return true;
            case "getBannerData":
                getBannerData(callbackContext);
                return true;
            case "getPreferenceCenterData":
                getPreferenceCenterData(callbackContext);
                return true;
            case "getOTConsentJSForWebview":
                getOTConsentJSForWebview(callbackContext);
                return true;
            case "dismissUI":
                dismissUI();
                return true;
            default:
                callbackContext.error("Unimplemented method called");
                break;
        }
        return false;
    }

    private void startSDK(String storageLocation, String domainId, String languageCode, JSONObject initParams, final CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                OTSdkParams sdkParams = null;

                if(initParams != null){
                    sdkParams = generateSDKParams(initParams);
                }

                ot.startSDK(storageLocation, domainId, languageCode, sdkParams, new OTCallback() {
                    @Override
                    public void onSuccess(OTResponse otResponse) {
                        callbackContext.success(1);
                    }

                    @Override
                    public void onFailure(OTResponse otResponse) {
                        callbackContext.error("Failed to initialize OneTrust SDK. --> "+otResponse.getResponseMessage());
                    }
                });
            }
        };
        runInThreadPool(runnable);
    }

    private OTSdkParams generateSDKParams(JSONObject initParams){
        final String COUNTRY_CODE = "countryCode";
        final String REGION_CODE = "regionCode";
        final String SYNC_PARAMS = "syncParams";
        final String IDENTIFIER = "identifier";
        final String JWT = "syncProfileAuth";
        final String UX_PARAMS_JSON = "androidUXParams";
        final String API_VERSION = "setAPIVersion";
        String countryCode;
        String regionCode;
        String apiVersion;

        OTSdkParams.SdkParamsBuilder sdkParamsBuilder = OTSdkParams.SdkParamsBuilder.newInstance();

        /* Add UXParams object to paramsbuilder if it's present in the payload*/
        if(initParams.has(UX_PARAMS_JSON)){
            JSONObject uxParamsJSON = initParams.optJSONObject(UX_PARAMS_JSON);
            OTUXParams uxParams = OTUXParams.OTUXParamsBuilder.newInstance()
                    .setUXParams(uxParamsJSON)
                    .build();
            sdkParamsBuilder.setOTUXParams(uxParams);
        }

        if(initParams.has(COUNTRY_CODE)){
            countryCode = initParams.optString(COUNTRY_CODE);
            sdkParamsBuilder.setOTCountryCode(countryCode);
        }

        if(initParams.has(REGION_CODE)){
            regionCode = initParams.optString(REGION_CODE);
            sdkParamsBuilder.setOTRegionCode(regionCode);
        }

        if(initParams.has(API_VERSION)){
            apiVersion = initParams.optString(API_VERSION);
            sdkParamsBuilder.setAPIVersion(apiVersion);
        }

        /*If sync params are present in the payload, add them to the object.
        This function will fail gracefully if either the identifier or JWT are missing.
        This also automatically turns on setSyncProfile and shouldCreateProfile. */
        if(initParams.has(SYNC_PARAMS)){
            JSONObject paramsObj = initParams.optJSONObject(SYNC_PARAMS);
            if(paramsObj.has(IDENTIFIER) && paramsObj.has(JWT)){
                String identifier = paramsObj.optString(IDENTIFIER);
                String jwt = paramsObj.optString(JWT);

                OTProfileSyncParams profileSyncParams = OTProfileSyncParams.OTProfileSyncParamsBuilder.newInstance()
                        .setIdentifier(identifier)
                        .setSyncProfileAuth(jwt)
                        .setSyncProfile("true")
                        .build();
                sdkParamsBuilder.shouldCreateProfile("true");
                sdkParamsBuilder.setProfileSyncParams(profileSyncParams);
            }

        }

        return sdkParamsBuilder.build();
    }

    private void showBannerUI(){
        if(ot.getBannerData()!=null){ //null check prevents starting blank activity if no banner data present
            startCMPActivity(0);
        }
    }

    private void showPreferenceCenterUI(){
        if(ot.getPreferenceCenterData() != null){ ////null check prevents starting blank activity if no PC data present
            startCMPActivity(1);
        }
    }

    private void shouldShowBanner(CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int shouldShow = ot.shouldShowBanner() ? 1:0; //Success callback has to be an int in Android, so we send back 1 or 0. 1 = truthy in JS
                callbackContext.success(shouldShow);
            }
        };
        runInThreadPool(runnable);
    }

      private void isBannerShown(CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(cordova.getContext()!= null){
                    int bannerShown = ot.isBannerShown(cordova.getContext());
                    callbackContext.success(bannerShown);
                }
            }
        };
        runInThreadPool(runnable);
    }

    private void getConsentStatusForCategory(String catId, CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int status = ot.getConsentStatusForGroupId(catId);
                callbackContext.success(status);
            }
        };
        runInThreadPool(runnable);
    }

    private void observeChanges(String catId){
        cordova.getActivity().registerReceiver(consentStatusChanged, new IntentFilter(catId));
    }

    private void getCachedIdentifier(CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String identifier = ot.getOTCache().getDataSubjectIdentifier();
                callbackContext.success(identifier);
            }
        };
        runInThreadPool(runnable);
    }

    private void getBannerData(CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JSONObject bannerData = ot.getBannerData();
                if(bannerData != null){
                    callbackContext.success(bannerData);
                }else{
                    callbackContext.error("No banner data found");
                }
            }
        };
        runInThreadPool(runnable);
    }

    private void getPreferenceCenterData(CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JSONObject preferenceCenterData = ot.getPreferenceCenterData();
                if(preferenceCenterData != null){
                    callbackContext.success(preferenceCenterData);
                }else{
                    callbackContext.error("No banner data found");
                }
            }
        };
        runInThreadPool(runnable);
    }

    private void getOTConsentJSForWebview(CallbackContext callbackContext){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String js = ot.getOTConsentJSForWebView();
                if(js!=null) {
                    callbackContext.success(js);
                }else{
                    callbackContext.error("No JS Found for WebView");
                }
            }
        };
        runInThreadPool(runnable);
    }

    private void dismissUI(){
        cordova.getActivity().finishActivity(1);
    }

    //Internal Methods
    private void runInThreadPool(Runnable runnable){
        this.cordova.getThreadPool().execute(runnable);
    }


    public void emit(String event, String payload){ //payload is a Stringified JSON obj
        Runnable js = new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript: cordova.fireDocumentEvent('"+event+"', "+payload+");");
            }
        };
        cordova.getActivity().runOnUiThread(js);
    }

    BroadcastReceiver consentStatusChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(OTBroadcastServiceKeys.EVENT_STATUS,-1);
            String category = intent.getAction();
            emit(category, "{categoryId:'"+category+"', consentStatus:"+status+"}");
        }
    };

    private void startCMPActivity(int uiType){
        Intent intent = new Intent(cordova.getContext(), CMPActivity.class);
        intent.putExtra("UIType", uiType);
        this.cordova.startActivityForResult(this, intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 1){
            emit("allSDKViewsDismissed","{}");
        }
    }
}