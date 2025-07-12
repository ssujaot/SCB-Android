# OneTrust CMP on Ionic
## Overview
OneTrust provides a Cordova plugin that can be used in Ionic implementations as well.

This documentation has examples in React. Capacitor is not required, but is compatible with this plugin.
## Installation
### Application Requirements
For Android implementations, AndroidX must be enabled in the Android platform. For many newer Ionic/Capacitor projects, this is on by default.

### SDK Version Requirements
Note that the version of the SDK must match the version of the JSON that you have published from your OneTrust instance. For example, if you have published your JSON with version 6.10.0, you must use the 6.10.0 version of the OneTrust plugin. It is recommend that a static version is specified to prevent automatic updates, as a JSON publish will need to occur with every update.

### Install Command
To install the OneTrust Cordova Plugin, download the plugin locally and run the following command in your project folder, pointing to the plugin folder. This copies the contents of the plugin into your project; there's no need to maintain a separate plugin folder.
#### With Cordova:
```
ionic cordova plugin add cordova-plugin-onetrust-cmp@6.17.0 
```
#### With Capacitor:
```
npm install cordova-plugin-onetrust-cmp@6.17.0
npx cap sync
```

#### With Cordova:
```
cordova plugin cordova-plugin-onetrust update
```
#### With Capacitor:
```
npm update cordova-plugin-onetrust
npx cap update
```

### Uninstalling
To uninstall, run the following in your project folder.
#### With Cordova:
```
cordova plugin remove cordova-plugin-onetrust
```
#### With Capacitor:
```
npm uninstall cordova-plugin-onetrust
npx cap sync
```
# Declare OneTrust
Before using any of OneTrust's methods, you will have to declare the OneTrust variable in your file, with type `any`.
```typescript
declare var OneTrust:any;
```
# Initialization
The OneTrust SDK retrieves an object that contains all the data needed to present a UI to a User to collect consent for the SDKs used in your application. The data returned is determined by the configurations made in the OneTrust Admin console.

## `startSDK`
This method is the initialization method for the SDK. It makes between 1-2 network calls, depending if an IAB2 template is fetched or not.

```typescript
declare var OneTrust;

OneTrust.startSDK("storageLocation","domainIdentifer","languageCode", params, (status:boolean) => {
    console.log("SDK Downloaded Successfully!")
},(error:string) =>{
  console.log("SDK Failed to initialize! Error = "+error)  
})
```
## Arguments
Argument|Description|Required|
|-|-|-|
|storageLocation|**String** the CDN location for the JSON that the SDK fetches.|Yes|
|domainIdentifier|**String** the application GUID from the OneTrust Admin Console|Yes|
|languageCode|**String** 2-digit ISO language code used to return content in a specific language|Yes
|params |**JSON Object** object containing additional setup params, detailed below|No, nullable|
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
    regionCode: "CA"
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
```typescript
OneTrust.showBannerUI()
```
### Determine if a banner should be shown
You can determine whether or not a banner should be shown by calling
```typescript
OneTrust.shouldShowBanner((result:boolean) => {
    console.log("A banner should be shown = "+result)
})
```
This returns a boolean that determines whether or not the banner should be displayed to the user. See [this page](https://developer.onetrust.com/sdk/mobile-apps/ios/displaying-ui#shouldshowbanner) for the logic used to determine the result of `shouldShowBanner()`.

**Example** <br>
Below is an example of how to combine the initialization success handler with the shouldShowBanner function to automatically show a banner, if required, once the SDK download has completed.

```typescript
OneTrust.startSDK(storageLocation,domainId,langCode, params, (status) =>{
    OneTrust.shouldShowBanner((shouldShow:boolean) => {
        if(status && shouldShow){
            OneTrust.showBannerUI()
        }
    })
}, (error:string) =>{
    console.log(error)
})

```
## Preference Center
The preference center can be shown by calling
```typescript
declare var OneTrust:any;

OneTrust.showPreferenceCenterUI()
```

## Force Close UI
If the UI needs to be dismissed programmatically, simply call
```javascript
OneTrust.dismissUI()
```

## App Tracking Transparency Pre-Prompt
The App Tracking Transparency Pre-Prompt can be shown by calling the below function. `OneTrust.devicePermission.idfa` is an enum for the type of permission. On Android and iOS versions < 14, requesting permission of type `ifda` will resolve a promise with a value of `-1` every time.
```javascript
OneTrust.showConsentUI(window.OneTrust.devicePermission.idfa, status =>{
    console.log(`The new ATT Status is ${status}`)
})
```

## Android - Custom styling with UXParams JSON
OneTrust allows you to add custom styling to your preference center by passing in style JSON in a certain format. Build out your JSON by following the guide in the [OneTrust Developer Portal](https://developer.onetrust.com/sdk/mobile-apps/android/customize-ui).

Add the JSON to the `params` argument of the `startSDK` method when initializing.

## iOS - Custom Styling with UXParams Plist
Custom styling can be added to your iOS Cordova application by using a .plist file in the iOS platform code. In addition to adding the .plist file (which can be obtained from the OneTrust Demo Application) to your bundle, there are a few changes that need to be made in the platform code, outlined below. Review the guide in the [OneTrust Developer Portal](https://developer.onetrust.com/sdk/mobile-apps/ios/customize-ui).

In `AppDelegate.swift`, import OTPublishersHeadlessSDK. Add an extension below the class to handle the protocol methods:

```swift
import OTPublishersHeadlessSDK
...
extension AppDelegate: UIConfigurator{
    func shouldUseCustomUIConfig() -> Bool {
        return true
    }
    
    func customUIConfigFilePath() -> String? {
        return Bundle.main.path(forResource: "OTSDK-UIConfig-iOS", ofType: "plist")
    }
}

```

In the `didFinishLaunchingWithOptions` protocol method, assign the AppDelegate as the UIConfigurator:

```swift
func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    OTPublishersHeadlessSDK.shared.uiConfigurator = self
    return true
  }
```

## UI Interaction Event
After the UI is dismissed from your application, a document event is broadcast. You can listen for this event to detect the UI dismissed.

```typescript
document.addEventListener('allSDKViewsDismissed', () =>{
    console.log("All OneTrust Views dismissed")
}, false)
```

# Get UI Data as JSON
After initialization, the data used to build a preference center or banner can be retrieved easily using the following methods. Each has a success and error handler, so if the download fails and there is no cached data, errors can be handled gracefully. This can also be used to determine whether or not OneTrust has the data required to operate offline (eg. if `getBannerData` does not return an error, at least one download has completed successfully and the required data is present to render a banner.)

```javascript
    OneTrust.getBannerData((data) => {
        console.log(`Banner Data = ${JSON.stringify(data)}`)
    }, (error) => {
        console.error(`No banner data! \n Error: ${error}`)
    })

    OneTrust.getPreferenceCenterData((data) => {
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
```typescript
OneTrust.getConsentStatusForCategory("C0004", (status:number) => {
        console.log("Queried consent status for C0004 is "+status)
    })
```

## Listening for Consent Changes
The OneTrust SDK emits events as consent statuses change. You must tell the platform code which events to listen for. Call this method for each category you wish to start observing.

```typescript
OneTrust.observeChanges("C0004")
```

Once a category is being observed, an event will be fired whenever the consent for that category changes. The listener carries a payload with the `categoryId` as a string and the `consentStatus` as an integer.

```typescript
document.addEventListener('C0004',(payload:any) => {
    console.log("The new status of "+payload.categoryId+" is now"+payload.consentStatus)
}, false)
```

Stop listening for consent changes by calling the following for each category you'd like to cancel.
```typescript
OneTrust.stopObservingChanges("C0004")
```

## Retrieving the Cached Data Subject Identifier
The Data Subject Identifier that OneTrust sets can be retrieved by calling the following method. If Consent Logging is enabled, you will be able to retrieve the receipt for any consent choices made by the user inside the Consent module.

```javascript
OneTrust.getCachedIdentifier((identifier) => {
          console.log(`Data Subject Identifier is ${identifier}.`)
        })
```

## Pass OneTrust Consent to a WebView
If your application uses WebViews to present content and the pages rendered are running the OneTrust Cookies CMP, you can inject a JavaScript variable, provided by OneTrust, to pass consent from the native application to your WebView.

The JavaScript must be evaluated before the Cookies CMP loads in the webview, therefore, it is recommended to evaluate the JS early on in the WebView load cycle.
```javascript
    OneTrust.getOTConsentJSForWebview((js) => {
        //inject javascript into webview
    })
```