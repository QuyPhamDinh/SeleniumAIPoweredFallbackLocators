# SeleniumAIPoweredFallbackLocators
docker-compose up -d selenium-hub chrome firefox
docker build --platform linux/arm64 -t selenium_ai .
docker-compose run --rm test-runner

