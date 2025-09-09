package com.meli.proxy.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.PathContainer;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

public class MockRequestFactory {

    public static ServerHttpRequest create(String method, String path, String ip) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);

        // Mock método HTTP
        when(request.getMethod()).thenReturn(HttpMethod.valueOf(method));

        // Mock path como PathContainer
        PathContainer pathContainer = PathContainer.parsePath(path);
        when(request.getPath()).thenReturn((RequestPath) pathContainer);

        // Mock headers con IP
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", ip);
        when(request.getHeaders()).thenReturn(headers);

        // Mock dirección remota
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 8080));

        return request;
    }

}
