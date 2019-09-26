#!/bin/bash

COUNTR_HOME=$(git rev-parse --show-toplevel)
COUNTR_ID_DIR=$COUNTR_HOME/example_face_from_countr/ExampleDataFaceRecognition/ID_Images
COUNTR_PERSON_DIR=$COUNTR_HOME/example_face_from_countr/ExampleDataFaceRecognition/Persons

function test_persons(){
    for person in $(ls $COUNTR_PERSON_DIR); do
        person_dir=$COUNTR_PERSON_DIR/$person
        echo ""
        echo "Starting scoring of $person"
        curl --silent "http://0.0.0.0:5000/clear_score" > /dev/null
        for photo in $(ls $person_dir);do
            # echo $person_dir/$photo
            curl --silent "http://0.0.0.0:5000/image_raw_mtcnn?name=$person_dir/$photo" > /dev/null
        done
        curl "http://0.0.0.0:5000/get_score"
        echo ""
    done
}

function load_yale(){
    echo "Loading yalefaces DB for noise:"
    for yale_face in $(ls $COUNTR_HOME/yalefaces); do
        curl --silent "http://0.0.0.0:5000/add_to_db?name=yalefaces/$yale_face" > /dev/null
    done
    echo ""
}

function load_countr(){
    echo "Loading countr faces DB:"
    for countr_face in $(ls $COUNTR_HOME/example_face_from_countr/ExampleDataFaceRecognition/ID_Images); do
        curl --silent "http://0.0.0.0:5000/add_to_db?name=$COUNTR_ID_DIR/$countr_face" > /dev/null
    done
    echo ""
}

function load_db(){
    load_yale
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
