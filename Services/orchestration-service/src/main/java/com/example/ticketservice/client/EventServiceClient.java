package com.example.ticketservice.client;

import com.example.ticketservice.config.ServiceUrlsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class EventServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceUrlsConfig serviceUrls;

    public Map<String, Object> getTipoEntrada(Long tipoEntradaId) {
        String url = serviceUrls.getEventService().getUrl() + "/api/tipos-entrada/" + tipoEntradaId;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getEvento(Long eventoId) {
        String url = serviceUrls.getEventService().getUrl() + "/api/eventos/" + eventoId;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public void decreaseCantidad(Long tipoEntradaId, Integer cantidad) {
        String url = serviceUrls.getEventService().getUrl() + "/api/tipos-entrada/" + tipoEntradaId + "/disminuir?cantidad=" + cantidad;
        restTemplate.put(url, null);
    }

    public void increaseCantidad(Long tipoEntradaId, Integer cantidad) {
        String url = serviceUrls.getEventService().getUrl() + "/api/tipos-entrada/" + tipoEntradaId + "/incrementar?cantidad=" + cantidad;
        restTemplate.put(url, null);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createEvento(Map<String, Object> eventoData) {
        String url = serviceUrls.getEventService().getUrl() + "/api/eventos";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, eventoData, Map.class);
        return response.getBody();
    }
}
