package io.roadmap.filestorage.intecrceptors;


import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public  class DecodeParamsInterceptor implements  HandlerInterceptor {

//    @Override
//    public  boolean  preHandle (HttpServletRequest request, HttpServletResponse response, Object handler)  throws Exception {
//        System.out.println( "PreHandle: Перехват запроса..." );
//        System.out.println( "URI запроса: " + request.getRequestURI());
//        request = new DecodedRequestWrapper(request);
//
//        return  true ; // Продолжить обработку запроса
//    }

//    @Override
//    public  void  postHandle (HttpServletRequest request, HttpServletResponse response, Object handler, org.springframework.web.servlet.ModelAndView modelAndView)  throws Exception {
//        System.out.println( "PostHandle: Обработка запроса завершена." );
//    }

//    @Override
//    public  void  afterCompletion (HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)  throws Exception {
//        System.out.println( "AfterCompletion: Обработка запроса завершена." );
//    }

    public class DecodedRequestWrapper extends HttpServletRequestWrapper {
        public DecodedRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return decode(value);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> original = super.getParameterMap();
            Map<String, String[]> decoded = new LinkedHashMap<>();

            for (Map.Entry<String, String[]> entry : original.entrySet()) {
                String[] decodedValues = Arrays.stream(entry.getValue())
                        .map(this::decode)
                        .toArray(String[]::new);
                decoded.put(entry.getKey(), decodedValues);
            }
            return decoded;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;

            return Arrays.stream(values)
                    .map(this::decode)
                    .toArray(String[]::new);
        }

        private String decode(String value) {
            if (value == null) return null;
            try {
                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to decode parameter: {}", value);
                return value;
            }
        }
    }
}