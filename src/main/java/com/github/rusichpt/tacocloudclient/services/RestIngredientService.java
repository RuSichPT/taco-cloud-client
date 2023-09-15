package com.github.rusichpt.tacocloudclient.services;

import com.github.rusichpt.tacocloudclient.Ingredient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Arrays;
import java.util.Objects;

@Service
public class RestIngredientService implements IngredientService {
    private final RestTemplate restTemplate;

    public RestIngredientService(String accessToken) {
        this.restTemplate = new RestTemplate();
        if (accessToken != null) {
            this.restTemplate
                    .getInterceptors()
                    .add(getBearerTokenInterceptor(accessToken));
        }
    }

    private ClientHttpRequestInterceptor getBearerTokenInterceptor(String accessToken) {
        return (request, bytes, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + accessToken);
            return execution.execute(request, bytes);
        };
    }

    @Bean
    @RequestScope
    public IngredientService ingredientService(OAuth2AuthorizedClientService clientService) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = null;
        if (authentication.getClass()
                .isAssignableFrom(OAuth2AuthenticationToken.class)) {
            OAuth2AuthenticationToken oauthToken =
                    (OAuth2AuthenticationToken) authentication;
            String clientRegistrationId =
                    oauthToken.getAuthorizedClientRegistrationId();
            if (clientRegistrationId.equals("taco-admin-client")) {
                OAuth2AuthorizedClient client =
                        clientService.loadAuthorizedClient(
                                clientRegistrationId, oauthToken.getName());
                accessToken = client.getAccessToken().getTokenValue();
            }
        }
        return new RestIngredientService(accessToken);
    }


    @Override
    public Iterable<Ingredient> findAll() {
        return Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(
                "http://localhost:8081/api/ingredients",
                Ingredient[].class)));
    }

    @Override
    public Ingredient addIngredient(Ingredient ingredient) {
        return restTemplate.postForObject(
                "http://localhost:8081/api/ingredients",
                ingredient,
                Ingredient.class);

    }
}
