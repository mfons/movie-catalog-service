package io.javabrains.movie_catalog_service.resources;

import com.netflix.discovery.DiscoveryClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.javabrains.movie_catalog_service.models.CatalogItem;
import io.javabrains.movie_catalog_service.models.Movie;
import io.javabrains.movie_catalog_service.models.Rating;
import io.javabrains.movie_catalog_service.models.UserRatings;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.xml.catalog.Catalog;
import java.nio.charset.StandardCharsets;
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
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId, HttpServletRequest request) {
        // get all rated movie ids
        //var ratings = restTemplate.getForObject("http://ratings-data-service/ratingsdata/user/" + userId, UserRatings.class);
        //HttpEntity<String> entity = getStringHttpEntity("user:385b36f8-fe63-4579-abde-3937d6f3ebcc");
        HttpEntity<String> entity = getStringHttpEntity("michael:password");
        var ratings = restTemplate.exchange("http://ratings-data-service/ratingsdata/user/" + userId, HttpMethod.GET, entity, UserRatings.class);
//        UserRatings ratings = webClientBuilder.build()
//                .get()
//                .uri("http://ratings-data-service/ratingsdata/user/" + userId)
//                .retrieve()
//                .bodyToMono(UserRatings.class)
//                .block();

        assert ratings.getBody() != null;
        System.out.println("*****HEY...the session id is :  " + request.getSession().getId());
        return ratings.getBody().ratings().stream()
                .map(rating -> {
                    HttpEntity<String> entityMovieInfo = getStringHttpEntity("michael:password");
                    var movie = restTemplate.exchange("http://movie-info-service/movies/" + rating.movieId(), HttpMethod.GET, entityMovieInfo, Movie.class);

//                    Movie movie = webClientBuilder.build()
//                            .get()
//                            .uri("http://movie-info-service/movies/" + rating.movieId())
//                            .retrieve()
//                            .bodyToMono(Movie.class)
//                            .block();

                    return new CatalogItem(movie.getBody().name(), "todo: desc", rating.rating());
                })
                .collect(Collectors.toList());

        // for each movie id, call movie info service and get details
    }

    private static HttpEntity<String> getStringHttpEntity(String auth) {
        byte[] encodedAuth = Base64.encodeBase64String(auth.getBytes(StandardCharsets.UTF_8)).getBytes();
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return entity;
    }

    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId, HttpServletRequest request, CallNotPermittedException e) {
        return Arrays.asList(new CatalogItem("No movie", "", 0));
    }
}
