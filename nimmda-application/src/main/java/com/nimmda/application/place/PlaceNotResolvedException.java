package com.nimmda.application.place;

import java.util.List;

public class PlaceNotResolvedException extends RuntimeException {

    private final List<String> suggestions;

    public PlaceNotResolvedException(List<String> suggestions) {
        super("Ort konnte nicht gefunden werden. Bitte überprüfe deine Eingabe.");
        this.suggestions = suggestions == null ? List.of() : List.copyOf(suggestions);
    }

    public List<String> suggestions() {
        return suggestions;
    }

    public String code() {
        return "placeNotFound";
    }
}
