#!/bin/bash
# This script runs on first PostgreSQL container startup.
# The default database (coresto_db) is created by POSTGRES_DB env var.
# Here we create the additional database for Keycloak.

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE keycloak_db;
    GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO $POSTGRES_USER;
EOSQL
