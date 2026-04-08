package io.helidon.labs.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import io.helidon.json.binding.Json;

@Embeddable
@Json.Entity
public class StoreFruitPriceId implements Serializable {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "fruit_id", nullable = false)
    private Long fruitId;

    public StoreFruitPriceId() {
    }

    public StoreFruitPriceId(Long storeId, Long fruitId) {
        this.storeId = storeId;
        this.fruitId = fruitId;
    }

    public StoreFruitPriceId(Store store, Fruit fruit) {
        this(store != null ? store.getId() : null, fruit != null ? fruit.getId() : null);
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getFruitId() {
        return fruitId;
    }

    public void setFruitId(Long fruitId) {
        this.fruitId = fruitId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StoreFruitPriceId that)) {
            return false;
        }
        return Objects.equals(storeId, that.storeId) && Objects.equals(fruitId, that.fruitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, fruitId);
    }
}
