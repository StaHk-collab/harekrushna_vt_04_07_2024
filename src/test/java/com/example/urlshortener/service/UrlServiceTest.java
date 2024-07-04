package com.example.urlshortener.service;

import com.example.urlshortener.entity.Url;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShortenUrl() {
        String longUrl = "http://example.com";
        String shortUrl = "abcdef";

        when(urlRepository.findByShortUrl(anyString())).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Url url = invocation.getArgument(0);
            url.setShortUrl(shortUrl);
            return null;
        }).when(urlRepository).save(any(Url.class));

        // Mock the generateShortUrl method to return the expected short URL
        UrlService spyUrlService = spy(urlService);
        doReturn(shortUrl).when(spyUrlService).generateShortUrl();

        String result = spyUrlService.shortenUrl(longUrl);
        assertNotNull(result);
        assertEquals(shortUrl, result);
    }

    @Test
    public void testGetLongUrl() {
        String shortUrl = "abcdef";
        String longUrl = "http://example.com";
        Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setLongUrl(longUrl);

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(url));

        Optional<Url> result = urlService.getLongUrl(shortUrl);
        assertTrue(result.isPresent());
        assertEquals(longUrl, result.get().getLongUrl());
    }

    @Test
    public void testGetLongUrl_NotFound() {
        String shortUrl = "abcdef";

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> {
            urlService.getLongUrl(shortUrl);
        });
    }

    @Test
    public void testUpdateShortUrl() {
        String shortUrl = "abcdef";
        String newLongUrl = "http://newexample.com";
        Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setLongUrl("http://oldexample.com");

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(url));

        boolean result = urlService.updateShortUrl(shortUrl, newLongUrl);
        assertTrue(result);
        assertEquals(newLongUrl, url.getLongUrl());
        verify(urlRepository, times(1)).save(url);
    }

    @Test
    public void testUpdateShortUrl_NotFound() {
        String shortUrl = "abcdef";
        String newLongUrl = "http://newexample.com";

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> {
            urlService.updateShortUrl(shortUrl, newLongUrl);
        });
    }

    @Test
    public void testUpdateExpiry() {
        String shortUrl = "abcdef";
        int daysToAdd = 30;
        Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setExpiresAt(LocalDateTime.now());
    
        // Capture the original expiry date
        LocalDateTime originalExpiryDate = url.getExpiresAt();
    
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(url));
    
        boolean result = urlService.updateExpiry(shortUrl, daysToAdd);
        assertTrue(result);
    
        // Compare the new expiry date with the original plus daysToAdd
        assertEquals(originalExpiryDate.plusDays(daysToAdd), url.getExpiresAt());
        verify(urlRepository, times(1)).save(url);
    }

    @Test
    public void testUpdateExpiry_NotFound() {
        String shortUrl = "abcdef";
        int daysToAdd = 30;

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> {
            urlService.updateExpiry(shortUrl, daysToAdd);
        });
    }
}
