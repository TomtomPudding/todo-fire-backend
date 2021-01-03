FROM golang:1.13-stretch

SHELL ["/bin/bash", "-c"]
RUN apt update && apt-get install -y vim unzip

# install protc
WORKDIR /protoc
RUN wget https://github.com/protocolbuffers/protobuf/releases/download/v3.11.2/protoc-3.11.2-linux-x86_64.zip
RUN unzip protoc-3.11.2-linux-x86_64.zip
RUN ln -s /protoc/bin/protoc /bin/protoc

# golang
WORKDIR /go-grpc
ENV GO111MODULE on
RUN go get -u github.com/golang/protobuf/protoc-gen-go
RUN go mod init grpc-sample

## CMD protoc --proto_path ./proto  --go_out=plugins=grpc:./pb/grpc_firebase_admin  ./proto/firebase-admin.proto
