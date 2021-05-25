package csnight.spider.poly.utils;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    private static PoolingHttpClientConnectionManager cm;
    private final static String jsonModel = "{\"applicationSource\":\"plat_pc\",\"current\":1,\"size\":10,\"atgc\":\"atoken\",\"utgc\":\"utoken\",\"timestamp\":0,\"applicationCode\":\"plat_pc\"}";
    public final static Map<String, String> cookies = new HashMap<>();

    static class IgnoreHostVerification implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static class DummyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private static SSLContext ctx = null;

    static {
        try {
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new TrustManager[]{new DummyTrustManager()},
                    new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new IgnoreHostVerification());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CloseableHttpClient getHttpClient(String url) {
        URI uri = URI.create(url);
        CloseableHttpClient client;
        HttpClientBuilder builder = HttpClientBuilder.create().setMaxConnTotal(10);
        builder.setConnectionManager(cm);
        builder.setMaxConnTotal(20);
        if (uri.getScheme().equalsIgnoreCase("HTTPS")) {
            client = builder.setSSLContext(ctx).setSSLHostnameVerifier(new IgnoreHostVerification()).build();
        } else {
            client = builder.build();
        }
        return client;
    }

    public static HttpRequestBase getHttpRequest(String url, String method) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(20 * 1000)
                .setSocketTimeout(20 * 1000)
                .setConnectionRequestTimeout(20 * 1000)
                .setRelativeRedirectsAllowed(true)
                .setRedirectsEnabled(true)
                .build();
        HttpRequestBase request;
        switch (method.toUpperCase()) {
            default:
            case "GET":
                request = new HttpGet(url);
                break;
            case "POST":
                request = new HttpPost(url);
                break;
            case "DELETE":
                request = new HttpDelete(url);
                break;
            case "PUT":
                request = new HttpPut(url);
                break;
        }
        request.addHeader("Accept", "*/*");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept-Encoding", "gzip, deflate, br");
        request.addHeader("Connection", "keep-alive");
        request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36");
        request.addHeader("Origin", "https://www.polyt.cn");
        request.addHeader("Referer", "https://www.polyt.cn");
        request.addHeader("Host", "platformpcgateway.polyt.cn");
        request.addHeader("sec-ch-ua", "'Not A;Brand';v='99', 'Chromium';v='90', 'Google Chrome';v='90'");
        request.addHeader("sec-ch-ua-mobile", "?0");
        request.addHeader("Sec-Fetch-Dest", "empty");
        request.addHeader("Sec-Fetch-Mode", "cors");
        request.addHeader("Sec-Fetch-Site", "same-site");
        request.setConfig(config);
        return request;
    }

    public static String reqProcessor(String url, String method, String httpType, JSONObject body) {
        String result = "";
        HttpPost requestBase = (HttpPost) HttpUtils.getHttpRequest(url, method);
        try {
            requestBase.addHeader("Cookie", String.join(";", cookies.values().toArray(new String[]{})));
            requestBase.addHeader("httpType", httpType);
            JSONObject model = JSONObject.parseObject(jsonModel);
            model.put("timestamp", System.currentTimeMillis());
            if (body != null) {
                body.put("requestModel", model);
                String sortBody = JSONObject.toJSONString(body, SerializerFeature.MapSortField);
                String aToken = IdentifyUtils.string2MD5(sortBody + "plat_pc", "");
                body.getJSONObject("requestModel").put("atoken", aToken);
                if (method.equals("POST")) {
                    requestBase.setEntity(new StringEntity(body.toString(), "application/json", "utf-8"));
                }
            }
        } catch (Exception e) {
            result = extractStackTrace(e);
            e.printStackTrace();
            return result;
        }
        HttpContext context = new HttpClientContext();
        try (CloseableHttpClient client = HttpUtils.getHttpClient(url);
             CloseableHttpResponse response = client.execute(requestBase, context)) {
            if (response.getStatusLine().getStatusCode() == 307) {
                CloseableHttpResponse redResp = client.execute(requestBase);
                byte[] respBytes = redResp.getEntity().getContent().readAllBytes();
                result = new String(respBytes);
                redResp.close();
            } else {
                Header[] headers = response.getHeaders("Set-Cookie");
                for (Header header : headers) {
                    String[] part = header.getValue().split(";");
                    if (part.length > 0) {
                        cookies.put(part[0].split("=")[0], part[0]);
                    }
                }
                byte[] respBytes = response.getEntity().getContent().readAllBytes();
                result = new String(respBytes);
            }
        } catch (Exception ex) {
            result = extractStackTrace(ex);
            ex.printStackTrace();
        }
        return result;
    }

    public static String extractStackTrace(Throwable var0) {
        StringWriter var1 = new StringWriter();
        PrintWriter var2 = new PrintWriter(var1);
        var0.printStackTrace(var2);
        var2.flush();
        return var1.toString();
    }

    public static void CloseHttpClient(CloseableHttpClient client) {
        try {
            client.close();
            if (cm != null) {
                cm.shutdown();
                cm = null;
                System.gc();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
