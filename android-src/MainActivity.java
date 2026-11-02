// language: Java, file: android-src/MainActivity.java
package com.cybercookie.pro;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = getBridge().getWebView();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void injectCookieAndLogin(String siteName, String cookieString) {
                CookieManager cm = CookieManager.getInstance();
                String domain = "";
                String loadUrl = "";

                switch(siteName) {
                    case "facebook":
                        domain = "facebook.com";
                        loadUrl = "https://m.facebook.com";
                        break;
                    case "google":
                        domain = "google.com";
                        loadUrl = "https://accounts.google.com";
                        break;
                    case "whatsapp":
                        domain = "web.whatsapp.com";
                        loadUrl = "https://web.whatsapp.com";
                        break;
                    case "instagram":
                        domain = "instagram.com";
                        loadUrl = "https://www.instagram.com";
                        break;
                    default:
                        domain = "facebook.com";
                        loadUrl = "https://m.facebook.com";
                }

                String formattedCookie = cookieString.trim();
                if (!formattedCookie.endsWith(";")) formattedCookie += ";";

                cm.setCookie("https://" + domain, formattedCookie);
                cm.setCookie("https://www." + domain, formattedCookie);
                cm.setCookie("https://." + domain, formattedCookie);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(loadUrl);
                    }
                });
            }
        }, "CookieInterface");
    }
          }
