package com.example.urlshortener.service;

import com.example.urlshortener.entity.Url;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.exception.UrlShorteningException;
import com.example.urlshortener.repository.UrlRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    @Autowired
    private UrlRepository urlRepository;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_URL_LENGTH = 8;

    public String shortenUrl(String longUrl) {
        String decodedUrl = decodeUrl(longUrl);

        String shortUrl = generateShortUrl();
        while (urlRepository.findByShortUrl(shortUrl).isPresent()) {
            shortUrl = generateShortUrl();
        }

        if (shortUrl.endsWith("=")) {
            shortUrl = shortUrl.substring(0, shortUrl.length() - 1);
        }

        Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setLongUrl(decodedUrl);
        url.setCreatedAt(LocalDateTime.now());
        url.setExpiresAt(LocalDateTime.now().plusMonths(10));

        urlRepository.save(url);
        return shortUrl;
    }

    public Optional<Url> getLongUrl(String shortUrl) {
        Optional<Url> optionalUrl = urlRepository.findByShortUrl(shortUrl);
        logger.info("optionalUrl is not present");
        if (optionalUrl.isPresent()) {
            logger.info("optionalUrl is present");
            return optionalUrl;
        } else {
            throw new UrlNotFoundException("Short URL not found: " + shortUrl);
        }
    }

    public boolean updateShortUrl(String shortUrl, String newLongUrl) {
        Optional<Url> optionalUrl = urlRepository.findByShortUrl(shortUrl);
        if (optionalUrl.isPresent()) {
            Url url = optionalUrl.get();
            
            String actualLongUrl = extractLongUrl(newLongUrl);
            
            url.setLongUrl(actualLongUrl);
            urlRepository.save(url);
            return true;
        } else {
            throw new UrlNotFoundException("Short URL not found: " + shortUrl);
        }
    }

    private String extractLongUrl(String newLongUrl) {
        int startIndex = newLongUrl.indexOf("&") + 1;
        int endIndex = newLongUrl.lastIndexOf("=");
        return newLongUrl.substring(startIndex, endIndex);
    }

    public boolean updateExpiry(String shortUrl, int daysToAdd) {
        Optional<Url> optionalUrl = urlRepository.findByShortUrl(shortUrl);
        if (optionalUrl.isPresent()) {
            Url url = optionalUrl.get();
            url.setExpiresAt(url.getExpiresAt().plusDays(daysToAdd));
            urlRepository.save(url);
            return true;
        } else {
            throw new UrlNotFoundException("Short URL not found: " + shortUrl);
        }
    }

    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UrlShorteningException("Failed to decode URL");
        }
    }

    String generateShortUrl() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
