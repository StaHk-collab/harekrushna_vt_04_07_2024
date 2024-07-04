package com.example.urlshortener.controller;

import com.example.urlshortener.entity.Url;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.exception.UrlShorteningException;
import com.example.urlshortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

public class UrlControllerTest {

    @Mock
    private UrlService urlService;

    @InjectMocks
    private UrlController urlController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShortenUrl() {
        String longUrl = "http://example.com";
        String shortUrl = "abcdef";

        when(urlService.shortenUrl(longUrl)).thenReturn(shortUrl);

        ResponseEntity<String> response = urlController.shortenUrl(longUrl);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost:8080/" + shortUrl, response.getBody());
    }

    @Test
    public void testShortenUrl_Exception() {
        String longUrl = "http://example.com";

        when(urlService.shortenUrl(longUrl)).thenThrow(new UrlShorteningException("Failed to shorten the URL"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            urlController.shortenUrl(longUrl);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Failed to shorten the URL", exception.getReason());
    }

    @Test
    public void testUpdateShortUrl() {
        String shortUrl = "abcdef";
        String newLongUrl = "http://newexample.com";

        when(urlService.updateShortUrl(shortUrl, newLongUrl)).thenReturn(true);

        ResponseEntity<Boolean> response = urlController.updateShortUrl(shortUrl, newLongUrl);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
    }

    @Test
    public void testUpdateShortUrl_NotFound() {
        String shortUrl = "abcdef";
        String newLongUrl = "http://newexample.com";

        when(urlService.updateShortUrl(shortUrl, newLongUrl)).thenThrow(new UrlNotFoundException("Short URL not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            urlController.updateShortUrl(shortUrl, newLongUrl);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Short URL not found", exception.getReason());
    }

    @Test
    public void testRedirectToFullUrl() throws Exception {
        String shortUrl = "abcdef";
        String longUrl = "http://example.com";
        Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setLongUrl(longUrl);

        when(urlService.getLongUrl(shortUrl)).thenReturn(Optional.of(url));

        HttpServletResponse response = mock(HttpServletResponse.class);

        urlController.redirectToFullUrl(response, shortUrl);

        verify(response).sendRedirect(longUrl);
    }

    @Test
    public void testRedirectToFullUrl_NotFound() throws Exception {
        String shortUrl = "abcdef";

        when(urlService.getLongUrl(shortUrl)).thenThrow(new UrlNotFoundException("Url not found"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            urlController.redirectToFullUrl(response, shortUrl);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Short URL not found", exception.getReason());
    }

    @Test
    public void testUpdateExpiry() {
        String shortUrl = "abcdef";
        int daysToAdd = 30;

        when(urlService.updateExpiry(shortUrl, daysToAdd)).thenReturn(true);

        ResponseEntity<Boolean> response = urlController.updateExpiry(shortUrl, daysToAdd);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
    }

    @Test
    public void testUpdateExpiry_NotFound() {
        String shortUrl = "abcdef";
        int daysToAdd = 30;

        when(urlService.updateExpiry(shortUrl, daysToAdd)).thenThrow(new UrlNotFoundException("Short URL not found"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            urlController.updateExpiry(shortUrl, daysToAdd);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Short URL not found", exception.getReason());
    }
}