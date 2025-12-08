package com.example.paymentservice.dto;

public class PaymentRequest {
    private String idempotencyKey; // NEW: UUID to prevent duplicate payments
    private Double monto;
    private String cardNumber;
    private String cvv;
    private String expiryDate;
    private String cardHolder;

    public PaymentRequest() {
    }

    public PaymentRequest(String idempotencyKey, Double monto, String cardNumber, String cvv, String expiryDate, String cardHolder) {
        this.idempotencyKey = idempotencyKey;
        this.monto = monto;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.cardHolder = cardHolder;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }
}
