package com.nimmda.application.admin;

import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingRepository;
import com.nimmda.domain.user.AccountMode;
import com.nimmda.domain.user.User;
import com.nimmda.domain.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDirectoryService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public AdminDirectoryService(UserRepository userRepository, ListingRepository listingRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public AdminOverview overview() {
        List<User> users = userRepository.findAll();
        List<Listing> listings = listingRepository.findAll();
        long buyers = users.stream().filter(user -> user.accountMode() != AccountMode.SELLER).count();
        long sellers = users.stream().filter(user -> user.accountMode() != AccountMode.BUYER).count();
        return new AdminOverview(buyers, sellers, listings.size(), 0, 0, 0);
    }

    public List<AdminUserView> users() {
        Map<String, Long> listingsBySeller = listingRepository.findAll().stream()
                .collect(Collectors.groupingBy(listing -> listing.sellerId().value(), Collectors.counting()));
        return userRepository.findAll().stream()
                .map(user -> new AdminUserView(
                        user.id().value(),
                        user.email(),
                        user.firstName(),
                        user.lastName(),
                        user.accountMode().name().toLowerCase(),
                        listingsBySeller.getOrDefault(user.id().value(), 0L),
                        user.createdAt()
                ))
                .toList();
    }
}
