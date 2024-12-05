package io.javabrains.movie_catalog_service.resources;

import com.netflix.discovery.DiscoveryClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.javabrains.movie_catalog_service.models.CatalogItem;
import io.javabrains.movie_catalog_service.models.Movie;
import io.javabrains.movie_catalog_service.models.Rating;
import io.javabrains.movie_catalog_service.models.UserRatings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.catalog.Catalog;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
    @Autowired
    private RestTemplate restTemplate;

//    @Autowired
//    private DiscoveryClient discoveryClient;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @RequestMapping("/{userId}")
    @CircuitBreaker(name = "backendA", fallbackMethod = "getFallbackCatalog")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {
        // get all rated movie ids
        var ratings = restTemplate.getForObject("http://ratings-data-service/ratingsdata/user/" + userId, UserRatings.class);
//        UserRatings ratings = webClientBuilder.build()
//                .get()
//                .uri("http://ratings-data-service/ratingsdata/user/" + userId)
//                .retrieve()
//                .bodyToMono(UserRatings.class)
//                .block();

        assert ratings != null;
        return ratings.ratings().stream()
                .map(rating -> {
                    var movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.movieId(), Movie.class);

//                    Movie movie = webClientBuilder.build()
//                            .get()
//                            .uri("http://movie-info-service/movies/" + rating.movieId())
//                            .retrieve()
//                            .bodyToMono(Movie.class)
//                            .block();

                    return new CatalogItem(movie.name(), "todo: desc", rating.rating());
                })
                .collect(Collectors.toList());

        // for each movie id, call movie info service and get details
    }

    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId, CallNotPermittedException e) {
        return Arrays.asList(new CatalogItem("No movie", "", 0));
    }
}
