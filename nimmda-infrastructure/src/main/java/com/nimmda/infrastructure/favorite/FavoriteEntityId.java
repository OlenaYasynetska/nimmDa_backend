package com.nimmda.infrastructure.favorite;

import java.io.Serializable;
import java.util.Objects;

public class FavoriteEntityId implements Serializable {

    private String userId;
    private String listingId;

    public FavoriteEntityId() {
    }

    public FavoriteEntityId(String userId, String listingId) {
        this.userId = userId;
        this.listingId = listingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FavoriteEntityId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(listingId, that.listingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, listingId);
    }
}
