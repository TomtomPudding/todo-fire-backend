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

func request(client pb.ProjectServiceClient) error {
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

    log.Printf("start")
    if err != nil {
        return errors.Wrap(err, "受取り失敗")
    }
    // log.Printf("サーバからの受け取り\n %d %s", reply.GetCode(), reply.GetMessage())
    log.Printf("サーバからの受け取り\nid: %s\nname %s\n", reply.GetId(), reply.GetName())
    return nil
}

func register() error {
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
    return request(client)
}

func main() {
    log.Printf("start")
    if err := register(); err != nil {
        log.Fatalf("%v", err)
    }
}
