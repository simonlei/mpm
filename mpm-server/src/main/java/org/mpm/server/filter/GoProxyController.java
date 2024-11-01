package org.mpm.server.filter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoProxyController {
    
    @PostMapping("/api/**")
    public ResponseEntity goProxy(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        Streams.readAndClose(new InputStreamReader(request.getInputStream()), body);
        Header header = Header.create();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            header.set(name, request.getHeader(name));
        }
        header.set("FromTeko", "true");
        Response resp = Http.post3("localhost:18880" + request.getRequestURI(), body.toString(), header,
                60 * 60 * 1000);

        ResponseEntity responseEntity = new ResponseEntity(Streams.readBytesAndClose(resp.getStream()),
                HttpStatus.valueOf(resp.getStatus()));
        return responseEntity;
    }

}
