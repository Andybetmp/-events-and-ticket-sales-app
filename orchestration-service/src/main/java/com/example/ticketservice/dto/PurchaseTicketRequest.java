package com.example.ticketservice.dto;

public class PurchaseTicketRequest {
    private Long tipoEntradaId;
    private Integer cantidad;
    private PaymentMethodDto paymentMethod;

    public static class PaymentMethodDto {
        private String cardNumber;
        private String cvv;
        private String expiryDate;
        private String cardHolder;

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

    public Long getTipoEntradaId() {
        return tipoEntradaId;
    }

    public void setTipoEntradaId(Long tipoEntradaId) {
        this.tipoEntradaId = tipoEntradaId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public PaymentMethodDto getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodDto paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
