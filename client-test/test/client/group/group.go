package main

import (
	"context"
	"log"
	"time"

	"github.com/pkg/errors"

	pb "grpc/pb/client"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
)

type ErrorReply struct {
	code    int32
	message string
}

func requestRegister(client pb.GroupServiceClient) (string, error) {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.GroupRegisterRequest{
		ProjectId: "99999999999",
		Name:      "テストグループ Name",
		Content:   "テストグループ Content",
		Position:  4,
	}
	reply, err := client.Register(ctx, &request)

	log.Printf("\n\nstart group register")
	if err != nil {
		return "", errors.Wrap(err, "failed group register")
	}
	log.Printf("サーバからの受け取り\nprojectId: %s\ngroupId: %s", reply.GetProjectId(), reply.GetGroupId())
	log.Printf("name: %s\ncontent: %s\n\n", reply.GetName(), reply.GetContent())
	return reply.GetGroupId(), nil
}

func requestUpdate(client pb.GroupServiceClient, id string) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.GroupUpdateRequest{
		ProjectId: "99999999999",
		GroupId:   id,
		Name:      "テストグループ Name（更新）",
		Content:   "テストグループ Content（更新）",
		Position:  3,
	}
	reply, err := client.Update(ctx, &request)

	log.Printf("\n\nstart group update")
	if err != nil {
		return errors.Wrap(err, "failed group update")
	}
	log.Printf("サーバからの受け取り\nprojectId: %s\ngroupId: %s", reply.GetProjectId(), reply.GetGroupId())
	log.Printf("name: %s\ncontent: %s\n\n", reply.GetName(), reply.GetContent())
	return nil
}

func requestDelete(client pb.GroupServiceClient, id string) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.GroupDeleteRequest{
		ProjectId: "99999999999",
		GroupId:   id,
	}
	_, err := client.Delete(ctx, &request)

	log.Printf("\n\nstart group delete")
	if err != nil {
		return errors.Wrap(err, "failed group delete")
	}
	return nil
}

func connect() (*grpc.ClientConn, pb.GroupServiceClient, error) {
	address := "host.docker.internal:6565"
	conn, err := grpc.Dial(
		address,
		grpc.WithInsecure(),
	)
	if err != nil {
		return nil, nil, errors.Wrap(err, "コネクションエラー")
	}

	client := pb.NewGroupServiceClient(conn)
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

	// グループ登録
	id, err := requestRegister(client)
	if err != nil {
		log.Fatalf(id, "%v", err)
	}
	log.Print("id: ", id)
	// グループ更新
	err = requestUpdate(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
	// グループ削除
	err = requestDelete(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
}
