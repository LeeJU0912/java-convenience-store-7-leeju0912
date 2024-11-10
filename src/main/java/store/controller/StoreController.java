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
        // 재고 물건 파일 로드 (최초 1회)
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
            // 재고 물건 목록
            initProductBuyList();
            // 구매 희망 물건 파싱
            getBuyProducts();
            // 모든 구매 희망 물건에 대해 정산
            processBuyProducts();
            // 멤버십 할인 적용 유무 확인
            checkApplyMembership();
            // 최종 계산 결과 출력
            printReceiptResult();
            // 재구매 확인 출력
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
            if (isNowInPromotionDate(buyProduct)) {
                if (storeService.checkPromotionProductStock(buyProduct.getName())) {
                    // 프로모션 공짜 물건 추가 유무 확인 후 구매 처리
                    processGetPromotionProduct(buyProduct);

                    // 프로모션 없는 재고 확인
                    Long allSameProductQuantity = storeService.calculateSameProductStockQuantity(buyProduct);
                    Long promotionIncludeProductQuantity = storeService.calculatePromotionOnlyProductQuantity(buyProduct);
                    // 프로모션 적용 안되는 재고만 추출
                    Long leftStock = allSameProductQuantity - promotionIncludeProductQuantity;

                    // 프로모션 적용 안되는 재고에 대해 살지 말지 확인 후 구매 처리
                    OutputView.printPromotionOutOfStock(buyProduct.getName(), leftStock);
                    // 구매 로직 - 먼저 프로모션을 빼고, 일반 재고를 뺀다
                    if (inputYesOrNo().equals("Y")) {
                        storeService.buyProcessNotPromotion(buyProduct.getName());
                    }
                    continue;
                }
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
            storeService.buyPromotionForFree(buyProduct.getName(), promotionQuantity);
            storeService.getReceipt().addPromotionProductQuantity(buyProduct.getName(), promotionQuantity);
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
