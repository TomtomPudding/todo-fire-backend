package main

import (
    "context"
    "log"
    "time"

    "github.com/pkg/errors"

    pb "grpc-sample/pb/grpc_firebase_admin"

    "google.golang.org/grpc"
)

func request(client pb.FirebaseAdminServiceClient) error {
    ctx, cancel := context.WithTimeout(
        context.Background(),
        time.Second,
    )
    defer cancel()
//     loginRequest := pb.LogoutRequest{}
    reply, err := client.Logout(ctx, &pb.Empty {})

    log.Printf("start")
    if err != nil {
        return errors.Wrap(err, "受取り失敗")
    }
    log.Printf("サーバからの受け取り\n %d %s", reply.GetCode(), reply.GetMessage())
    return nil
}

func login() error {
    address := "mainhost:6565"
    conn, err := grpc.Dial(
        address,
        grpc.WithInsecure(),
    )
    if err != nil {
        return errors.Wrap(err, "コネクションエラー")
    }
    defer conn.Close()
    client := pb.NewFirebaseAdminServiceClient(conn)
    return request(client)
}

func main() {
    log.Printf("start")
    if err := login(); err != nil {
        log.Fatalf("%v", err)
    }
}