package store.controller;

import store.domain.BuyProduct;
import store.domain.Product;
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
                continue;
            }
            storeService.buyProcessNotPromotion(buyProduct.getName());
        }
    }

    private boolean isNowInPromotionDate(BuyProduct buyProduct) {
        return storeService.getPromotionProducts().nowInPromotionDate(buyProduct.getName());
    }

    private void processGetPromotionProduct(BuyProduct buyProduct) {
        // 전체 프로모션 물건 재고 갯수
        Long promotionQuantity = storeService.getPromotionProductQuantity(buyProduct.getName());
        // 손님이 사야할 물건 갯수
        Long toBuyProductQuantity = buyProduct.getQuantity();
        // 프로모션 적용되는 물건 갯수
        Long promotionOnlyQuantity = storeService.calculatePromotionOnlyProductQuantity(buyProduct);
        // 프로모션이 적용되기 위한 최소 물건 갯수
        Long promoteNeedQuantity = storeService.getPromoteNeedQuantity(buyProduct.getName());
        // 프로모션이 적용되면 받는 물건 갯수
        Long promotePlusQuantity = storeService.getPromotePlusQuantity(buyProduct.getName());
        // 사야할 물건 상세 정보
        Product productInfoToBuy = storeService.getPromoteProductDetail(buyProduct.getName());
        // 프로모션이 적용되는 사는 물량 전체 총합 (프로모션 갯수 * (n+1))
        Long promotionOnlyBuyQuantity = promotionOnlyQuantity * (promoteNeedQuantity + promotePlusQuantity);
        // 전체 살 물건 중에 프로모션 적용이 안되고 생짜로 사는 물량
        Long notPromotedToBuyProduct = toBuyProductQuantity - promotionOnlyBuyQuantity;


        if (canPickExtraQuantity(promotionQuantity, toBuyProductQuantity)) {
            if (canAddExtraQuantity(toBuyProductQuantity, promoteNeedQuantity, promotePlusQuantity)) {
                OutputView.printGetMoreProductsForPromotion(buyProduct.getName(), promotePlusQuantity);
                if (inputYesOrNo().equals("Y")) {
                    storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, toBuyProductQuantity);
                    if (promotionOnlyQuantity >= 1) {
                        storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
                        storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), toBuyProductQuantity);
                        storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(toBuyProductQuantity);
                    }
                    calculateProcess2(buyProduct, productInfoToBuy, promotePlusQuantity, promotePlusQuantity);
                    return;
                }
                calculateProcess2(buyProduct, productInfoToBuy, toBuyProductQuantity, promotionOnlyQuantity);
                return;
            }
            calculateProcess(buyProduct, productInfoToBuy, promotionOnlyBuyQuantity, promotionOnlyQuantity);
            return;
        }

        if (promotionQuantity == toBuyProductQuantity) {
            if (toBuyProductQuantity % (promoteNeedQuantity + promotePlusQuantity) == 0) {
                calculateProcess2(buyProduct, productInfoToBuy, toBuyProductQuantity, promotionOnlyQuantity);
                return;
            }
            OutputView.printPromotionOutOfStock(buyProduct.getName(), notPromotedToBuyProduct);
            if (inputYesOrNo().equals("Y")) {
                calculateProcess(buyProduct, productInfoToBuy, promotionOnlyBuyQuantity, promotionOnlyQuantity);
            }
            return;
        }

        if (promotionQuantity < toBuyProductQuantity) {
            OutputView.printPromotionOutOfStock(buyProduct.getName(), notPromotedToBuyProduct);
            if (inputYesOrNo().equals("Y")) {
                calculateProcess(buyProduct, productInfoToBuy, promotionOnlyBuyQuantity, promotionOnlyQuantity);
            }
        }
    }

    private void calculateProcess(BuyProduct buyProduct, Product productInfoToBuy, Long promotionOnlyBuyQuantity, Long promotionOnlyQuantity) {
        // 정가로 일단 전부 결제
        calculateProcess2(buyProduct, productInfoToBuy, promotionOnlyBuyQuantity, promotionOnlyQuantity);
        // 나머지 재고 털이
        storeService.buyProcessNotPromotion(buyProduct.getName());
    }

    private void calculateProcess2(BuyProduct buyProduct, Product productInfoToBuy, Long toBuyProductQuantity, Long promotionOnlyQuantity) {
        // 정가로 일단 전부 결제
        storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, toBuyProductQuantity);
        // 영수증 promotion 할인 기재
        storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
        // 재고 감소
        storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), toBuyProductQuantity);
        storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(toBuyProductQuantity);
    }

    private static boolean canAddExtraQuantity(Long toBuyProductQuantity, Long promoteNeedQuantity, Long promotePlusQuantity) {
        return toBuyProductQuantity % (promoteNeedQuantity + promotePlusQuantity) == promoteNeedQuantity;
    }

    private static boolean canPickExtraQuantity(Long promotionQuantity, Long toBuyProductQuantity) {
        return promotionQuantity > toBuyProductQuantity;
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
