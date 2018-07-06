#!/bin/sh

set -eux

# create CA
openssl genrsa -out ca.key 4096
openssl req -new -x509 -days 9999 -subj "/C=US/ST=CA/L=Sunnyvale/O=Ollie/OU=RootCA" -key ca.key -out ca.crt

# create server key
openssl genrsa 4096 | openssl pkcs8 -topk8 -inform pem -outform pem -passout pass:ollie -out server.key
openssl req -new -key server.key -passin pass:ollie -subj "/C=US/ST=CA/L=Sunnyvale/O=Ollie/OU=Server/CN=localhost" -out server.csr
openssl x509 -req -days 9999 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt
cat server.crt server.key > server.pem

# create client key
openssl genrsa 4096 | openssl pkcs8 -topk8 -inform pem -outform pem -passout pass:ollie -out client.key
openssl req -new -key client.key -passin pass:ollie -subj "/C=US/ST=CA/L=Sunnyvale/O=Ollie/OU=Client/CN=testing" -out client.csr
openssl x509 -req -days 9999 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 02 -out client.crt
cat client.crt client.key > client.pem

# Cleanup intermediary files
rm -f ca.key
rm -f client.crt
rm -f client.csr
rm -f client.key
rm -f server.crt
rm -f server.csr
rm -f server.key
