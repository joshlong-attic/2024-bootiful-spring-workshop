package bootiful.resource_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;

@SpringBootApplication
public class ResourceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceServerApplication.class, args);
	}

}

@Controller
@ResponseBody
class ClientController {

	@GetMapping("/")
	Map<String, String> hello(Principal principal) {
		return Map.of("message", "hello from the resource server, " + principal.getName());
	}
}
