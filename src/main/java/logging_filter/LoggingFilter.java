package logging_filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.MDC;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Collectors;

public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put("Trace-ID", UUID.randomUUID().toString());
        CachedBodyHttpServletRequest requestWrapper = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            logger.info(createRequestLog(requestWrapper));
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logger.info(createResponseLog(responseWrapper));
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private String createRequestLog(CachedBodyHttpServletRequest request) {
        StringBuilder msg = new StringBuilder("STARE, ");
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null) {
            msg.append('?').append(queryString);
        }

        String client = request.getRemoteAddr();
        if (StringUtils.hasLength(client)) {
            msg.append(", client=").append(client);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            msg.append(", session=").append(session.getId());
        }

        String user = request.getRemoteUser();
        if (user != null) {
            msg.append(", user=").append(user);
        }

        msg.append(", headers=").append(new ServletServerHttpRequest(request).getHeaders());
        msg.append(", body=").append(request.getReader().lines().collect(Collectors.joining("\n")));
        return msg.toString();
    }

    private String createResponseLog(ContentCachingResponseWrapper response) throws UnsupportedEncodingException {
        StringBuilder msg = new StringBuilder("END");
        msg.append(", headers=[");
        for (Iterator<String> iterator = response.getHeaderNames().iterator(); iterator.hasNext(); ) {
            String headerName = iterator.next();
            msg.append(headerName).append(":");
            if (!iterator.hasNext()) {
                msg.append(response.getHeader(headerName));
                break;
            }
            msg.append(response.getHeader(headerName)).append(", ");
        }
        msg.append("]");
        msg.append(", body=").append(new String(response.getContentAsByteArray(), response.getCharacterEncoding()));
        return msg.toString();
    }
}
