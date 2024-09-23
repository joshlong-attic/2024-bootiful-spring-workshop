package bootiful.passwordless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

import static org.springframework.security.config.annotation.web.configurers.WebauthnConfigurer.webauthn;

@SpringBootApplication
public class PasswordlessApplication {

    public static void main(String[] args) {
        SpringApplication.run(PasswordlessApplication.class, args);
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    UserDetailsPasswordService userDetailsPasswordService(JdbcUserDetailsManager userDetailsManager) {
        return (user, newPassword) -> {
            var updated = User.withUserDetails(user).password(newPassword).build();
            userDetailsManager.updateUser(updated);
            return updated;
        };
    }

    @Bean
    @Order(1)
    SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        http
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oneTimeTokenLogin(configurer -> configurer.generatedOneTimeTokenSuccessHandler((request, response, oneTimeToken) -> {
                    var msg = "go to http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue();
                    System.out.println(msg);
                    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                    response.getWriter().print("you've got console mail!");
                }))
                .with(webauthn(), c -> c
                        .rpId("localhost")
                        .rpName("bootiful passkeys")
                        .allowedOrigins("http://localhost:8080")
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }
}

@Controller
@ResponseBody
class SecuredController {

    @GetMapping("/admin")
    Map<String, String> admin(Principal principal) {
        return Map.of("admin", principal.getName());
    }

    @GetMapping("/")
    Map<String, String> hello(Principal principal) {
        return Map.of("user", principal.getName());
    }
}