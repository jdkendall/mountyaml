package net.deployover.mountyaml.controllers;

import net.deployover.mountyaml.config.ServerConfigProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DemoController {

    ServerConfigProperties config;

    public DemoController(ServerConfigProperties config) {
        this.config = config;
    }

    @GetMapping("/servers")
    public ServerListings get() {
        // Read Spring config property config.server.listings from application.yaml
        // which contains a dynamic list of key/value pairs into a ServerListings object
        List<ServerListing> listings = config.getListings()
                .entrySet()
                .stream()
                .map(e -> new ServerListing(e.getKey(), e.getValue()))
                .toList();
        return new ServerListings(listings);
    }
}
