package dev.yerokha.cookscorner.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Yerbolat",
                        email = "yerbolatt312@gmail.com",
                        url = "https://t.me/yerokhych"
                ),
                title = "CooksCorner API",
                description = "OpenApi documentation for CooksCorner Project",
                version = "0.0.1"
        ),
        servers = {
                @Server(
                        description = "Railway App",
                        url = "https://cooks-corner-production.up.railway.app"
                )
        }
)
public class OpenApiConfig {
}

