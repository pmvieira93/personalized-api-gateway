# 'bash -c help' to learn more
SHELL := /bin/bash -c

# https://stackoverflow.com/questions/10858261/how-to-abort-makefile-if-variable-not-set
SERVICE_NAME:=
# agw = api gateway
PROJECT=-p agw

up:
	docker-compose $(PROJECT) up --build -d $(if $(SERVICE_NAME),$(SERVICE_NAME))
	sleep 5
	newman run fga-initializer.postman_collection.json --export-globals ./target/generated-ids.json
	jq -r '.values | map("export \(.key|sub("-";"_"; "g")|ascii_upcase)=\(.value|tostring)")|.[]' ./target/generated-ids.json > ./target/.spring-env

down:
	docker-compose $(PROJECT) down $(if $(SERVICE_NAME),$(SERVICE_NAME))
