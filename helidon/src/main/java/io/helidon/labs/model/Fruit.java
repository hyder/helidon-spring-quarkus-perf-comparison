package io.helidon.labs.model;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "fruits")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "fruit")
public class Fruit {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fruits_seq")
  @SequenceGenerator(
    name = "fruits_seq",
    sequenceName = "fruits_seq",
    allocationSize = 1
  )
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  private String description;

  @OneToMany(mappedBy = "fruit")
  @BatchSize(size = 32)
  @Cache(
    usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE,
    region = "fruitStorePrices"
  )
  private List<StoreFruitPrice> storePrices = new ArrayList<>();

  public Fruit() {}

  public Fruit(Long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<StoreFruitPrice> getStorePrices() {
    return storePrices;
  }

  public void setStorePrices(List<StoreFruitPrice> storePrices) {
    this.storePrices = storePrices;
  }
}
