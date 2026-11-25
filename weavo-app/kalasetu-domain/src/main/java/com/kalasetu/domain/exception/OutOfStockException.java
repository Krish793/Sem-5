package com.kalasetu.domain.exception;

/** Thrown when requested quantity exceeds available artisan craft stock. */
public class OutOfStockException extends KalaSetuException {
    private final String productTitle;
    private final int availableQty;
    private final int requestedQty;

    public OutOfStockException(String productTitle, int availableQty, int requestedQty) {
        super(
            "OUT_OF_STOCK",
            409,
            String.format("Insufficient stock for '%s': requested %d, available %d.",
                productTitle, requestedQty, availableQty)
        );
        this.productTitle = productTitle;
        this.availableQty = availableQty;
        this.requestedQty = requestedQty;
    }

    public String getProductTitle() { return productTitle; }
    public int getAvailableQty() { return availableQty; }
    public int getRequestedQty() { return requestedQty; }
}
