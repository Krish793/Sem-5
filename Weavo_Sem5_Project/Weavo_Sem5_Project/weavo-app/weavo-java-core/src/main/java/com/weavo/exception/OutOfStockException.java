package com.weavo.exception;

/**
 * Custom Exception thrown when an artisan product's stock is insufficient
 * during concurrent checkout.
 */
public class OutOfStockException extends WeavoException {
    private final String productId;
    private final int requestedQty;
    private final int availableQty;

    public OutOfStockException(String productId, int requestedQty, int availableQty) {
        super(String.format("Product '%s' is out of stock. Requested: %d, Available: %d", 
                productId, requestedQty, availableQty), "ERR_OUT_OF_STOCK");
        this.productId = productId;
        this.requestedQty = requestedQty;
        this.availableQty = availableQty;
    }

    public String getProductId() { return productId; }
    public int getRequestedQty() { return requestedQty; }
    public int getAvailableQty() { return availableQty; }
}
