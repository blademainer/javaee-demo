package com.xiongyingqi.filter;

import com.xiongyingqi.util.HttpUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

/**
 * @author xiongyingqi
 * @since 16-10-24 下午12:39
 */
public class RedirectFilter implements Filter {
    private static final Logger logger       = LoggerFactory.getLogger(RedirectFilter.class);
    private              String host         = "baidu.com";
    private              String redirectHost = "https://baidu.com";

    /**
     * Called by the web container to indicate to a filter that it is
     * being placed into service.
     * <p>The servlet container calls the init
     * method exactly once after instantiating the filter. The init
     * method must complete successfully before the filter is asked to do any
     * filtering work.
     * <p>The web container cannot place the filter into service if the init
     * method either
     * <ol>
     * <li>Throws a ServletException
     * <li>Does not return within a time period defined by the web container
     * </ol>
     *
     * @param filterConfig
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the
     * container each time a request/response pair is passed through the
     * chain due to a client request for a resource at the end of the chain.
     * The FilterChain passed in to this method allows the Filter to pass
     * on the request and response to the next entity in the chain.
     * <p>A typical implementation of this method would follow the following
     * pattern:
     * <ol>
     * <li>Examine the request
     * <li>Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering
     * <li>Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering
     * <li>
     * <ul>
     * <li><strong>Either</strong> invoke the next entity in the chain
     * using the FilterChain object
     * (<code>chain.doFilter()</code>),
     * <li><strong>or</strong> not pass on the request/response pair to
     * the next entity in the filter chain to
     * block the request processing
     * </ul>
     * <li>Directly set headers on the response after invocation of the
     * next entity in the filter chain.
     * </ol>
     *
     * @param request
     * @param response
     * @param chain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        //        invokeAllGetMethods(HttpServletRequest.class, httpServletRequest);
        //        invokeAllGetMethods(HttpServletResponse.class, httpServletResponse);
        String queryString = httpServletRequest.getQueryString();
        if(logger.isDebugEnabled()) {
            logger.debug("QueryString={}", queryString);
        }
        try {
            redirect(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //        chain.doFilter(request, response);
    }

    private void redirect(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws Exception {
        String method = httpServletRequest.getMethod();
        HttpRequestBase requestBase = null;

        String url = redirectHost + httpServletRequest.getRequestURI()
                + (httpServletRequest.getQueryString() == null ?
                "" :
                "?" + httpServletRequest.getQueryString());
        if ("get".equalsIgnoreCase(method)) {
            requestBase = new HttpGet(url);
        } else if ("post".equalsIgnoreCase(method)) {
            requestBase = new HttpPost(url);
            ServletInputStream inputStream = httpServletRequest.getInputStream();
            HttpEntity entity = new InputStreamEntity(inputStream);
            ((HttpPost) requestBase).setEntity(entity);
        } else if ("put".equalsIgnoreCase(method)) {
            requestBase = new HttpPut(url);
            ServletInputStream inputStream = httpServletRequest.getInputStream();
            InputStreamEntity entity = new InputStreamEntity(inputStream, httpServletRequest.getContentLengthLong());
            ((HttpPut) requestBase).setEntity(entity);
        } else if ("delete".equalsIgnoreCase(method)) {
            requestBase = new HttpDelete(url);
        }

        if (requestBase == null) {
            return;
        }
        //setHeaders(httpServletRequest, requestBase);
        requestBase.setHeader("Host", host);

        HttpEntity entity = HttpUtils.executeRequest(requestBase, "UTF-8", 1000);
        if (entity == null) {
            logger.error("No entity return with request: {}!", requestBase);
            return;
        }
        //        if(logger.isDebugEnabled()) {
        //            String response = EntityUtils.toString(entity, "utf-8");
        //            logger.debug("Getting response:{}", response);
        //        }

        //        System.out.println("response=========" + string);

        //setResponseHeaders(httpServletResponse, entity);
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        try {
            entity.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            requestBase.releaseConnection();
            outputStream.flush();
            outputStream.close();
        }
    }

    private void setHeaders(HttpServletRequest httpServletRequest, HttpRequestBase requestBase) {
        for (Enumeration<String> headerNames = httpServletRequest.getHeaderNames(); headerNames
                .hasMoreElements(); ) {
            String name = headerNames.nextElement();
            String value = httpServletRequest.getHeader(name);
            if (logger.isDebugEnabled()) {
                logger.debug("Setting header to http client: {}={}", name, value);
            }
            requestBase.setHeader(name, value);
        }
    }

    private void setResponseHeaders(HttpServletResponse httpServletResponse, HttpEntity entity) {
        Header contentType = entity.getContentType();
        if (contentType != null) {
            httpServletResponse.setHeader(contentType.getName(), contentType.getValue());
        }

        Header contentEncoding = entity.getContentEncoding();
        if (contentEncoding != null) {
            httpServletResponse.setHeader(contentEncoding.getName(), contentEncoding.getValue());
        }

        long contentLength = entity.getContentLength();
        httpServletResponse.setContentLengthLong(contentLength);

    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     * <p>This method is only called once all threads within the filter's
     * doFilter method have exited or after a timeout period has passed.
     * After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter.
     * <p>This method gives the filter an opportunity to clean up any
     * resources that are being held (for example, memory, file handles,
     * threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
    @Override
    public void destroy() {

    }

    private void invokeAllGetMethods(Class clazz, Object o) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            String name = declaredMethod.getName();
            if (!(name.startsWith("get") && Modifier
                    .isPublic(declaredMethod.getModifiers()))) {
                continue;
            }
            try {
                Object invoke = declaredMethod.invoke(o);
                System.out.println(clazz.getName() + "." + name + "=========" + invoke);
            } catch (Exception e) {
                System.out.println(name + " invoke error");
            }
        }
    }

    private void invokeAllGetMethods(Object o) {
        invokeAllGetMethods(o.getClass(), o);
    }

    //    public static void main(String[] args) {
    //    }
}
