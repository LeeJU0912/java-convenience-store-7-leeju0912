package store.dto;

import camp.nextstep.edu.missionutils.DateTimes;
import store.domain.BuyProduct;
import store.domain.Product;

import java.time.LocalDateTime;

public class PromotionProducts extends Products {

    public boolean nowInPromotionDate(String productName) {
        if (getProductByName(productName) == null) {
            return false;
        }
        if (checkPromotionDateTime(productName)) return false;
        return true;
    }

    private boolean checkPromotionDateTime(String productName) {
        LocalDateTime now = DateTimes.now();
        Product promotionProduct = getProducts().get(productName);
        if (promotionProduct.getPromotion().promoteEndDate().isBefore(now)
                || promotionProduct.getPromotion().promoteStartDate().isAfter(now)) {
            return true;
        }
        return false;
    }

    public Long getPromotionValidQuantity(String productName) {
        return getProducts().get(productName).getPromotion().promoteQuantity();
    }

    public Long getPromotionProductStockQuantity(String productName) {
        return getProducts().get(productName).getStock();
    }

    public Long getPromotionOnlyProductQuantity(BuyProduct buyProduct) {
        Product product = getProducts().get(buyProduct.getName());
        double result = (double) Math.min(product.getStock(), buyProduct.getQuantity())
                / (product.getPromotion().promoteQuantity() + product.getPromotion().promotePlus());
        return Math.round(result);
    }

    public void reducePromotionQuantity(String productName, Long quantity) {
        getProducts().get(productName).reduceStock(quantity);
    }
}
