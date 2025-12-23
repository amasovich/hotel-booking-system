package ru.mifi.booking.bookingservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.mifi.booking.bookingservice.client.dto.ConfirmAvailabilityRequest;
import ru.mifi.booking.common.exception.ConflictException;
import ru.mifi.booking.common.exception.NotFoundException;
import ru.mifi.booking.common.exception.UnauthorizedException;
import ru.mifi.booking.common.http.RequestHeaders;

/**
 * Клиент для вызова hotel-service (internal endpoints).
 *
 * <p>
 * Важно: эти endpoints НЕ публикуются через API Gateway,
 * поэтому booking-service обращается напрямую на baseUrl.
 * </p>
 */
@Service
public class HotelServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HotelServiceClient(
            RestTemplate restTemplate,
            @Value("${services.hotel-service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void confirmAvailability(Long roomId, ConfirmAvailabilityRequest req, String serviceJwt, String requestId) {
        String url = baseUrl + "/api/rooms/" + roomId + "/confirm-availability";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceJwt);
        if (requestId != null && !requestId.isBlank()) {
            headers.set(RequestHeaders.X_REQUEST_ID, requestId);
        }

        HttpEntity<ConfirmAvailabilityRequest> entity = new HttpEntity<>(req, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpClientErrorException ex) {
            mapAndThrow(ex);
        }
    }

    public void release(Long roomId, String bookingId, String serviceJwt, String requestId) {
        String url = baseUrl + "/api/rooms/" + roomId + "/release?bookingId=" + bookingId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceJwt);
        if (requestId != null && !requestId.isBlank()) {
            headers.set(RequestHeaders.X_REQUEST_ID, requestId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpClientErrorException ex) {
            mapAndThrow(ex);
        }
    }

    private void mapAndThrow(HttpClientErrorException ex) {
        HttpStatusCode code = ex.getStatusCode();

        if (code.value() == 401 || code.value() == 403) {
            throw new UnauthorizedException("Hotel service rejected service token");
        }
        if (code.value() == 404) {
            throw new NotFoundException("Hotel service resource not found");
        }
        if (code.value() == 409) {
            throw new ConflictException(ex.getResponseBodyAsString() == null || ex.getResponseBodyAsString().isBlank()
                    ? "Hotel service conflict"
                    : ex.getResponseBodyAsString());
        }

        throw ex;
    }
}
