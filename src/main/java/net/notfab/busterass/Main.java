package net.notfab.busterass;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import net.notfab.busterass.routes.YoutubeExtract;
import org.json.JSONObject;

public class Main implements HttpHandler {

    private final YoutubeExtract extractRoute = new YoutubeExtract();

    Main() {
        Undertow server = Undertow.builder()
                .addHttpListener(8008, "0.0.0.0")
                .setHandler(this)
                .build();
        server.start();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        try {
            if ("/info".equals(exchange.getRequestURI())) {
                JSONObject object = this.extractRoute.handleRequest(exchange);
                if (object != null) {
                    exchange.getResponseSender().send(object.toString());
                } else {
                    throw new IllegalStateException("No response");
                }
            }
        } catch (Exception ex) {
            if (!(ex instanceof IllegalStateException)) {
                ex.printStackTrace();
            }
            exchange.getResponseSender().send(new JSONObject().put("error", true).toString());
        } finally {
            exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "application/json");
        }
    }

}
