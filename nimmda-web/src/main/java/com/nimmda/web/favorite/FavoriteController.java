package com.nimmda.web.favorite;

import com.nimmda.application.favorite.ListFavoritesUseCase;
import com.nimmda.application.favorite.ToggleFavoriteUseCase;
import com.nimmda.application.listing.ListingView;
import com.nimmda.web.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final ListFavoritesUseCase listFavoritesUseCase;
    private final ToggleFavoriteUseCase toggleFavoriteUseCase;

    public FavoriteController(
            ListFavoritesUseCase listFavoritesUseCase,
            ToggleFavoriteUseCase toggleFavoriteUseCase
    ) {
        this.listFavoritesUseCase = listFavoritesUseCase;
        this.toggleFavoriteUseCase = toggleFavoriteUseCase;
    }

    @GetMapping
    public List<ListingView> list(Authentication authentication) {
        return listFavoritesUseCase.execute(AuthenticatedUser.id(authentication));
    }

    @PostMapping("/{listingId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingView add(Authentication authentication, @PathVariable String listingId) {
        return toggleFavoriteUseCase.execute(AuthenticatedUser.id(authentication), listingId, true);
    }

    @DeleteMapping("/{listingId}")
    public ListingView remove(Authentication authentication, @PathVariable String listingId) {
        return toggleFavoriteUseCase.execute(AuthenticatedUser.id(authentication), listingId, false);
    }
}
