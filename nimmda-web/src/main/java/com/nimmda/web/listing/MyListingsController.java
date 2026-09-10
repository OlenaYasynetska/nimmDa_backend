package com.nimmda.web.listing;

import com.nimmda.application.listing.ListSellerListingsUseCase;
import com.nimmda.application.listing.ListingView;
import com.nimmda.web.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my")
public class MyListingsController {

    private final ListSellerListingsUseCase listSellerListingsUseCase;

    public MyListingsController(ListSellerListingsUseCase listSellerListingsUseCase) {
        this.listSellerListingsUseCase = listSellerListingsUseCase;
    }

    @GetMapping("/listings")
    public List<ListingView> mine(Authentication authentication) {
        return listSellerListingsUseCase.execute(AuthenticatedUser.id(authentication));
    }
}
