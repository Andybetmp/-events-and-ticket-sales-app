package com.example.notificationservice.dto;

import java.util.Map;

public class NotificationRequest {
    private String tipo; // BIENVENIDA, EVENTO_CREADO, TICKET_COMPRADO, PAGO_RECHAZADO
    private String destinatario; // email
    private Map<String, Object> datos;

    public NotificationRequest() {
    }

    public NotificationRequest(String tipo, String destinatario, Map<String, Object> datos) {
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.datos = datos;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public void setDatos(Map<String, Object> datos) {
        this.datos = datos;
    }
}
