//package com.etledge.common.utils;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.nio.charset.Charset;
//import java.util.*;
//
///**
// * http 工具类
// */
//public class HttpClientUtils {
//
//    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
//
//    private static int DEFAULT_CONNECTION_TIMEOUT = 30000 * 10; //单位：ms
//    private static int DEFAULT_SOCKET_TIMEOUT = 30000 * 10;
//
//    private static HttpClient httpClient;
//
//    // 默认编码
//    private static String chaset = "UTF-8";
//
//    /*
//     * 初始化HttpClient实例
//     */
//    static {
//        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//        cm.setMaxTotal(100);
//        cm.setDefaultMaxPerRoute(10);
//        httpClient = HttpClients.custom()
//                .setConnectionManager(cm)
//                .build();
//    }
//
//    /**
//     * 获取HttpClient实例
//     */
//    public static HttpClient getHttpClient() {
//        return httpClient;
//    }
//
//    /**
//     * get请求(不带参数)
//     *
//     * @param url 请求url
//     * @return html 页面数据
//     */
//    public static String get(String url) {
//        HttpGet httpGet = new HttpGet(url);
//        String html = null;
//        try {
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
//                    .build();
//            httpGet.setConfig(requestConfig);
//
//            HttpResponse response = httpClient.execute(httpGet);
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == HttpStatus.SC_OK) {
//                HttpEntity entity = response.getEntity();
//                html = EntityUtils.toString(entity, chaset);
//            } else {
//                throw new RuntimeException(String.format("您访问的链接地址【%s】不可用！", url));
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new ETLException(e.getMessage());
//        } finally {
//            httpGet.releaseConnection();
//        }
//        return html;
//    }
//
//
//    public static String postJson(String url, String jsonStr) throws ETLException {
//        HttpPost httpPost = new HttpPost(url);
//
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
//                .build();
//        httpPost.setConfig(requestConfig);
//
//        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
//
//        String html = null;
//        try {
//            StringEntity se = new StringEntity(jsonStr, "UTF-8");
//            se.setContentType("text/json");
//            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//            httpPost.setEntity(se);
//
//            HttpResponse response = httpClient.execute(httpPost);
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == HttpStatus.SC_OK) {
//                HttpEntity entity = response.getEntity();
//                html = EntityUtils.toString(entity, chaset);
//            } else {
//                throw new RuntimeException(String.format("您访问的链接地址【%s】不可用！", url));
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            throw new ETLException(e.getMessage());
//        } finally {
//            httpPost.releaseConnection();
//        }
//        return html;
//    }
//
//    public static String post(String url, String jsonStr, String apiKey) {
//        // 创建httpClient
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//
//        // 执行http的post请求
//        CloseableHttpResponse httpResponse;
//        String result = null;
//        try {
//            // 创建post请求方式实例
//            HttpPost httpPost = new HttpPost(url);
//            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT).build();
//            httpPost.setConfig(requestConfig);
//
//            // 设置请求头 发送的是json数据格式
//            httpPost.setHeader("Content-type", "application/json;charset=utf-8");
//            httpPost.setHeader("Connection", "Close");
//            httpPost.setHeader("apiKey", apiKey);
//
//            // 设置参数---设置消息实体 也就是携带的数据
//            StringEntity entity = new StringEntity(jsonStr, Charset.forName("UTF-8"));
//            // 设置编码格式
//            entity.setContentEncoding("UTF-8");
//            // 发送Json格式的数据请求
//            entity.setContentType("application/json");
//            // 把请求消息实体塞进去
//            httpPost.setEntity(entity);
//
//            httpResponse = httpClient.execute(httpPost);
//            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new ETLException(e.getMessage());
//        }
//
//        return result;
//    }
//
//    /**
//     * get请求(带参数)
//     *
//     * @param url       请求url
//     * @param paramsMap get请求参数
//     * @return html 页面数据
//     */
//    public static String getWithParams(String url, Map<String, String> headers, Map<String, Object> paramsMap) {
//
//        HttpGet httpGet = null;
//        String html = null;
//        try {
//            url = url + "?" + parseParams(paramsMap);
//            httpGet = new HttpGet(url);
//
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
//                    .build();
//            httpGet.setConfig(requestConfig);
//
//            if (headers != null && headers.size() > 0) {
//                for (Map.Entry<String, String> header : headers.entrySet()) {
//                    httpGet.addHeader(header.getKey(), header.getValue());
//                }
//            }
//
//            HttpResponse response = httpClient.execute(httpGet);
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == HttpStatus.SC_OK) {
//                HttpEntity entity = response.getEntity();
//                html = EntityUtils.toString(entity, chaset);
//            } else {
//                throw new RuntimeException(String.format("您访问的链接地址【%s】不可用！", url));
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new ETLException(e.getMessage());
//        } finally {
//            httpGet.releaseConnection();
//        }
//        return html;
//    }
//
//
//    /**
//     * Content-Type: application/x-www-form-urlencoded; charset=UTF-8
//     *
//     * @param url
//     * @param params
//     * @param headers
//     * @return
//     */
//    public static String doPost(String url, Map<String, Object> params, Map<String, Object> headers) {
//        String result = "";
//        HttpPost httpPost = new HttpPost(url);
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT)
//                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
//                .build();
//        httpPost.setConfig(requestConfig);
//
//        List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            if (entry.getValue() != null && !"".equals(entry.getValue())) {
//                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
//                pairList.add(pair);
//            }
//        }
//        httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName(chaset)));
//        //设置header 参数
//        if (headers != null) {
//            Set<String> keys = headers.keySet();
//            for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
//                String key = (String) i.next();
//                httpPost.addHeader(key, headers.get(key).toString());
//            }
//        }
//        try {
//            HttpResponse response = httpClient.execute(httpPost);
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == HttpStatus.SC_OK) {
//                HttpEntity entity = response.getEntity();
//                result = EntityUtils.toString(entity, chaset);
//            } else {
//                throw new RuntimeException(String.format("您访问的链接地址【%s】不可用！", url));
//            }
//        } catch (IOException e) {
//            logger.error(e.getMessage(), e);
//            throw new ETLException(e.getMessage());
//        } finally {
//            httpPost.releaseConnection();
//        }
//        return result;
//    }
//
//    /**
//     * 转换参数列表用于get请求
//     *
//     * @param paramsMap
//     * @return
//     */
//    private static String parseParams(Map<String, Object> paramsMap) throws UnsupportedEncodingException {
//        String params = "";
//        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
//            params += entry.getKey() + "=" + URLEncoder.encode(String.valueOf(entry.getValue()), chaset) + "&";
//        }
//        if (params.length() > 0)
//            return params.substring(0, params.length() - 1);
//        else
//            return params;
//    }
//
//
//    public static String postWithHeader(String url, Map<String, String> headers, String jsonStr) {
//        // 创建httpClient
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//
//        // 执行http的post请求
//        CloseableHttpResponse httpResponse;
//        String result = null;
//        try {
//            // 创建post请求方式实例
//            HttpPost httpPost = new HttpPost(url);
//            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(600000).setConnectTimeout(600000).build();
//            httpPost.setConfig(requestConfig);
//
//            // 设置请求头 发送的是json数据格式
//            httpPost.setHeader("Content-type", "application/json;charset=utf-8");
//            httpPost.setHeader("Connection", "Close");
//
//            if (headers != null && headers.size() > 0) {
//                for (Map.Entry<String, String> header : headers.entrySet()) {
//                    httpPost.addHeader(header.getKey(), header.getValue());
//                }
//            }
//
//            // 设置参数---设置消息实体 也就是携带的数据
//            StringEntity entity = new StringEntity(jsonStr, Charset.forName("UTF-8"));
//            // 设置编码格式
//            entity.setContentEncoding("UTF-8");
//            // 发送Json格式的数据请求
//            entity.setContentType("application/json");
//            // 把请求消息实体塞进去
//            httpPost.setEntity(entity);
//
//            httpResponse = httpClient.execute(httpPost);
//            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//}
