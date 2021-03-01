package main

import (
	"context"
	"io"
	"log"
	"time"

	"github.com/pkg/errors"

	pb "grpc/pb/admin"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
)

func authStatus(client pb.AdminStatusServiceClient) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.Empty{}
	stream, err := client.AuthState(ctx, &request)
	if err != nil {
		return errors.Wrap(err, "streamエラー")
	}
	log.Printf("start auth status")
	for i := 1; i <= 3; i++ {
		reply, err := stream.Recv()

		if err == io.EOF {
			log.Println("EOF")
			break
		}

		if err != nil {
			return err
		}
		log.Println(i, "回目： status: ", reply.GetStatus())
	}

	return nil
}

func connect() (*grpc.ClientConn, pb.AdminStatusServiceClient, error) {
	address := "host.docker.internal:6565"
	conn, err := grpc.Dial(
		address,
		grpc.WithInsecure(),
	)
	if err != nil {
		return nil, nil, errors.Wrap(err, "コネクションエラー")
	}

	client := pb.NewAdminStatusServiceClient(conn)
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

	err = authStatus(client)
	if err != nil {
		log.Fatalf("%v", err)
	}
}
