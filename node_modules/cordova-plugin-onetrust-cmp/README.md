# OneTrust CMP Cordova Plugin
## Installation
### Application Requirements
For Android implementations, the AndroidX dependency is required. Add the following to your `config.xml` file, inside of your Android platform tag.

```xml
<preference name="AndroidXEnabled" value="true" />
```

### SDK Version Requirements
Note that the version of the SDK must match the version of the JSON that you have published from your OneTrust instance. For example, if you have published your JSON with version 6.10.0, you must use the 6.10.0 version of the OneTrust plugin. It is recommend that a static version is specified to prevent automatic updates, as a JSON publish will need to occur with every update.

### Install Command
To install the OneTrust Cordova Plugin, use the following command.
```
cordova plugin add cordova-plugin-onetrust-cmp@6.16.0
```

### Uninstalling
To uninstall, run the following in your project folder.
```
cordova plugin remove cordova-plugin-onetrust-cmp
```

# Initialization
The OneTrust SDK retrieves an object that contains all the data needed to present a UI to a User to collect consent for the SDKs used in your application. The data returned is determined by the configurations made in the OneTrust Admin console.

## Import OneTrust
OneTrust methods can be accessed via `window.OneTrust` or `cordova.plugins.OneTrust`.

## `startSDK`
This method is the initialization method for the SDK. It makes between 1-2 network calls, depending if an IAB2 template is fetched or not.

```javascript
window.OneTrust.startSDK(storageLocation,domainIdentifer,languageCode,params,(status) => {
    console.log("SDK Downloaded Successfully!")
},(error) =>{
  console.log("SDK Failed to initialize! Error = "+error)  
})
```
## Arguments
|Argument|Description|Required|
|-|-|-|
|storageLocation|**String** the CDN location for the JSON that the SDK fetches.|Yes|
|domainIdentifier|**String** the application GUID from the OneTrust Admin Console|Yes|
|languageCode|**String** 2-digit ISO language code used to return content in a specific language|Yes
|params |**JSON Object** object containing additional setup params, detailed below|No, nullable
|success|**function** Success handler once download has completed|No|
|failure|**function** Failure handler if download fails |No|

## Initialization Params
The values below can be placed in a JSON object and passed into the `params` argument of `startSDK` to provide additional configurations.

**Example**
```javascript
    const params = {
    syncParams: {
      identifier: 'example@onetrust.com',
      syncProfileAuth:'eyJhb...',
    },
    countryCode:"US",
    regionCode: "CA",
    androidUXParams: uxJson
  }
```

|Key|Description|
|-|-|
countryCode|**String** two-digit ISO country code. When specified, the OneTrust SDK will use this value as the user's geolocation.
|regionCode|**String** two-digit ISO region code. When specified along with a Country Code, the OneTrust SDK will use this value as the user's geolocation.
|androidUXParams|**JSON Object** JSON object representing in-app customizations for Android. See *Android - Custom styling with UXParams JSON* section.
|syncParams|**JSON Object** JSON object with the user's identifier and authorization for cross-device syncing. See section below.|

## Sync Params
Cross-Device Consent is an optional feature. The parameters below are not required for initializing the SDK. Each of the parameters are **required** to sync the user's consent.

|Key|Description|
|-|-|
|identifier|**String** The identifier associated with the user for setting or retrieving consent|
|setSyncProfileAuth|**String** A pre-signed JWT auth token required to perform cross-device consent.|

# Display User Interfaces
## Banner
The banner can be shown by calling
```javascript
window.OneTrust.showBannerUI()
```
### Determine if a banner should be shown
You can determine whether or not a banner should be shown by calling
```javascript
window.OneTrust.shouldShowBanner((result)=>{
    console.log("A banner should be shown = "+result)
})
```
This returns a boolean that determines whether or not the banner should be displayed to the user. See [this page](https://developer.onetrust.com/sdk/mobile-apps/ios/displaying-ui#shouldshowbanner) for the logic used to determine the result of `shouldShowBanner()`.

**Example** <br>
Below is an example of how to combine the initialization success handler with the shouldShowBanner function to automatically show a banner, if required, once the SDK download has completed.

```javascript
    window.OneTrust.startSDK(storageLocation,domainId,langCode, params, (status) =>{
        window.OneTrust.shouldShowBanner((shouldShow) => {
            if(shouldShow){
                window.OneTrust.showBannerUI()
            }
        })
    }, (error) =>{
        console.log(error)
    })

```
## Preference Center
The preference center can be shown by calling
```javascript
window.OneTrust.showPreferenceCenterUI()
```

## Force Close UI
If the UI needs to be dismissed programmatically, simply call
```javascript
window.OneTrust.dismissUI()
```

## App Tracking Transparency Pre-Prompt
The App Tracking Transparency Pre-Prompt can be shown by calling the below function. `OneTrust.devicePermission.idfa` is an enum for the type of permission. On Android and iOS versions < 14, requesting permission of type `ifda` will resolve a promise with a value of `-1` every time.
```javascript
window.OneTrust.showConsentUI(window.OneTrust.devicePermission.idfa, status =>{
    console.log(`The new ATT Status is ${status}`)
})
```

## Android - Custom styling with UXParams JSON
OneTrust allows you to add custom styling to your preference center by passing in style JSON in a certain format. Build out your JSON by following the guide in the [OneTrust Developer Portal](https://developer.onetrust.com/sdk/mobile-apps/android/customize-ui).

Add the JSON to the `params` argument of the `startSDK` method when initializing.

## iOS - Custom Styling with UXParams Plist
Custom styling can be added to your iOS Cordova application by using a .plist file in the iOS platform code. In addition to adding the .plist file (which can be obtained from the OneTrust Demo Application) to your bundle, there are a few changes that need to be made in the platform code, outlined below. Review the guide in the [OneTrust Developer Portal](https://developer.onetrust.com/sdk/mobile-apps/ios/customize-ui).

In `appDelegate.h`, import OTPublishersHeadlessSDK and make sure that AppDelegate conforms to the OTUIConfigurator protocol.

```obj-c
#import <Cordova/CDVViewController.h>
#import <Cordova/CDVAppDelegate.h>
#import <OTPublishersHeadlessSDK/OTPublishersHeadlessSDK.h>

@interface AppDelegate : CDVAppDelegate <OTUIConfigurator>{}

@end
```

In `appDelegate.m`, set the UIConfigurator to self. Then conform to the `shouldUseCustomUIConfig` and `customUIConfigFilePath` protocol methods.

```obj-c

#import "AppDelegate.h"
#import "MainViewController.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    self.viewController = [[MainViewController alloc] init];
    [OTPublishersHeadlessSDK.shared setUiConfigurator:self]; //set UIConfigurator to Self
    return [super application:application didFinishLaunchingWithOptions:launchOptions];
}


- (BOOL)shouldUseCustomUIConfig { //conform to shouldUseCustomUIConfig
    return true;
}

- (NSString *)customUIConfigFilePath{ //conform to filepath protocol method
    NSString * configFile = [[NSBundle mainBundle] pathForResource:@"OTSDK-UIConfig-iOS" ofType:@"plist"]; //find path for config file
    return configFile;
}

@end
```

## UI Interaction Event
After the UI is dismissed from your application, a document event is broadcast. You can listen for this event to detect the UI dismissed.

```javascript
document.addEventListener('allSDKViewsDismissed', () =>{
    console.log("All OneTrust Views dismissed")
}, false)
```

# Get UI Data as JSON
After initialization, the data used to build a preference center or banner can be retrieved easily using the following methods. Each has a success and error handler, so if the download fails and there is no cached data, errors can be handled gracefully. This can also be used to determine whether or not OneTrust has the data required to operate offline (eg. if `getBannerData` does not return an error, at least one download has completed successfully and the required data is present to render a banner.)

```javascript
    window.OneTrust.getBannerData((data) => {
        console.log(`Banner Data = ${JSON.stringify(data)}`)
    }, (error) => {
        console.error(`No banner data! \n Error: ${error}`)
    })

    window.OneTrust.getPreferenceCenterData((data) => {
        console.log(`Preference Center Data = ${JSON.stringify(data)}`)
    }, (error) => {
        console.error(`No Preference Center data! \n Error: ${error}`)
    })
```

# When Consent Changes
There are two ways to get consent values:
* Query for Consent
* Listen to events for changes

## Consent Values
OneTrust universally uses the following values for consent status:
|Status|Explanation|
|-|-|
|1|Consent Given|
|0|Consent Not Given|
|-1|Consent not yet gathered or SDK not initialized|

## Querying for Consent
To obtain the current consent status for a category, use the `getConsentStatusForCategory` method, which returns an `Int`.
```javascript
window.OneTrust.getConsentStatusForCategory("C0004", (status) => {
        console.log("Queried consent status for C0004 is "+status)
    })
```

## Listening for Consent Changes
The OneTrust SDK emits events as consent statuses change. You must tell the platform code which events to listen for. Call this method for each category you wish to start observing.

```javascript
window.OneTrust.observeChanges("C0004")
```

Once a category is being observed, an event will be fired whenever the consent for that category changes. The listener carries a payload with the `categoryId` as a string and the `consentStatus` as an integer.

```javascript
document.addEventListener('C0004',(payload) => {
    console.log("The new status of "+payload.categoryId+" is now"+payload.consentStatus)
}, false)
```

Stop listening for consent changes by calling the following for each category you'd like to cancel.
```javascript
window.OneTrust.stopObservingChanges("C0004")
```

## Retrieving the Cached Data Subject Identifier
The Data Subject Identifier that OneTrust sets can be retrieved by calling the following method. If Consent Logging is enabled, you will be able to retrieve the receipt for any consent choices made by the user inside the Consent module.

```javascript
window.OneTrust.getCachedIdentifier((identifier) => {
          console.log(`Data Subject Identifier is ${identifier}.`)
        })
```

## Pass OneTrust Consent to a WebView
If your application uses WebViews to present content and the pages rendered are running the OneTrust Cookies CMP, you can inject a JavaScript variable, provided by OneTrust, to pass consent from the native application to your WebView.

The JavaScript must be evaluated before the Cookies CMP loads in the webview, therefore, it is recommended to evaluate the JS early on in the WebView load cycle.
```javascript
    window.OneTrust.getOTConsentJSForWebview((js) => {
        //inject javascript into webview
    })
```