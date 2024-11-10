package store.dto;

import store.domain.Product;

import java.util.LinkedHashMap;
import java.util.Map;

public class Products {
    private final Map<String, Product> products = new LinkedHashMap<>();

    public Map<String, Product> getProducts() {
        return products;
    }

    public void addProduct(Product product) {
        getProducts().put(product.getName(), product);
    }

    public Product getProductByName(String productName) {
        return getProducts().get(productName);
    }

    public Long getProductPrice(String productName) {
        return getProducts().get(productName).getPrice();
    }

    public Long getProductQuantity(String productName) {
        return getProducts().get(productName).getStock();
    }
}
