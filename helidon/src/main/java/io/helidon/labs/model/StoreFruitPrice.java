package io.helidon.labs.model;

import java.math.BigDecimal;
public class StoreFruitPrice {

  private StoreFruitPriceId id;

  private Store store;

  private Fruit fruit;

  private BigDecimal price;

  public StoreFruitPrice() {}

  public StoreFruitPrice(Store store, Fruit fruit, BigDecimal price) {
    this.store = store;
    this.fruit = fruit;
    this.price = price;
    this.id = new StoreFruitPriceId(store, fruit);
  }

  public StoreFruitPriceId getId() {
    return id;
  }

  public void setId(StoreFruitPriceId id) {
    this.id = id;
  }

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
    this.id = new StoreFruitPriceId(
      store != null ? store.getId() : null,
      id != null ? id.getFruitId() : null
    );
  }

  public Fruit getFruit() {
    return fruit;
  }

  public void setFruit(Fruit fruit) {
    this.fruit = fruit;
    this.id = new StoreFruitPriceId(
      id != null ? id.getStoreId() : null,
      fruit != null ? fruit.getId() : null
    );
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }
}
