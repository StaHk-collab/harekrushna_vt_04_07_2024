# URL Shortner

Backend : Java, Spring boot

Database used : PostgreSQL

## APIs implemented :

1. POST: Shorten Url (Destination Url) → Short Url, id → Success/ Failure
```
curl --location 'http://localhost:8080/api/v1/shorten' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode '{long_url_to_shorten}'
```
2. POST: Update short url (Short Url, Destination Url) → Boolean
```
curl --location 'http://localhost:8080/api/v1/update?shortUrl={short_url_to_update}' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode '{new_long_url}'
```
3. GET: Get Destination Url (Short Url) → Destination Url
```
curl --location 'http://localhost:8080/{short_url}'
```
4. POST: Update Expiry (Short Url, Days to add in expiry) → Boolean
```
curl --location --request POST 'http://localhost:8080/api/v1/update-expiry?shortUrl={enter_short_url}&daysToAdd={number_of_days}'
```
