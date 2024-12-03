package com.github.pmvieira93.gateway.infrastructure.config;

import dev.openfga.sdk.api.OpenFgaApi;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ApiToken;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.ClientCredentials;
import dev.openfga.sdk.api.configuration.Credentials;
import dev.openfga.sdk.api.configuration.CredentialsMethod;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import lombok.Getter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Getter
@Configuration
@ConditionalOnProperty(name = {"openfga.api.url"})
public class OpenFgaConfig {

    @Value("${openfga.api.url}")
    private String apiUrl;

    @Value("${openfga.api.username}")
    private String apiUsername;

    @Value("${openfga.api.password}")
    private String apiPassword;

    @Value("${openfga.api.token}")
    private String apiToken;

    @Value("${openfga.api.store.id:#{null}}")
    private String storeId;

    @Value("${openfga.api.auth-model.id:#{null}}")
    private String authorizationModelId;

    @Bean
    public ClientConfiguration fgaConfig() {
        var credentials = new Credentials();
        credentials.setCredentialsMethod(CredentialsMethod.NONE);
        if(Objects.nonNull(apiToken)) {
            credentials.setCredentialsMethod(CredentialsMethod.API_TOKEN);
            credentials.setApiToken(new ApiToken(apiToken));
        } else if (Objects.nonNull(apiUsername) && Objects.nonNull(apiPassword)) {
            credentials.setCredentialsMethod(CredentialsMethod.CLIENT_CREDENTIALS);
            credentials.setClientCredentials(new ClientCredentials().clientId(apiUsername).clientSecret(apiPassword));
        }
        var clientConfiguration = new ClientConfiguration();
        clientConfiguration.apiUrl(apiUrl);
        clientConfiguration.credentials(credentials);
        if (Objects.nonNull(storeId)) {
            clientConfiguration.storeId(storeId);
        }
        if (Objects.nonNull(authorizationModelId)) {
            clientConfiguration.authorizationModelId(authorizationModelId);
        }
        return clientConfiguration;
    }

    @Bean
    public OpenFgaClient fgaClient() {
        try {
            return new OpenFgaClient(fgaConfig());
        }catch (FgaInvalidParameterException ex){
            throw new BeanCreationException("Failed to create OpenFGA Client bean", ex);
        }
    }

    @Bean
    public OpenFgaApi fgaApi() {
        try {
            return new OpenFgaApi(fgaConfig());
        } catch (FgaInvalidParameterException ex) {
            throw new BeanCreationException("Failed to create OpenFGA Api bean", ex);
        }
    }
}
