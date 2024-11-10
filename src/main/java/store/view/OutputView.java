package store.view;

import store.domain.Product;
import store.dto.BuyProducts;
import store.dto.Products;
import store.domain.Receipt;
import store.dto.PromotionProducts;

import java.text.DecimalFormat;

public class OutputView {
    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    public static void printInit(Products products, Products promotionProducts) {
        System.out.println("""
                안녕하세요. W편의점입니다.
                현재 보유하고 있는 상품입니다.

                """);
        printProductStockList(products, promotionProducts);
        System.out.println("\n" + "구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");
    }

    private static void printProductStockList(Products products, Products promotionProducts) {
        for (Product product : products.getProducts().values()) {
            System.out.println("- " + product.getName() + " " + formatter.format(product.getPrice()) + "원 " + filterZero(product.getStock()));
        }
        for (Product product : promotionProducts.getProducts().values()) {
            System.out.println("- " + product.getName() + " " + formatter.format(product.getPrice()) + "원 " + filterZero(product.getStock()) + " " + product.getPromotionName());
        }
    }

    private static String filterZero(Long stock) {
        if (stock == 0) {
            return "재고 없음";
        }
        return formatter.format(stock) + "개";
    }

    public static void printReceipt(Receipt receipt) {
        System.out.println("===========W 편의점=============\n" + "상품명\t\t수량\t금액");
        printReceiptBuyProducts(receipt);
        System.out.println("===========증\t정=============");
        printReceiptPromotionProducts(receipt);
        System.out.println("==============================");
        printReceiptCalculatedValues(receipt);
    }

    private static void printReceiptBuyProducts(Receipt receipt) {
        BuyProducts buyProducts = receipt.getBuyProducts();
        buyProducts.buyProducts().values().forEach(buyProduct -> System.out.println(buyProduct.getName() + "\t\t" + buyProduct.getQuantity() + " \t" + buyProduct.calculatePriceSum()));
    }

    private static void printReceiptPromotionProducts(Receipt receipt) {
        PromotionProducts promotionProducts = receipt.getPromotionProducts();
        promotionProducts.getProducts().forEach((k, v) -> System.out.println(k + "\t\t" + v));
    }

    private static void printReceiptCalculatedValues(Receipt receipt) {
        System.out.println("총구매액\t\t" + formatter.format(receipt.calculateTotalQuantity()) + "\t" + formatter.format((receipt.calculateTotal())));
        System.out.println("행사할인\t\t\t" + filterReceiptZero(receipt.calculatePromotionTotal()));
        System.out.println("멤버십할인\t\t\t" + filterReceiptZero(receipt.calculateMembershipDiscount()));
        System.out.println("내실돈\t\t\t " + formatter.format(receipt.calculateOverallCost()));
    }

    private static String filterReceiptZero(Long quantity) {
        if (quantity == 0L) {
            return "0";
        }
        return "-" + formatter.format(quantity);
    }

    public static void printGetMoreProductsForPromotion(String productName, Long promotion) {
        System.out.println("현재 " + productName + "은(는) "+ promotion + "개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)");
    }

    public static void printPromotionOutOfStock(String productName, Long quantity) {
        System.out.println("현재 " + productName + " " + quantity + "개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)");
    }

    public static void printMembershipDiscount() {
        System.out.println("멤버십 할인을 받으시겠습니까? (Y/N)");
    }

    public static void printBuyOneMoreTime() {
        System.out.println("감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)");
    }
}
