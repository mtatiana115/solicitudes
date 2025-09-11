package co.com.bancolombia.api.config;

import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.auth.gateway.IValidateToken;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {

    private final IValidateToken validateTokenClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/v3/api-docs/**") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-ui/index.html") ||
                path.startsWith("/webjars/swagger-ui") ||
                path.equals("/v3/api-docs/swagger-config") ||
                path.equals("/api/v1/token") ||
                path.equals("/v3/api-docs")

        ) {
            return chain.filter(exchange);
        }
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        if (!auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = auth.replace("Bearer ", "");
        return validateTokenClient.validateToken(token)
                .flatMap(user -> {
                    Authentication authentication = createAuthentication(user);
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder
                                    .withAuthentication(authentication));
                } );


    }
    private Authentication createAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                user.email(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.rolName()))
        );
    }
}
