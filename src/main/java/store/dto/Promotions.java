package store.dto;

import store.domain.Promotion;

import java.util.ArrayList;
import java.util.List;

public class Promotions {
    private final List<Promotion> promotions = new ArrayList<>();

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public void addPromotion(Promotion promotion) {
        promotions.add(promotion);
    }
}
