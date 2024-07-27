package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"strings"

	"github.com/hudhud/traffic/github.com/hudhud/tomtom"
	"github.com/jackc/pgx/v5"
	"google.golang.org/protobuf/proto"
)

func main() {
	b, err := os.ReadFile("ff-late.proto")
	if err != nil {
		log.Fatal(err)
	}
	message := tomtom.TrafficFlowGroup{}
	err = proto.Unmarshal(b, &message)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println(len(message.TrafficFlowWithPredictionPerSection))
	conn, err := pgx.Connect(context.Background(), "postgres://postgres:postgres@localhost:5434/postgres")
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close(context.Background())
	err = conn.Ping(context.Background())
	if err != nil {
		log.Fatal(err)
	}
	// m := map[string]bool{}
	errors := 0
	succ := 0
	eFile, err := os.Create("errors.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer eFile.Close()
	sFile, err := os.Create("success.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer sFile.Close()

	for i, flow := range message.TrafficFlowWithPredictionPerSection {
		fmt.Println("Index:", i)
		// fmt.Println(flow)
		// fmt.Println(SectionToSQLArray(*flow.Location))
		var res string
		err = conn.QueryRow(
			context.Background(),
			"SELECT ST_AsText(construct_route($1))",
			SectionToSQLArray(*flow.Location),
		).Scan(&res)
		if err != nil {
			var sss []string
			for _, j := range SectionToSQLArray(*flow.Location) {
				sss = append(sss, fmt.Sprintf("ARRAY[%d,%d,%d,%d]", j[0], j[1], j[2], j[3]))
			}
			// log.Fatal(err)
			eFile.WriteString(fmt.Sprintf("%d | Select(construct_route(ARRAY[%s]));\n", i, strings.Join(sss, ",")))
			errors++
		} else {
			var sss []string
			for _, j := range SectionToSQLArray(*flow.Location) {
				sss = append(sss, fmt.Sprintf("ARRAY[%d,%d,%d,%d]", j[0], j[1], j[2], j[3]))
			}
			succ++
			sFile.WriteString(fmt.Sprintf("%d | %s, ARRAY[%s]\n", i, res, strings.Join(sss, ",")))
		}
	}
	fmt.Println("Errors:", errors)
	fmt.Println("Success:", succ)
	//	Errors: 5910
	//
	// Success: 118337
	// 	Errors: 2621
	// Success: 121626
}

func SectionToSQLArray(location tomtom.Location) [][]uint32 {
	res := [][]uint32{}
	for _, id := range location.OsmIds.Ids {
		res = append(res, []uint32{id.GetOsmId(), id.GetStart(), id.GetEnd(), id.GetReversed()})
	}
	return res
}
