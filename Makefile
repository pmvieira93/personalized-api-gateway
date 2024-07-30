SHELL := /bin/bash

# https://stackoverflow.com/questions/10858261/how-to-abort-makefile-if-variable-not-set
SERVICE_NAME:=

up:
	docker compose up --build -d $(if $(SERVICE_NAME),$(SERVICE_NAME))

down:
	docker compose down $(if $(SERVICE_NAME),$(SERVICE_NAME))
