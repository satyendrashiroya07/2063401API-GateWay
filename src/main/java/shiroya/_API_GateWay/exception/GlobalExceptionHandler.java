package shiroya._API_GateWay.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status;

        if (ex.getMessage().contains("Missing Authorization")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex.getMessage().contains("Invalid Token")) {
            status = HttpStatus.UNAUTHORIZED;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message = ex.getMessage();

        String responseBody = "{ \"status\": " + status.value() +
                ", \"error\": \"" + status.getReasonPhrase() +
                "\", \"message\": \"" + message + "\" }";

        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(bytes);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .add("Content-Type", "application/json");

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
