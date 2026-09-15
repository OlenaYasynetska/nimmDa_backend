package com.nimmda.application.port.geo;

import java.util.List;

public interface Geocoder {

    List<GeocodedPlace> search(String query);
}
