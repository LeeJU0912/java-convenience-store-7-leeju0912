package store.domain;

public class BuyProduct {
    private final String name;
    private Long quantity;
    private Long price;

    public BuyProduct(String name, Long quantity, Long price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long calculatePriceSum() {
        return quantity * price;
    }

    public void reduceQuantity(Long quantity) {
        this.quantity -= quantity;
    }

    public void addQuantity(Long quantity) {
        this.quantity += quantity;
    }
}
