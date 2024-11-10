package store.dto;

import store.domain.BuyProduct;
import store.domain.Product;

import java.util.Map;

public record BuyProducts(Map<String, BuyProduct> buyProducts) {

    public void addBuyProductQuantity(Product product, Long quantity) {
        if (!buyProducts.containsKey(product.getName())) {
            buyProducts.put(product.getName(), new BuyProduct(product.getName(), 0L, product.getPrice()));
        }
        buyProducts.get(product.getName()).addQuantity(quantity);
    }
    public void reduceBuyProductQuantity(String productName, Long quantity) {
        buyProducts.get(productName).reduceQuantity(quantity);
    }
}
