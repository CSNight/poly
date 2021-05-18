package csnight.spider.poly.utils;


import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class HttpUtils {
    private static PoolingHttpClientConnectionManager cm;
    private static Registry<ConnectionSocketFactory> reg;
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

    private static class SocksSSLConnectionSocketFactory extends PlainConnectionSocketFactory {
        @Override
        public Socket createSocket(final HttpContext context) {
            InetSocketAddress socksAddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }

    private static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

        public MySSLConnectionSocketFactory(final SSLContext sslContext) {
            super(sslContext);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksAddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddr);
            return new Socket(proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
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

    public static HttpURLConnection getDtHttpConnection(String url, String method, String proxyAddr) throws IOException {
        URI uri = URI.create(url);
        URL urlReq = new URL(url);
        URLConnection connection;
        if (proxyAddr != null && proxyAddr.split(":").length == 3) {
            String[] proxyInfo = proxyAddr.split(":");
            Proxy proxy = new Proxy(Proxy.Type.valueOf(proxyInfo[0].toUpperCase()), new InetSocketAddress(proxyInfo[1].replaceAll("//", ""), Integer.parseInt(proxyInfo[2])));
            connection = urlReq.openConnection(proxy);
        } else {
            connection = urlReq.openConnection();
        }
        setHttpProperties(connection);
        if (uri.getScheme().equalsIgnoreCase("HTTP")) {
            HttpURLConnection httpCon = (HttpURLConnection) connection;
            httpCon.setInstanceFollowRedirects(true);
            httpCon.setRequestMethod(method);
            return httpCon;
        } else {
            HttpsURLConnection httpCon = (HttpsURLConnection) connection;
            httpCon.setInstanceFollowRedirects(true);
            httpCon.setRequestMethod(method);
            return httpCon;
        }
    }

    private static void setHttpProperties(URLConnection connection) {
        connection.setConnectTimeout(1000 * 5);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("Proxy-Connection", "keep-alive");
        connection.setRequestProperty("DNT", "1");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setReadTimeout(1000 * 20);
    }

    public static CloseableHttpClient getHttpClient(String url, String proxyAddr) {
        URI uri = URI.create(url);
        CloseableHttpClient client;
        HttpClientBuilder builder = HttpClientBuilder.create().setMaxConnTotal(10);
        if (proxyAddr != null && !proxyAddr.contains("socks")) {
            HttpHost proxy = HttpHost.create(proxyAddr.replaceAll("socks", "socks5"));
            builder.setProxy(proxy);
        } else if (proxyAddr != null && proxyAddr.contains("socks")) {
            if (cm == null) {
                reg = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new SocksSSLConnectionSocketFactory())
                        .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault())).build();
                cm = new PoolingHttpClientConnectionManager(reg);
            }
            builder.setConnectionManager(cm);
        }
        builder.setMaxConnTotal(20);
        if (uri.getScheme().toUpperCase().equals("HTTPS")) {
            client = builder.setSSLContext(ctx).setSSLHostnameVerifier(new IgnoreHostVerification()).build();
        } else {
            client = builder.build();
        }
        return client;
    }

    public static HttpRequestBase getHttpRequest(String url, String method) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setSocketTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .setRelativeRedirectsAllowed(true)
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
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        request.addHeader("Upgrade-Insecure-Requests", "1");
        request.addHeader("accept", "*/*");
        request.addHeader("Cookie", "NSC_opnbet-cmes-ttm=ffffffff09a49e5445525d5f4f58455e445a4a423660");
        request.addHeader("connection", "Keep-Alive");
        request.addHeader("Proxy-Connection", "keep-alive");
        request.addHeader("DNT", "1");
        request.addHeader("Sec-Fetch-Mode", "navigate");
        request.addHeader("Sec-Fetch-Site", "same-origin");
        request.addHeader("Sec-Fetch-User", "?1");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        request.setConfig(config);
        return request;
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
