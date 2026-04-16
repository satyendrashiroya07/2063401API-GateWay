package shiroya._API_GateWay.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import shiroya._API_GateWay.configuration.AppConfig;

import java.security.Key;
import java.util.List;

@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    private final AppConfig appConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String SECRET = "mysecretkeymysecretkeymysecretkey";

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());


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
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();

                List<String> roles = claims.get("roles", List.class);

                exchange = exchange.mutate()
                        .request(r -> r
                                .header("X-User-Id", userId)
                                .header("X-Roles", String.join(",", roles))
                        )
                        .build();

                // this is not needed because while doing claims validation already doing
                //String username = JwtUtil.validateToken(token);
                System.out.println("Extracted username: " + userId);
                exchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", userId))
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Invalid Token");
            }

            return chain.filter(exchange);
        };
    }
}