package co.com.bancolombia.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class RestConsumerConfig {

    private final String url;

    private final int timeout;

    public RestConsumerConfig(@Value("${adapter.restconsumer.url}") String url,
                              @Value("${adapter.restconsumer.timeout}") int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    @Bean
    public WebClient getWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnector())
                // Agregamos un filtro para incluir el token JWT en las solicitudes salientes.
                .filter((request, next) ->
                        // Accede al contexto de seguridad reactivo
                        ReactiveSecurityContextHolder.getContext()
                                .map(ctx -> ctx.getAuthentication())
                                // Nos aseguramos de que la autenticación sea un token JWT
                                .cast(JwtAuthenticationToken.class)
                                .map(JwtAuthenticationToken::getToken)
                                // Si se encuentra el token, modifica la cabecera de la solicitud
                                .map(token -> ClientRequest.from(request)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getTokenValue())
                                        .build()
                                )
                                // Realiza la solicitud con la cabecera modificada
                                .flatMap(next::exchange)
                                // Si no hay token, la solicitud se hace sin la cabecera de autenticación
                                .switchIfEmpty(next.exchange(request))
                )
                .build();
    }

    private ClientHttpConnector getClientHttpConnector() {

        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }

}