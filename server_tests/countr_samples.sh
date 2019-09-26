#!/bin/bash

COUNTR_HOME=$(git rev-parse --show-toplevel)
COUNTR_ID_DIR=$COUNTR_HOME/example_face_from_countr/ExampleDataFaceRecognition/ID_Images
COUNTR_PERSON_DIR=$COUNTR_HOME/example_face_from_countr/ExampleDataFaceRecognition/Persons

function test_persons(){
    for person in $(ls $COUNTR_PERSON_DIR); do
        person_dir=$COUNTR_PERSON_DIR/$person
        echo "Starting scoring of $person"
        echo ""
        curl --silent "http://0.0.0.0:5000/clear_score" > /dev/null
        for photo in $(ls $person_dir);do
            # echo $person_dir/$photo
            curl --silent "http://0.0.0.0:5000/image_raw_mtcnn?name=$person_dir/$photo" > /dev/null
        done
        curl "http://0.0.0.0:5000/get_score"
        break
    done
}

function load_yale(){
    for yale_face in $(ls $COUNTR_HOME/yalefaces); do
        curl "http://0.0.0.0:5000/add_to_db?name=yalefaces/$yale_face"
        echo ""
    done
}
function load_countr(){
    for countr_face in $(ls $COUNTR_HOME/example_face_from_countr/ExampleDataFaceRecognition/ID_Images); do
        echo "Trying face $countr_face: /n"
        curl "http://0.0.0.0:5000/add_to_db?name=$COUNTR_ID_DIR/$countr_face"
        echo ""
    done
}
function load_db(){
    load_count
    load_countr
}

function dump_db(){
    curl 0.0.0.0:5000/dump_db 
}

function main(){
    # Load db
    load_db
    # Dump Db For future debugging
    dump_db
    test_persons
}
main
