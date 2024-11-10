package store.controller;

import store.domain.BuyProduct;
import store.service.StoreService;
import store.view.InputView;
import store.view.OutputView;

public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    public void run() {
        initFileLoad();
        productBuyProcess();
    }

    private void initFileLoad() {
        storeService.createNewProducts();
        storeService.createNewPromotionProducts();
        storeService.createNewPromotions();
        storeService.readProductData();
    }

    private void productBuyProcess() {
        do {
            initProductBuyList();
            getBuyProducts();
            processBuyProducts();
            checkApplyMembership();
            printReceiptResult();
        } while (buyAgain());
    }

    private void initProductBuyList() {
        OutputView.printInit(storeService.getProducts(), storeService.getPromotionProducts());
        storeService.createNewReceipt();
    }

    private void getBuyProducts() {
        storeService.setBuyProducts(inputBuyProducts());
    }

    private void processBuyProducts() {
        for (BuyProduct buyProduct : storeService.getBuyProducts().buyProducts().values()) {
            if (isNowInPromotionDate(buyProduct) && storeService.checkPromotionProductStock(buyProduct.getName())) {
                processGetPromotionProduct(buyProduct);
                processGetNotPromotionProduct(buyProduct);
                continue;
            }
            storeService.buyProcessNotPromotion(buyProduct.getName());
        }
    }

    private boolean isNowInPromotionDate(BuyProduct buyProduct) {
        return storeService.getPromotionProducts().nowInPromotionDate(buyProduct.getName());
    }

    private void processGetPromotionProduct(BuyProduct buyProduct) {
        Long promotionQuantity = storeService.calculatePromotionOnlyProductQuantity(buyProduct);
        OutputView.printGetMoreProductsForPromotion(buyProduct.getName(), promotionQuantity);
        if (inputYesOrNo().equals("Y")) {
            storeService.buyPromotionForFree(storeService.getPromotionProducts().getProductByName(buyProduct.getName()), promotionQuantity);
            storeService.addReceiptPromotionProductQuantity(buyProduct.getName(), promotionQuantity);
        }
    }

    private void processGetNotPromotionProduct(BuyProduct buyProduct) {
        Long allSameProductQuantity = storeService.calculateSameProductStockQuantity(buyProduct);
        Long promotionIncludeProductQuantity = storeService.calculatePromotionOnlyProductQuantity(buyProduct);
        Long leftStock = allSameProductQuantity - promotionIncludeProductQuantity;

        OutputView.printPromotionOutOfStock(buyProduct.getName(), leftStock);
        if (inputYesOrNo().equals("Y")) {
            storeService.buyProcessNotPromotion(buyProduct.getName());
        }
    }

    private void checkApplyMembership() {
        OutputView.printMembershipDiscount();
        if (inputYesOrNo().equals("Y")) {
            storeService.setMembershipDiscount();
        }
    }

    private void printReceiptResult() {
        OutputView.printReceipt(storeService.getReceipt());
    }

    private boolean buyAgain() {
        OutputView.printBuyOneMoreTime();
        return inputYesOrNo().equals("Y");
    }

    private String inputBuyProducts() {
        while(true) {
            try {
                String response = InputView.inputBuyProducts();
                storeService.validateInputBuyProducts(response);
                return response;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String inputYesOrNo() {
        while(true) {
            try {
                String response = InputView.inputYesOrNo();
                storeService.validateInputYesOrNo(response);
                return response;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
