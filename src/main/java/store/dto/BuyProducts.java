package store.dto;

import store.domain.BuyProduct;

import java.util.Map;

public record BuyProducts(Map<String, BuyProduct> buyProducts) {
}
