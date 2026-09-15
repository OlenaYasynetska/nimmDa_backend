package com.nimmda.web.place;

import com.nimmda.application.place.PlaceView;
import com.nimmda.application.place.ResolvePlaceService;
import com.nimmda.domain.place.Place;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final ResolvePlaceService resolvePlaceService;

    public LocationController(ResolvePlaceService resolvePlaceService) {
        this.resolvePlaceService = resolvePlaceService;
    }

    @GetMapping
    public List<PlaceView> suggest(@RequestParam(required = false) String q) {
        return resolvePlaceService.suggest(q, 12).stream()
                .map(LocationController::toView)
                .toList();
    }

    private static PlaceView toView(Place place) {
        return new PlaceView(place.name(), place.region(), place.postalCode(), place.displayName());
    }
}
