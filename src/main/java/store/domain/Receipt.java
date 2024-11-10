package store.domain;

import store.dto.BuyProducts;
import store.dto.PromotionProducts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Receipt {
    private final BuyProducts buyProducts;
    private final PromotionProducts promotionProducts;
    private boolean membershipDiscount;

    public Receipt() {
        this.buyProducts = new BuyProducts(new ConcurrentHashMap<>());
        this.promotionProducts = new PromotionProducts();
        this.membershipDiscount = false;
    }

    public void setMembershipDiscount() {
        this.membershipDiscount = true;
    }

    public void addBuyProduct(Product product) {
        buyProducts.buyProducts().put(product.getName(), new BuyProduct(product.getName(), 0L, product.getPrice()));
    }

    public BuyProducts getBuyProducts() {
        return buyProducts;
    }

    public PromotionProducts getPromotionProducts() {
        return promotionProducts;
    }

    public void addBuyProductQuantity(Product product, Long quantity) {
        Map<String, BuyProduct> buyProducts = getBuyProducts().buyProducts();
        if (!buyProducts.containsKey(product.getName())) {
            addBuyProduct(product);
        }
        buyProducts.get(product.getName()).addQuantity(quantity);
    }

    public void addPromotionProductQuantity(Product product, Long quantity) {
        promotionProducts.addPromotionQuantity(product, quantity);
    }

    public Long calculateTotalQuantity() {
        return buyProducts.buyProducts().values().stream().mapToLong(BuyProduct::getQuantity).sum();
    }

    public Long calculateTotal() {
        return buyProducts.buyProducts().values().stream().mapToLong(BuyProduct::calculatePriceSum).sum();
    }

    public Long calculatePromotionTotal() {
        return promotionProducts.getProducts().values().stream().mapToLong(Product::getPriceSum).sum();
    }

    public Long calculateMembershipDiscount() {
        if (membershipDiscount) {
            return min(8000L, calculateTotal() * 70 / 100);
        }
        return 0L;
    }

    public Long calculateOverallCost() {
        if (membershipDiscount) {
            return max(0L, (calculateMembershipDiscount() - calculatePromotionTotal()));
        }
        return max(0L, (calculateTotal() - calculatePromotionTotal()));
    }
}
