var exec = require('cordova/exec');
const pluginName = "OneTrust"

var exports = {
  //Initialize Methods
    startSDK:(storageLocation, appId, languageCode, params, success, error) => {
      exec(success, error, pluginName, 'startSDK', [storageLocation, appId, languageCode, params])
    },
    initOTSDKData: (storageLocation, appId, languageCode, success, error) => {
      console.warn("initOTSDKData is deprecated. Use startSDK instead.")
      exec(success, error, pluginName, 'initOTSDKData', [storageLocation, appId, languageCode])
    },
    //Styling
    addCustomStylesAndroid: (jsonObject) => {
      console.warn("addCustomStylesAndroid is deprecated. Pass the JSON into the params argument of startSDK with the key androidUXParams.")
    },
    //UI Methods
    showBannerUI: () => {
      exec(null, null, pluginName, 'showBannerUI')
    },
    showPreferenceCenterUI: () => {
      exec(null, null, pluginName, 'showPreferenceCenterUI')
    },
    showConsentUI: (OTDevicePermission, success, error) => {
      exec(success, error, pluginName, 'showConsentUI', [OTDevicePermission])
    },
    //Query for Consent
    getConsentStatusForCategory: (categoryId, success, error) => {
      exec(success, error, pluginName, 'getConsentStatusForCategory', [categoryId])
    },
    //Boolean banner show methods
    shouldShowBanner: (success, error) => {
      exec(success, error, pluginName, 'shouldShowBanner')
    },
    isBannerShown: (success, error) => {
      exec(success, error, pluginName, 'isBannerShown')
    },
    //Listen for changes
    observeChanges: (categoryId) => {
      exec(null, null, pluginName, 'observeChanges', [categoryId])
    },
    stopObservingChanges: (categoryId) => {
      exec(null, null, pluginName, 'stopObservingChanges', [categoryId])
    },
    //Get OneTrust-set UUID
    getCachedIdentifier: (success, error) =>{
      exec(success, error, pluginName, 'getCachedIdentifier')
    },
    //BYOUI Methods
    getPreferenceCenterData: (success, error) => {
      exec(success,error, pluginName, 'getPreferenceCenterData')
    },
    getBannerData: (success, error) => {
      exec(success, error, pluginName, 'getBannerData')
    },
    //Get JS to inject to webview
    getOTConsentJSForWebview:(success, error) => {
      exec(success, error, pluginName, 'getOTConsentJSForWebview')
    },

    //Force close UI
    dismissUI: () =>{
      exec(null, null, pluginName, 'dismissUI')
    },

    devicePermission: Object.freeze({idfa:0})
  }
  
module.exports = exports
