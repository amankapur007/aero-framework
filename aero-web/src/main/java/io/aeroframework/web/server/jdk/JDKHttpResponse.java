package io.aeroframework.web.server.jdk;

import com.sun.net.httpserver.HttpExchange;
import io.aeroframework.web.server.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDKHttpResponse implements HttpResponse {
    private final HttpExchange exchange;
    private int status = 200;
    private String body;
    private Map<String , List<String>> headers;

    public JDKHttpResponse(HttpExchange exchange) {
        this.exchange = exchange;
        this.headers = exchange.getResponseHeaders();
        if(this.headers == null)
            this.headers = new HashMap<>();
    }


    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void commit(){
        for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
            exchange.getResponseHeaders().put(entry.getKey(), entry.getValue());
        }

        if(this.body != null){
            try {
                this.exchange.sendResponseHeaders(status,this.body.length());
            } catch (IOException e) {
                throw new RuntimeException("Unable to commit response.", e);
            }
            try(OutputStream os = exchange.getResponseBody()){
                os.write(this.body.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Unable to commit response.", e);
            }

        }

        this.exchange.close();
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setHeader(String name, String value) {
        if(headers.containsKey(name)) {
            headers.get(name).add(value);
        }
        else {
            headers.put(name, new ArrayList<>());
            headers.get(name).add(value);
        }
    }
}
