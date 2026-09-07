package com.nimmda.web.listing;

import com.nimmda.application.listing.GetListingUseCase;
import com.nimmda.application.listing.ListPublishedListingsUseCase;
import com.nimmda.application.listing.ListSellerListingsUseCase;
import com.nimmda.application.listing.ListingView;
import com.nimmda.application.listing.PublishListingCommand;
import com.nimmda.application.listing.PublishListingUseCase;
import com.nimmda.application.listing.UpdateListingStatusUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListPublishedListingsUseCase listPublishedListingsUseCase;
    private final ListSellerListingsUseCase listSellerListingsUseCase;
    private final GetListingUseCase getListingUseCase;
    private final PublishListingUseCase publishListingUseCase;
    private final UpdateListingStatusUseCase updateListingStatusUseCase;

    public ListingController(
            ListPublishedListingsUseCase listPublishedListingsUseCase,
            ListSellerListingsUseCase listSellerListingsUseCase,
            GetListingUseCase getListingUseCase,
            PublishListingUseCase publishListingUseCase,
            UpdateListingStatusUseCase updateListingStatusUseCase
    ) {
        this.listPublishedListingsUseCase = listPublishedListingsUseCase;
        this.listSellerListingsUseCase = listSellerListingsUseCase;
        this.getListingUseCase = getListingUseCase;
        this.publishListingUseCase = publishListingUseCase;
        this.updateListingStatusUseCase = updateListingStatusUseCase;
    }

    @GetMapping
    public List<ListingView> list(@RequestParam(required = false) String category) {
        return listPublishedListingsUseCase.execute(category);
    }

    @GetMapping("/mine")
    public List<ListingView> mine(Authentication authentication) {
        return listSellerListingsUseCase.execute(authentication.getName());
    }

    @GetMapping("/{id}")
    public ListingView get(@PathVariable String id) {
        return getListingUseCase.execute(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingView publish(Authentication authentication, @Valid @RequestBody PublishListingRequest request) {
        return publishListingUseCase.execute(new PublishListingCommand(
                authentication.getName(),
                request.title(),
                request.price(),
                request.category(),
                request.location(),
                request.imageSrc()
        ));
    }

    @PatchMapping("/{id}")
    public ListingView updateStatus(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ListingStatusRequest request
    ) {
        return updateListingStatusUseCase.execute(id, authentication.getName(), request.status());
    }
}
