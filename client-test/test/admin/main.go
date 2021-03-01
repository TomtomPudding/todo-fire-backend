package main

import (
	"context"
	"log"
	"time"

	"github.com/pkg/errors"

	pb "grpc/pb/admin"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
)

func login(client pb.AdminServiceClient) (string, error) {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer test_token")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	loginRequest := pb.LoginRequest{
		Uid:      "90000001",
		Email:    "tomcat@gjkfljdsa.com",
		Password: "V8dW482gFigp",
	}
	reply, err := client.Login(ctx, &loginRequest)

	log.Printf("start")
	if err != nil {
		return "", errors.Wrap(err, "受取り失敗")
	}
	log.Printf("サーバからの受け取り\nuid: %s\nemail %s\ntoken: %s", reply.GetUid(), reply.GetEmail(), reply.GetToken())
	return reply.GetToken(), nil
}

func loginGuest(client pb.AdminServiceClient) (string, error) {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	request := pb.Empty{}
	reply, err := client.LoginGuest(ctx, &request)

	log.Printf("start")
	if err != nil {
		return "", errors.Wrap(err, "受取り失敗")
	}
	log.Printf("サーバからの受け取り\nuid: %s\nemail %s\ntoken: %s", reply.GetUid(), reply.GetEmail(), reply.GetToken())
	return reply.GetToken(), nil
}

func connect() (*grpc.ClientConn, pb.AdminServiceClient, error) {
	address := "host.docker.internal:6565"
	conn, err := grpc.Dial(
		address,
		grpc.WithInsecure(),
	)
	if err != nil {
		return nil, nil, errors.Wrap(err, "コネクションエラー")
	}

	client := pb.NewAdminServiceClient(conn)
	if err != nil {
		return nil, nil, errors.Wrap(err, "実行エラー")
	}
	return conn, client, nil
}

func main() {
	log.Printf("start")

	conn, client, err := connect()
	if err != nil {
		log.Fatalf("%v", err)
		return
	}
	defer conn.Close()

	_, err = loginGuest(client)
	if err != nil {
		log.Fatalf("%v", err)
	}
}
