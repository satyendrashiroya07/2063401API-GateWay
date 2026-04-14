package shiroya._API_GateWay.Jwt;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import shiroya._API_GateWay.configuration.AppConfig;

@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    private final AppConfig appConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtFilter(AppConfig appConfig) {
        super(Config.class);
        this.appConfig = appConfig;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            for (String endpoint : appConfig.getPublicEndpoints()) {
                if (pathMatcher.match(endpoint, path)) {
                    return chain.filter(exchange);
                }
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Missing Authorization Header");
            }

            String token = authHeader.substring(7);

            try {
                String username = JwtUtil.validateToken(token);
                System.out.println("Extracted username: " + username);
                exchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", username))
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Invalid Token");
            }

            return chain.filter(exchange);
        };
    }
}