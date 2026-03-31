# Variables
APP_NAME=coupon-api

.PHONY: help build up down test logs clean

help:
	@echo "Available Commands:"
	@echo "  make build  - Compiles the application and generates the Docker image"
	@echo "  make up     - Starts the application container"
	@echo "  make down   - Stops and removes the containers"
	@echo "  make test   - Runs the unit tests (JUnit 5)"
	@echo "  make logs   - Displays the logs in real time"
	@echo "  make clean  - Clears temporary Maven files"

build:
	docker-compose build --no-cache

up:
	docker-compose up -d

down:
	docker-compose down

test:
	mvn clean test jacoco:report

logs:
	docker logs -f $(APP_NAME)

clean:
	mvn clean