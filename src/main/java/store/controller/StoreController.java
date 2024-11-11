package store.controller;

import store.domain.BuyProduct;
import store.domain.Product;
import store.service.StoreService;
import store.validator.ValidatorMessage;
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

            // 프로모션은 1+1, 2+1 한정
            // 2+1인데 7개 있고, 10개 원하는 경우 -> 4+2 + 4
            // 2+1인데 6개 있고, 7개 원하는 경우 -> 4+2 + 1
            // 2+1인데 7개 있고, 5개 원하는 경우 -> 2+1 + 2 + 보너스 1
            // 사는 것이 기준. 프로모션 포함하여 샀는데, 깎아준다는 느낌?

            // 프로모션 기간이고, 프로모션 재고가 보너스를 줄 수 있는 재고만큼 남은 경우
            if (isNowInPromotionDate(buyProduct) && storeService.checkPromotionProductStock(buyProduct.getName())) {
                // 프로모션 털어내기
                processGetPromotionProduct(buyProduct);
                continue;
            }

            // 나머지 재고들에 대해 털어내기
            storeService.buyProcessNotPromotion(buyProduct.getName());
        }
    }

    private boolean isNowInPromotionDate(BuyProduct buyProduct) {
        return storeService.getPromotionProducts().nowInPromotionDate(buyProduct.getName());
    }







    private void processGetPromotionProduct(BuyProduct buyProduct) {
        // 전체 프로모션 물건 재고 갯수
        Long promotionQuantity = storeService.getPromotionProducts().getPromotionProductStockQuantity(buyProduct.getName());
        // 손님이 가져온 물건 갯수
        Long toBuyProductQuantity = buyProduct.getQuantity();
        // 프로모션 적용되는 물건 갯수
        Long promotionOnlyQuantity = storeService.calculatePromotionOnlyProductQuantity(buyProduct);

        // 프로모션이 적용되기 위한 최소 물건 갯수
        Long promoteNeedQuantity = storeService.getPromotionProducts().getProductByName(buyProduct.getName()).getPromotion().promoteQuantity();

        // 프로모션이 적용되면 받는 물건 갯수
        Long promotePlusQuantity = storeService.getPromotionProducts().getProductByName(buyProduct.getName()).getPromotion().promotePlus();

        // 사야할 물건 상세 정보
        Product productInfoToBuy = storeService.getPromotionProducts().getProductByName(buyProduct.getName());

        // 프로모션이 적용되는 사는 물량 전체 총합
        Long promotionOnlyBuyQuantity = promotionOnlyQuantity * (promoteNeedQuantity + promotePlusQuantity);

        Long notPromotedToBuyProduct = toBuyProductQuantity - promotionOnlyBuyQuantity;


        // 추가 물건을 넣을 수 있다면, 넣는다.
        // 2+1 :: 7 / 3 = 2 * 3 = 6 -> 1개가 더 들어갔다. 1개를 더 사야 보너스를 주기 때문에 불가능
        // 1+1 :: 7 / 2 = 3 * 2 = 6 -> 1개가 더 들어갔다. 1개 더 보너스로 받는 것이 가능
        if (promotionQuantity > toBuyProductQuantity) {
            // 만약 초과해서 샀는데, 딱 +1만 부족해서 더 줄 수 있다면? 알림을 준다.
            if (toBuyProductQuantity % (promoteNeedQuantity + promotePlusQuantity) == promoteNeedQuantity) {
                OutputView.printGetMoreProductsForPromotion(buyProduct.getName(), promotePlusQuantity);
                if (inputYesOrNo().equals("Y")) {
                    // 정가로 일단 전부 결제
                    storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, toBuyProductQuantity);
                    if (promotionOnlyQuantity >= 1) {
                        // 영수증 promotion 할인 기재
                        storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
                        // 재고 감소
                        storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), toBuyProductQuantity);
                        storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(toBuyProductQuantity);
                    }



                    // 구매 하나 더 추가
                    storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, promotePlusQuantity);
                    // 영수증 promotion 할인 갯수 +1
                    storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotePlusQuantity);
                    // 재고 감소 -1
                    storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), promotePlusQuantity);
                    storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(promotePlusQuantity);

//                    storeService.buyPromotionForFree(productInfoToBuy, promotionQuantity);
//                    storeService.addReceiptPromotionProductQuantity(buyProduct.getName(), promotionQuantity);

                    return;
                }

                // 1개 추가 하지 않는 케이스
                // 정가로 일단 전부 결제
                storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, toBuyProductQuantity);

                // 영수증 promotion 할인 기재
                storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
                // 재고 감소
                storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), toBuyProductQuantity);
                storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(toBuyProductQuantity);

                return;
            }

            // 만약 1을 더 못 주는 경우,
            // 2+1 :: 7 / 3 = 2 * 3 = 6 -> 1개가 더 들어갔다. 1개를 더 사야 보너스를 주기 때문에 불가능
            // 정가로 일단 전부 결제
            storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, promotionOnlyBuyQuantity);

            // 영수증 promotion 할인 기재
            storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
            // 재고 감소
            storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), promotionOnlyBuyQuantity);
            storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(promotionOnlyBuyQuantity);

            // 나머지 재고 털이
            storeService.buyProcessNotPromotion(buyProduct.getName());
        }

        // 물어보지 않고 프로모션 할인 로직 진행
        if (promotionQuantity == toBuyProductQuantity) {
            if (toBuyProductQuantity % (promoteNeedQuantity + promotePlusQuantity) == 0) {
                // 정가로 일단 전부 결제
                storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, toBuyProductQuantity);
                // 영수증 promotion 할인 기재
                storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
                // 재고 감소
                storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), toBuyProductQuantity);
                storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(toBuyProductQuantity);
                return;
            }

            // 정가로 일단 전부 결제
            storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, promotionOnlyBuyQuantity);

            // 영수증 promotion 할인 기재
            storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
            // 재고 감소
            storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), promotionOnlyBuyQuantity);
            storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(promotionOnlyBuyQuantity);

            // 나머지 재고 털이
            storeService.buyProcessNotPromotion(buyProduct.getName());
            return;
        }

        // 프로모션 적용 불가 메시지 출력
        if (promotionQuantity < toBuyProductQuantity) {
            Long leftStock = storeService.calculateSameProductStockQuantity(buyProduct);

            OutputView.printPromotionOutOfStock(buyProduct.getName(), notPromotedToBuyProduct);
            if (inputYesOrNo().equals("Y")) {
                // 정가로 일단 전부 결제
                storeService.getReceipt().addBuyProductQuantity(productInfoToBuy, promotionOnlyBuyQuantity);

                // 영수증 promotion 할인 기재
                storeService.getReceipt().addPromotionProductQuantity(productInfoToBuy, promotionOnlyQuantity);
                // 재고 감소
                storeService.getPromotionProducts().reducePromotionQuantity(buyProduct.getName(), promotionOnlyBuyQuantity);
                storeService.getBuyProducts().buyProducts().get(buyProduct.getName()).reduceQuantity(promotionOnlyBuyQuantity);

                // 나머지 재고 털이
                storeService.buyProcessNotPromotion(buyProduct.getName());
            }
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
                if (response.contains(" ")) {
                    throw new IllegalArgumentException(ValidatorMessage.WRONG_BUY_FORMAT.getErrorMessage());
                }
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
