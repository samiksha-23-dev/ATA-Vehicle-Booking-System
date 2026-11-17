package com.ata.booking.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        logger.error("Feign call failed: {} - status: {}", methodKey, response.status());
        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException("Requested resource not found in remote service");
            case 503 -> new BookingException("Admin service is currently unavailable. Please try again later.");
            default -> new BookingException("Error communicating with admin service (status: " + response.status() + ")");
        };
    }
}
