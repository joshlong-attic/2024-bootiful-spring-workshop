package bootiful.oauth_login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.Map;

@SpringBootApplication
public class OauthLoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthLoginApplication.class, args);
    }

}

@Controller
@ResponseBody
class ClientController {

    private final RestClient http;

    ClientController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/")
    Map<String, String> hello(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        var token = oAuth2AuthorizedClient.getAccessToken();
        return http
                .get()
                .uri("http://localhost:9091/")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token.getTokenValue()))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

    }
}
