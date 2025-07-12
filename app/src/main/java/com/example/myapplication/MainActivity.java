package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.onetrust.otpublishers.headless.Public.DataModel.OTConfiguration;
import com.onetrust.otpublishers.headless.Public.DataModel.OTProfileSyncParams;
import com.onetrust.otpublishers.headless.Public.DataModel.OTSdkParams;
import com.onetrust.otpublishers.headless.Public.DataModel.OTUXParams;
import com.onetrust.otpublishers.headless.Public.OTCallback;
import com.onetrust.otpublishers.headless.Public.OTPublishersHeadlessSDK;
import com.onetrust.otpublishers.headless.Public.OTThemeConstants;
import com.onetrust.otpublishers.headless.Public.Response.OTResponse;

import org.json.JSONObject;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "OTConsent";

    private OTPublishersHeadlessSDK otPublishersHeadlessSDK;
    private BroadcastReceiver otConsentUpdatedBroadCastReceiver;

    private TextView consentStatusTextView;
    private Button loadPreferenceCenterButton;
    private Button loadBannerButton;
    private Button loadPreferenceCenterButton_DarkMode;
    private Button loadBannerButton_DarkMode;

    private boolean isLightModeInitialized = false;
    private boolean isDarkModeInitialized = false;
    private OTUXParams darkModeParams;
    private OTUXParams lightModeParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consentStatusTextView = findViewById(R.id.consentStatusTextView);
        loadPreferenceCenterButton = findViewById(R.id.buttonLoadPreferenceCentre);
        loadBannerButton = findViewById(R.id.buttonLoadBanner);
        loadPreferenceCenterButton_DarkMode = findViewById(R.id.buttonLoadPreferenceCentreDarkMode);
        loadBannerButton_DarkMode = findViewById(R.id.buttonLoadBannerDarkMode);

        otPublishersHeadlessSDK = new OTPublishersHeadlessSDK(this);

        // Load JSON once
        lightModeParams = loadOTUXParamsFromFile("ot_ui_params.json");
        darkModeParams = loadOTUXParamsFromFile("ot_ui_params_dark.json");

        // Init light mode once on startup
        if (lightModeParams != null) {
            OTSdkParams lightSdkParams = OTSdkParams.SdkParamsBuilder.newInstance()
                    .setOTUXParams(lightModeParams)
                    .build();

            otPublishersHeadlessSDK.startSDK("cdn.cookielaw.org",
                    "0197cf40-4548-7c32-92a8-cdeaa7bb8eba-test",
                    "en", null, new OTCallback() { //lightSdkParams
                        @Override
                        public void onSuccess(@NonNull OTResponse otSuccessResponse) {
                            Log.i(LOG_TAG, "SDK initialized for Light Mode");
                            isLightModeInitialized = true;
                        }

                        @Override
                        public void onFailure(@NonNull OTResponse otErrorResponse) {
                            Log.e(LOG_TAG, "Light SDK Init Error: " + otErrorResponse.getResponseMessage());
                        }
                    });
        }

        loadPreferenceCenterButton.setOnClickListener(v -> {
            OTConfiguration config = getOTConfiguration(this, false);
            otPublishersHeadlessSDK.showPreferenceCenterUI(this, config);
        });

        loadBannerButton.setOnClickListener(v -> {
            OTConfiguration config = getOTConfiguration(this, false);
            otPublishersHeadlessSDK.showBannerUI(this, config);
        });

        loadPreferenceCenterButton_DarkMode.setOnClickListener(v -> handleDarkModeUI(true));
        loadBannerButton_DarkMode.setOnClickListener(v -> handleDarkModeUI(false));

        // Consent broadcast receiver
        otConsentUpdatedBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "OT_CONSENT_UPDATED".equals(intent.getAction())) {
                    Log.i("BroadcastService", "Consent updated: " + intent.getAction());

                    int consentGroup4 = otPublishersHeadlessSDK.getConsentStatusForGroupId("C0004");
                    int consentGroup2 = otPublishersHeadlessSDK.getConsentStatusForGroupId("C0002");

                    String consentStatus = "Consent Group C0004: " +
                            ((consentGroup4 == 1) ? "1" : (consentGroup4 == 0) ? "0" : "Not Collected") + "\n" +
                            "Consent Group C0002: " +
                            ((consentGroup2 == 1) ? "1" : (consentGroup2 == 0) ? "0" : "Not Collected");

                    runOnUiThread(() -> consentStatusTextView.setText(consentStatus));
                }
            }
        };

        registerReceiver(otConsentUpdatedBroadCastReceiver,
                new IntentFilter("OT_CONSENT_UPDATED"));
    }

    private void handleDarkModeUI(boolean isPreferenceCenter) {
        OTConfiguration config = getOTConfiguration(this, true);

        if (!isDarkModeInitialized) {
            if (darkModeParams == null) {
                Log.e(LOG_TAG, "Dark mode params not loaded");
                return;
            }

            OTSdkParams darkSdkParams = OTSdkParams.SdkParamsBuilder.newInstance()
                    .setOTUXParams(darkModeParams)
                    .build();

            otPublishersHeadlessSDK.startSDK("cdn.cookielaw.org",
                    "0197cf40-4548-7c32-92a8-cdeaa7bb8eba-test",
                    "en", null, new OTCallback() { //darkSdkParams
                        @Override
                        public void onSuccess(@NonNull OTResponse otSuccessResponse) {
                            Log.i(LOG_TAG, "Dark SDK Init Success");
                            isDarkModeInitialized = true;
                            if (isPreferenceCenter) {
                                otPublishersHeadlessSDK.showPreferenceCenterUI(MainActivity.this, config);
                            } else {
                                otPublishersHeadlessSDK.showBannerUI(MainActivity.this, config);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull OTResponse otErrorResponse) {
                            Log.e(LOG_TAG, "Dark SDK Init Error: " + otErrorResponse.getResponseMessage());
                        }
                    });
        } else {
            // Already initialized
            if (isPreferenceCenter) {
                otPublishersHeadlessSDK.showPreferenceCenterUI(this, config);
            } else {
                otPublishersHeadlessSDK.showBannerUI(this, config);
            }
        }
    }

    private OTUXParams loadOTUXParamsFromFile(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, "UTF-8");

            return OTUXParams.OTUXParamsBuilder.newInstance()
                    .setOTSDKTheme(OTThemeConstants.OT_THEME_APP_COMPACT_LIGHT_NO_ACTION_BAR_LANDSCAPE_FULL_SCREEN)
                    .setUXParams(new JSONObject(jsonStr))
                    .build();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load UXParams from: " + fileName, e);
            return null;
        }
    }

    private OTConfiguration getOTConfiguration(Context context, boolean enableDarkMode) {
        Typeface font = ResourcesCompat.getFont(context, R.font.aguafinascript_regular);
        OTConfiguration.OTConfigurationBuilder builder = OTConfiguration.OTConfigurationBuilder.newInstance();

        if (font != null) builder.addOTTypeFace("ot-font", font);
        if (enableDarkMode) builder.shouldEnableDarkMode("true");

        return builder.build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otConsentUpdatedBroadCastReceiver != null) {
            unregisterReceiver(otConsentUpdatedBroadCastReceiver);
        }
    }
}
