package com.nimmda.web.listing;

import com.nimmda.application.listing.GetListingUseCase;
import com.nimmda.application.listing.ListPublishedListingsUseCase;
import com.nimmda.application.listing.ListingView;
import com.nimmda.application.listing.PublishListingCommand;
import com.nimmda.application.listing.PublishListingUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final GetListingUseCase getListingUseCase;
    private final PublishListingUseCase publishListingUseCase;

    public ListingController(
            ListPublishedListingsUseCase listPublishedListingsUseCase,
            GetListingUseCase getListingUseCase,
            PublishListingUseCase publishListingUseCase
    ) {
        this.listPublishedListingsUseCase = listPublishedListingsUseCase;
        this.getListingUseCase = getListingUseCase;
        this.publishListingUseCase = publishListingUseCase;
    }

    @GetMapping
    public List<ListingView> list(@RequestParam(required = false) String category) {
        return listPublishedListingsUseCase.execute(category);
    }

    @GetMapping("/{id}")
    public ListingView get(@PathVariable String id) {
        return getListingUseCase.execute(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingView publish(@Valid @RequestBody PublishListingRequest request) {
        return publishListingUseCase.execute(new PublishListingCommand(
                request.sellerId(),
                request.title(),
                request.price(),
                request.category(),
                request.location(),
                request.imageSrc()
        ));
    }
}
