package edu.bellevue.cis530.week6.client;

import edu.bellevue.cis530.week6.dto.PostDto;
import edu.bellevue.cis530.week6.exception.ApiClientException;
import edu.bellevue.cis530.week6.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class JsonPlaceholderClient {

    private final WebClient webClient;

    public JsonPlaceholderClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<PostDto> getAllPosts() {
        try {
            List<PostDto> posts = webClient.get()
                    .uri("/posts")
                    .retrieve()
                    .bodyToFlux(PostDto.class)
                    .collectList()
                    .block();

            return posts != null ? posts : Collections.emptyList();
        } catch (WebClientRequestException ex) {
            throw new ApiClientException("Unable to reach JSONPlaceholder: " + ex.getMessage());
        }
    }

    public PostDto getPostById(Long id) {
        try {
            return webClient.get()
                    .uri("/posts/{id}", id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            response -> Mono.error(new ResourceNotFoundException("Post not found with id: " + id)))
                    .onStatus(status -> status.is5xxServerError(),
                            response -> Mono.error(new ApiClientException("JSONPlaceholder returned a server error")))
                    .bodyToMono(PostDto.class)
                    .block();
        } catch (WebClientRequestException ex) {
            throw new ApiClientException("Unable to reach JSONPlaceholder: " + ex.getMessage());
        }
    }

    public List<PostDto> getPostsByUserId(Long userId) {
        try {
            List<PostDto> posts = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/posts")
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .bodyToFlux(PostDto.class)
                    .collectList()
                    .block();

            return posts != null ? posts : Collections.emptyList();
        } catch (WebClientRequestException ex) {
            throw new ApiClientException("Unable to reach JSONPlaceholder: " + ex.getMessage());
        }
    }
}