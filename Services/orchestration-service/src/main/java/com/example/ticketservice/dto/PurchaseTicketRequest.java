package com.example.ticketservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class PurchaseTicketRequest {
    private String idempotencyKey; // NEW: For idempotent payments
    
    @NotNull(message = "El tipo de entrada es requerido")
    private Long tipoEntradaId;
    
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 10, message = "No se pueden comprar más de 10 tickets")
    private Integer cantidad;
    
    @NotNull(message = "El método de pago es requerido")
    @Valid
    private PaymentMethodDto paymentMethod;

    public static class PaymentMethodDto {
        @NotBlank(message = "El número de tarjeta es requerido")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Número de tarjeta inválido")
        private String cardNumber;
        
        @NotBlank(message = "El CVV es requerido")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV debe tener 3 o 4 dígitos")
        private String cvv;
        
        @NotBlank(message = "La fecha de expiración es requerida")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Formato de fecha inválido (MM/YY)")
        private String expiryDate;
        
        @NotBlank(message = "El titular de la tarjeta es requerido")
        @Size(min = 3, max = 100, message = "El nombre del titular debe tener entre 3 y 100 caracteres")
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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
