package com.xiongyingqi.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author xiongyingqi
 * @since 2016-09-18
 */
public abstract class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static final int    DEFAULT_TIMEOUT = 6000;
    public static final String DEFAULT_CHARSET = "UTF-8";

    public static HttpClient getClient(int timeout) {
        if (timeout <= 0) { // 不允许设置不超时连接
            timeout = DEFAULT_TIMEOUT;
        }
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setSocketTimeout(timeout);
        requestBuilder.setConnectTimeout(timeout);
        requestBuilder.setConnectionRequestTimeout(timeout);

        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestBuilder.build())
                .setHostnameVerifier(new AllowAllHostnameVerifier())
                .build();
        return client;

    }

    public static HttpClient getClient() {
        return getClient(DEFAULT_TIMEOUT);

    }

    /**
     * @param request
     * @return
     */
    public static String executeRequestAndGetStringResponse(HttpRequestBase request, String charset, int timeout) {
        HttpEntity httpEntity = executeRequest(request, charset, timeout);
        try {
            return EntityUtils.toString(httpEntity, charset);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("", e);
            return null;
        }
    }

    /**
     * @param request
     * @return
     */
    public static HttpEntity executeRequest(HttpRequestBase request, String charset, int timeout) {
        if (StringUtils.isEmpty(charset)) {
            charset = DEFAULT_CHARSET;
        }
        HttpResponse response = null;
        try {
            HttpClient client = getClient(timeout);
            if (client == null) {
                logger.error("Failed to get httpclient!");
                return null;
            }
            response = client.execute(request);
            if (response == null) {
                logger.error("response is null!");
                return null;
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("statusCode: {}", statusCode);
                return null;
            }
            HttpEntity entity = response.getEntity();
            return entity;
        } catch (Exception e) {
            logger.error("Http error with message: " + e.getMessage(), e);
            return null;
        } finally {
//            releaseConnection(request);
        }
    }

    /**
     * @param request
     */
    private static void releaseConnection(HttpRequestBase request) {
        if (request != null) {
            request.releaseConnection();
        }
    }

    public static String get(String url, String charset, int timeout) {
        HttpGet httpGet = new HttpGet(url);
        String response = executeRequestAndGetStringResponse(httpGet, charset, timeout);
        if (logger.isDebugEnabled()) {
            logger.debug("request: {}", httpGet.toString());
        }
        return response;
    }

    /**
     * @param postUrl
     * @param reqEntity
     * @return
     */
    public static String post(String postUrl, HttpEntity reqEntity, String charset, int timeout) {
        HttpPost httpPost = new HttpPost(postUrl);
        httpPost.setEntity(reqEntity);
        String response = executeRequestAndGetStringResponse(httpPost, charset, timeout);
        if (logger.isDebugEnabled()) {
            logger.debug("request: {}", reqEntity.toString());
        }
        return response;
    }

    public static String post(String postUrl, String content, String charset, int timeout) {
        StringEntity entity = new StringEntity(content, charset);
        entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
        return post(postUrl, entity, charset, timeout);
    }

    public static String buildUrlParams(String url,
                                        Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        if (!url.contains("?")) {
            builder.append("?");
        } else {
            builder.append("&");
        }
        for (Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator(); iterator
                .hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isEmpty(key)) {
                continue;
            }
            builder.append(key);
            builder.append("=");
            builder.append(value);
            builder.append("&");
        }
        String substring = builder.substring(0, builder.length() - 1);
        return substring;
    }
}
