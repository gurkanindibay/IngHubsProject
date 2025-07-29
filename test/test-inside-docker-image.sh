# Build the Ubuntu 24 image
docker build -f Dockerfile -t wallet-service-ubuntu24 .

# Run with your project mounted
docker run -it --name wallet-test -v "c:\source\Tryouts\IngWalletService:/app" -p 8080:8080 -p 8443:8443 wallet-service-ubuntu24