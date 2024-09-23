# Goodbye, Passwords

with Spring Security lead Rob Winch and Spring Developer Advocate Josh Long

[Slides](https://docs.google.com/presentation/d/1mi6xGsGBq9PjxzOj1W9oEtFHBnfE61X8nrr5K_FcHLo/edit?usp=sharing)

## preflight 
* go to safari and delete all the old passkeys. TAN: why are passkeys for the entire OS in their bloody browser?
* shut down all the docker compose instances
* reset database with new import
* install the schema in `misc/legacy-schema.sql` to PostgreSQL by going to `/Users/jlong/Desktop/talk/springone-2024/passwordless-with-rob-winch` and running `init_db.sh`
* Bring up slides

## demo 
* intro
* authN v authZ
* MAKE SURE TO MIGRATE TO `application.yml` ASAP.
* start.spring.io: service is called `auth`, add: `web`, `jdbc`, `security`, `graalvm`, and `postgresql`.
* create a simple hello controller, restart
  ```
  @Controller
  @ResponseBody
  class SecuredEndpoint {
	
      @GetMapping("/")
      Map<String, String> hello(Principal principal) {
          return Map.of("hello", principal.getName());
      }
  }
  ``` 
  * spring boot configures default user/random password
  * where does that come from? u can change with properties
  * but ultimately it's an object. the root of all authentication is `AuthenticationManager`. its a little open ended. 
  * `UserDetailsService` concerns itself specifically with passwords, which are a common type of authentication (but not the only ones, importantly for later) 
  * register `InMemoryUserDetailsManager` with different users and roles for each
  ```
      @Bean
      InMemoryUserDetailsManager userDetailsService() {
          var users = User.withDefaultPasswordEncoder();
          return new InMemoryUserDetailsManager(
                  users.username("rob").password("pw").roles("USER", "ADMIN").build(),
                  users.username("josh").password("pw").roles("USER").build()
          );
      }

  ```
  * add the endpoint: 
  ``` 
      @GetMapping ("/admin")
      Map<String, String> helloPrincipal(Principal principal) {
          return Map.of("hello administrator ", principal.getName());
      }
	
  ```
  * now what do those roles buy us? let's say we created another endpoint (admin-only endpoint) and want to secure it. configure authorization based on roles, like this.
  ```
   @Bean
      SecurityFilterChain securityWebFilterChain(HttpSecurity httpSecurity) throws Exception {
          return httpSecurity
               .authorizeHttpRequests(http -> http
                  .requestMatchers("/admin").hasRole("ADMIN")
                  .anyRequest().authenticated()
              )
              .formLogin(Customizer.withDefaults())
              .build();
      }
  ```	
* so weve got authz,n; go to the `InMemoryUserDetailsManager` and print out rob's `getPasssword()`; it's encoded! Who's done that? `PasswordEncoder` did that.
* RW: password history discussion by Rob
* make sure to disambiguate encoding vs encryption
* turns out we have an existing system setup using sha256. as we talked about, that's no longer secure. but how do we migrate them if the password encoding is a one way ticket? simple, we do it at the time of login, when we have a known-to-be-valid password.
* to see this in action, we have to fix a few things:
* we need to migrate to a durable store. let's use JDBC.
```
	@Bean
	JdbcUserDetailsManager jdbcUserDetailsService(DataSource dataSource) {
		return new JdbcUserDetailsManager(dataSource);
	}
```
* as it stands we won't be able to login. our passwords are in the old format. let's fix that.
* run `select * from users`; see the problem? the data is old.
* run a migration to set the password to be : `update users set password = '{sha256}' || password `
* and finally the JDBC store won't automatically handle persisting the updated passwords. 
* RW: talk about password upgrades
```
@Bean
UserDetailsPasswordService userDetailsPasswordService(UserDetailsManager userDetailsManager) {
	return (user, newPassword) -> {
		var updated = User.withUserDetails(user).password(newPassword).build();
		userDetailsManager.updateUser(updated);
		return updated;
	};
}
``` 
* now restart the app and login as rob or josh and see that their pw in the db users table has been upgraded transparently. nice!
* RW: Passkeys slides
* ensure to install webauthn support
```
git clone https://github.com/rwinch/spring-security-webauthn/
cd spring-security-webauthn
./gradlew publishToMavenLocal
```
* add the dependency:
```
    <dependency>
        <groupId>io.github.rwinch.webauthn</groupId>
        <artifactId>spring-security-webauthn</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
```
* Add snapshot repository
```
	<repositories>
		<repository>
			<id>spring-snapshot</id>
			<url>https://repo.spring.io/snapshot</url>
		</repository>
	</repositories>
```
* specify the following in `pom.xml`:
```
	<spring-security.version>6.4.0-SNAPSHOT</spring-security.version>
```
* add the following to the config:
```
  .with(webauthn() ,c -> c 
		.allowedOrigins("http://localhost:8080")
		.rpId("localhost")
		.rpName("Bootiful Passkeys")
)
  ```
* start the app and visit http://localhost:8080/webauthn/register
* logout
* http://localhost:8080/ log in with passkey
  * NOTE: If fails check if remembered to clear old passkeys
* RW: magic links 101 - slides
* add the onetimetoken configuration element:
```
	.oneTimeTokenLogin(configurer -> configurer.generatedOneTimeTokenSuccessHandler((request, response, oneTimeToken) -> {
		var msg = "go to http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue();
		System.out.println(msg);
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.getWriter().write("you've got console mail!");
	}))
	
```
* EZ! were now logged in with naught but a magic link. now what about your services? do you need to login like this for each service u stand up? wouldnt it be nice if you could centralize all this configuration in one place and give your other services and appos a means to benefit from that? 
* that's the power of Spring Authorization Service.
* to see it in action lets transform our app into a Spring Authorization Server. All we need to do is add the Spring Authorization Server dependency ad then configure an OAuth client. 
* RW: what's an OAuth? 
* so copy and paste the requisite configuration:
```
spring:
  security:
    oauth2:
      authorizationserver:
        client:
          oidc-client:
            registration:
              client-id: "spring"
              client-secret: "spring"
              client-authentication-methods:
                - "client_secret_basic"
              authorization-grant-types:
                - "authorization_code"
                - "refresh_token"
              redirect-uris:
                - "http://127.0.0.1:8081/login/oauth2/code/spring"
              scopes:
                - "openid"
                - "profile"

```
* this establishes a new client called `oidc-client`. other apps will say they're connecting as this client _on behalf_ of a user context. now we need to standup a client. that client will have a filter that rejects requests and forces an authentication. which is of course what we've just spent a lot of time simplifying. lets build a new client. 
* we'll also need a seperate spring authorization security filter chain, like this:
```
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

```
* restart. now we have an authorization server! nice. confirm as much by visiting `http://localhost:8080/.well-known/openid-configuration`. great. 
* now we need a client.
* start.spring.io and create a new project called `oauth-login`: `security` , `web` , `oauth client`.
* configure some properties:
```
spring:

  application:
    name: oauth-login

  security:
    oauth2:
      client:
        registration:
          spring:
            scope: openid,profile
            client-id: spring
		  	client-secret: "{noop}spring"
        provider:
          spring:
            issuer-uri: http://localhost:8080
server:
  port: 8081

```
* add a simple endpoint to the oauth client.
```
@Controller
@ResponseBody
class ClientApplication {

	@GetMapping("/")
	Map<String, String> hello(Principal principal) {
		return Map.of("message", "hello, from client " + principal.getName());
	}
}

```
* restart. visit the oauth client on port `8081`: `http://127.0.0.1:8081`. itll redirect us back to the auth server which in turn wil dump us back on the client, this time after having established a token. so were authenticated but the user never had to enter a pasword on the client. they can trust the auth server as the one true place. u see this scheme countles places. google. facebook. twitter. linkedin. github. etc.
* it works. 
* now what if i have a downstream microservice to which the client wants to make calls? simple. sertup a resource server. 
* start.spring.io: `resource server` , `web`, `graalvm`
* configure port: 8082
* configure issuer uri : http://localhost:8080
* add a controller in the resource server to be exactly the same as in the client:
```
@Controller
@ResponseBody
class ClientController {

	@GetMapping("/")
	Map<String, String> hello(Principal principal) {
		return Map.of("message", "hello, " + principal.getName());
	}
}

```
* and now change the client to relay the token in the request:
```

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
                .uri("http://localhost:8082/")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token.getTokenValue()))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

    }
}

```
* go back to the client as before: `127.0.0.1:8081/`
