package net.deployover.mountyaml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "config.servers")
public class ServerConfigProperties {
    private Map<String, String> listings;

    public Map<String, String> getListings() {
        return listings;
    }

    public void setListings(Map<String, String> listings) {
        this.listings = listings;
    }
}