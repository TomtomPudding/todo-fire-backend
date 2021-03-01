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

func requestRegister(client pb.ToDoServiceClient) (string, error) {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ToDoRegisterRequest{
		ProjectId: "99999999999",
		GroupId:   "000000001",
		Title:     "テストTODO Title",
		Content:   "テストTODO Content",
	}
	reply, err := client.Register(ctx, &request)

	log.Printf("\n\nstart todo register")
	if err != nil {
		return "", errors.Wrap(err, "failed todo register")
	}
	log.Printf("サーバからの受け取り\nprojectId: %s\ngroupId: %s\ntodoId: %s", reply.GetProjectId(), reply.GetGroupId(), reply.GetTodoId())
	log.Printf("title: %s\ncontent: %s\n\n", reply.GetTitle(), reply.GetContent())
	return reply.GetTodoId(), nil
}

func requestUpdate(client pb.ToDoServiceClient, id string) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ToDoUpdateRequest{
		ProjectId:   "99999999999",
		TodoId:      id,
		MoveGroupId: "000000002",
		Title:       "テストTODO Title（更新）",
		Content:     "テストTODO Content（更新）",
	}
	reply, err := client.Update(ctx, &request)

	log.Printf("\n\nstart todo update")
	if err != nil {
		return errors.Wrap(err, "failed todo update")
	}
	log.Printf("サーバからの受け取り\nprojectId: %s\ngroupId: %s\ntodoId: %s", reply.GetProjectId(), reply.GetGroupId(), reply.GetTodoId())
	log.Printf("title: %s\ncontent: %s\n\n", reply.GetTitle(), reply.GetContent())
	return nil
}

func requestDelete(client pb.ToDoServiceClient, id string) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ToDoDeleteRequest{
		ProjectId: "99999999999",
		TodoId:    id,
	}
	_, err := client.Delete(ctx, &request)

	log.Printf("\n\nstart todo delete")
	if err != nil {
		return errors.Wrap(err, "failed todo delete")
	}
	return nil
}

func connect() (*grpc.ClientConn, pb.ToDoServiceClient, error) {
	address := "host.docker.internal:6565"
	conn, err := grpc.Dial(
		address,
		grpc.WithInsecure(),
	)
	if err != nil {
		return nil, nil, errors.Wrap(err, "コネクションエラー")
	}

	client := pb.NewToDoServiceClient(conn)
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

	// ToDo 登録
	id, err := requestRegister(client)
	if err != nil {
		log.Fatalf(id, "%v", err)
	}
	log.Print("id: ", id)
	// ToDo 更新
	err = requestUpdate(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
	// ToDo 削除
	err = requestDelete(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
}
