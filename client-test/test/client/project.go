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
    code int32
    message string
}

func requestRegister(client pb.ProjectServiceClient) error {
    ctx, cancel := context.WithTimeout(
        context.Background(),
        time.Second * 100,
    )
    defer cancel()
    // loginRequest := pb.LogoutRequest{}
    // reply, err := client.Logout(ctx, &pb.Empty {})

    md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
    ctx = metadata.NewOutgoingContext(context.Background(), md)
    request := pb.ProjectRegisterRequest{
        Name: "テストプロジェクト",
    }
    reply, err := client.Register(ctx, &request)

    log.Printf("start project register")
    if err != nil {
        return errors.Wrap(err, "failed project register")
    }
    // log.Printf("サーバからの受け取り\n %d %s", reply.GetCode(), reply.GetMessage())
    log.Printf("サーバからの受け取り\nid: %s\nname %s\n", reply.GetId(), reply.GetName())
    return nil
}

func requestSearch(client pb.ProjectServiceClient) error {


    md := metadata.Pairs("Authorization", "bearer de25d818-be22-4481-805b-6ebb13064815")
    ctx := metadata.NewOutgoingContext(context.Background(), md)
    request := pb.ProjectSearchRequest{
        Id: "99999999998",
    }
    stream, err := client.Search(ctx, &request)
    if err != nil {
        return errors.Wrap(err, "streamエラー")
    }
    log.Printf("start project search")
    for i := 1; i <= 10; i++ {
        reply, err := stream.Recv()

        if err == io.EOF {
            log.Println("EOF")
            break
        }

        if err != nil {
            return err
        }
        log.Println(i, "回目： id ", reply.GetId(), "\nname ", reply.GetName())
        log.Println("groups ", reply.GetGroups(),"\ntype ", reply.GetType())
    }

    return nil
}

func request(executeReq func(pb.ProjectServiceClient) error) error {
    // address := "mainhost:6565"
    address := "host.docker.internal:6565"
    conn, err := grpc.Dial(
        address,
        grpc.WithInsecure(),
    )
    if err != nil {
        return errors.Wrap(err, "コネクションエラー")
    }
    defer conn.Close()

    client := pb.NewProjectServiceClient(conn)
    if err := executeReq(client); err != nil {
        return errors.Wrap(err, "実行エラー")
    }
    return nil
}

func main() {
    log.Printf("start")

    // err := request(requestRegister)
    // if err != nil {
    //     log.Fatalf("%v", err)
    // }
    // プロジェクトID検索
    err := request(requestSearch)
    if err != nil {
        log.Fatalf("%v", err)
    }
}
