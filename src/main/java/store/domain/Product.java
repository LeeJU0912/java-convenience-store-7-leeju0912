package store.domain;

public class Product {

    private final String name;
    private final Long price;
    private Long stock;
    private String promotionName;

    private Promotion promotion;

    public Product(String name, Long price, Long stock, String promotionName) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.promotionName = promotionName;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public Long getStock() {
        return stock;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public Long getPriceSum() {
        return price * stock;
    }

    public void updateStock(Long stock) {
        this.stock += stock;
    }

    public void reduceStock(Long stock) {
        this.stock -= stock;
    }
}
