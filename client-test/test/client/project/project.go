package main

import (
	"context"
	"io"
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

func requestRegister(client pb.ProjectServiceClient) (string, error) {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ProjectRegisterRequest{
		Name: "テストプロジェクト",
	}
	reply, err := client.Register(ctx, &request)

	log.Printf("start project register")
	if err != nil {
		return "", errors.Wrap(err, "failed project register")
	}
	log.Printf("サーバからの受け取り\nid: %s\nname %s\n", reply.GetId(), reply.GetName())
	return reply.GetId(), nil
}

func requestUpdate(client pb.ProjectServiceClient, id string) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ProjectUpdateRequest{
		Id:   id,
		Name: "テストプロジェクト（修正後）",
	}
	reply, err := client.Update(ctx, &request)

	log.Printf("start project update")
	if err != nil {
		return errors.Wrap(err, "failed project update")
	}
	log.Printf("サーバからの受け取り\nid: %s\nname %s\n", reply.GetId(), reply.GetName())
	return nil
}

func requestDelete(client pb.ProjectServiceClient, id string) error {
	ctx, cancel := context.WithTimeout(
		context.Background(),
		time.Second*100,
	)
	defer cancel()

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx = metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ProjectDeleteRequest{
		Id: id,
	}
	_, err := client.Delete(ctx, &request)

	log.Printf("start project delete")
	if err != nil {
		return errors.Wrap(err, "failed project delete")
	}
	return nil
}

func requestSearch(client pb.ProjectServiceClient, id string) error {

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx := metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ProjectSearchRequest{
		Id: id,
	}
	stream, err := client.Search(ctx, &request)
	if err != nil {
		return errors.Wrap(err, "streamエラー")
	}
	log.Printf("start project search")
	for i := 1; i <= 3; i++ {
		reply, err := stream.Recv()

		if err == io.EOF {
			log.Println("EOF")
			break
		}

		if err != nil {
			return err
		}
		log.Println(i, "回目： id ", reply.GetId(), "\nname ", reply.GetName())
		log.Println("groups ", reply.GetGroups(), "\ntype ", reply.GetType())
	}

	return nil
}

func requestSearchAll(client pb.ProjectServiceClient) error {

	md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
	ctx := metadata.NewOutgoingContext(context.Background(), md)
	request := pb.ProjectSearchAllRequest{
		Type:   pb.ProjectType_COMMUNITY,
		Status: pb.ProjectStatus_OPEN,
		Color:  pb.Color_BLACK,
	}
	stream, err := client.SearchAll(ctx, &request)
	if err != nil {
		return errors.Wrap(err, "streamエラー")
	}
	log.Printf("start project search all")
	for i := 1; i <= 3; i++ {
		reply, err := stream.Recv()

		if err == io.EOF {
			log.Println("EOF")
			break
		}

		if err != nil {
			return err
		}
		for _, pro := range reply.GetProjects() {
			log.Println(i, "回目： id ", pro.GetId(), "\nname ", pro.GetName())
		}
	}

	return nil
}

func connect() (*grpc.ClientConn, pb.ProjectServiceClient, error) {
	address := "host.docker.internal:6565"
	conn, err := grpc.Dial(
		address,
		grpc.WithInsecure(),
	)
	if err != nil {
		return nil, nil, errors.Wrap(err, "コネクションエラー")
	}

	client := pb.NewProjectServiceClient(conn)
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

	// プロジェクト登録
	id, err := requestRegister(client)
	if err != nil {
		log.Fatalf("%v", err)
	}
	// プロジェクト更新
	err = requestUpdate(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
	// プロジェクトID検索
	err = requestSearch(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
	// プロジェクト複数検索
	err = requestSearchAll(client)
	if err != nil {
		log.Fatalf("%v", err)
	}
	// プロジェクト削除
	err = requestDelete(client, id)
	if err != nil {
		log.Fatalf("%v", err)
	}
}
