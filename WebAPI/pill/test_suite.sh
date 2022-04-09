#!/bin/bash

# test suite
if [[ "$#" -ne 2 ]]; then
    echo "Please use like this"
    echo "Arg1: domain and port"
    echo "Arg2: protocol"
    echo "Ex: ./test_suite.sh 127.0.0.1:12122 http"
    exit 0
fi

# $1 is domain name and port
if [[ $2 -eq "http" ]]; then
    PROTO="http"
else
    PROTO="https"
fi

URL=$1

echo "-----------**--------------"

# Create user
TOKEN=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x_x","password":"#abcd"}' $PROTO://$URL/api/v1/signup | jq -r .token)
if [ "$TOKEN" = null ]; then
    echo "1 fail"
    exit
else
    echo "1 OK - User x created - Token: $TOKEN"
fi
echo "-----------**--------------"


check_user(){
    # Create user not valid
    ERR=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x_x","password":""}' $PROTO://$URL/api/v1/signup | jq -r .message)

    if [ "$ERR" = "User not created! An error occurred - Username may be taken" ]; then
        echo "2 OK - User not created, user already exists and no pw given"
    else
        echo "2 fail"
        exit
    fi

    echo "-----------**--------------"


    # Create user
    TOKEN2=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x2","password":"#abcd"}' $PROTO://$URL/api/v1/signup | jq -r .token)
    if [ "$TOKEN2" = null ]; then
        echo "2B fail"
        exit
    else
        echo "2B OK - User x2 created - Token: $TOKEN2"
    fi

    echo "-----------**--------------"

    # Login
    TOKEN=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x_x","password":"#abcd"}' $PROTO://$URL/api/v1/login | jq -r .token)
    if [ "$TOKEN" = null ]; then
        echo "3 fail"
        exit
    else
        echo "3 OK - Login as user x - Token: $TOKEN"
    fi

    echo "-----------**--------------"

    # Get my user
    USER=$(curl -s -H "Authorization: Bearer $TOKEN" $PROTO://$URL/api/v1/users | jq -r .username)
    if [ "$USER" = "x_x" ]; then
        echo "4 OK - Get my user - User: $USER"
    else
        echo "4 fail"
        exit
    fi

    echo "-----------**--------------"

    # Get myuser by ID
    USER=$(curl -s -H "Authorization: Bearer $TOKEN" $PROTO://$URL/api/v1/users/1 | jq -r .username)
    if [ "$USER" = "x_x" ]; then
        echo "5 OK - Get my user by ID - User: $USER"
    else
        echo "5 fail"
        exit
    fi

    echo "-----------**--------------"

    # Get myuser by ID
    USER=$(curl -s -H "Authorization: Bearer $TOKEN" $PROTO://$URL/api/v1/users/2 | jq -r .username)
    if [ "$USER" = "x_x" ]; then
        echo "5b fail"
        exit
    else
        echo "5b OK - Cannot get other user by ID - Msg: $USER"
    fi

    echo "-----------**--------------"

    # Update username
    USER=$(curl -s -XPUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN2" -d '{"username":"x_new","password":"#abcd"}' $PROTO://$URL/api/v1/users/2 | jq -r .message)
    if [ "$USER" = "User x_new updated!" ]; then
        echo "6 OK - Updated username for user x2 - User: $USER"
    else
        echo "6 fail"
        exit
    fi

    echo "-----------**--------------"

    # Update pw
    USER=$(curl -s -XPUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN2" -d '{"username":"x_new","password":"#abcd2"}' $PROTO://$URL/api/v1/users/2  | jq -r .message)
    if [ "$USER" = "User x_new updated!" ]; then
        echo "7 OK - Update pw for x2 (x_new) - User: $USER"
    else
        echo "7 fail"
        exit
    fi

    echo "-----------**--------------"

    # Update pw & username
    USER=$(curl -s -XPUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN2" -d '{"username":"x2","password":"#abcd"}' $PROTO://$URL/api/v1/users/2 | jq -r .message)
    if [ "$USER" = "User x2 updated!" ]; then
        echo "8 OK - Update pw and username for x2 - User: $USER"
    else
        echo "8 fail"
        exit
    fi

    echo "-----------**--------------"

    # Delete user
    USER=$(curl -s -XDELETE -H "Authorization: Bearer $TOKEN2" $PROTO://$URL/api/v1/users/2 | jq -r .message)
    if [ "$USER" = "User x2 deleted!" ]; then
        echo "9 OK - Deleted user x2 - User: $USER"
    else
        echo "9 fail"
        exit
    fi

    echo "-----------**--------------"

}
# ----------- USER DONE -----------------

check_days() {
    # Create day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/1/days" -d '{ "today": "2021-08-15" }'  | jq -r .today)
    if [ "$DAY" = "2021-08-15" ]; then
        echo "10 OK - Created Day: $DAY"
    else
        echo "10 fail"
        exit
    fi

    echo "-----------**--------------"

    # Show days
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/1/days" | jq -r .[0].today)
    if [ "$DAY" = "2021-08-15" ]; then
        echo "11 OK - Show all days - Day: $DAY"
    else
        echo "11 fail"
        exit
    fi

    echo "-----------**--------------"

    # Show day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "$PROTO://$URL/api/v1/users/1/days/1"| jq -r .today)
    if [ "$DAY" = "2021-08-15" ]; then
        echo "12 OK - Show day 1 - Day: $DAY"
    else
        echo "12 fail"
        exit
    fi

    echo "-----------**--------------"

    # Update day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XPUT "$PROTO://$URL/api/v1/users/1/days/1" -d '{ "today": "2021-06-06" }'  | jq -r .message)
    if [ "$DAY" = "Day 2021-06-06 updated!" ]; then
        echo "13 OK - Update day 2021-08-15 - Day: $DAY"
    else
        echo "13 fail"
        exit
    fi

    echo "-----------**--------------"

    # Create day
    A=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/1/days" -d '{ "today": "2021-08-15" }')
    # Delete day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XDELETE "$PROTO://$URL/api/v1/users/1/days/2" | jq -r .message)
    if [ "$DAY" = "Day 2021-08-15 deleted!" ]; then
        echo "14 OK - Delete newly created day - Day: $DAY"
    else
        echo "14 fail"
        exit
    fi

    echo "-----------**--------------"

}

# -------------- DAYS DONE --------------------
check_pills() {
    # Create pill
    PILL=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/2/pills" -d '{"color":"#000000", "active":true}' | jq -r .uuid)
    if [ "$PILL" = null ]; then
        echo "15 fail"
        exit
    else
        echo "15 OK - Create pill - Pill: $PILL"
    fi

    echo "-----------**--------------"

    UUID=$PILL

    # Get pills
    PILL=$(curl -s -H "Authorization: Bearer $TOKEN" "$PROTO://$URL/api/v1/users/2/pills" | jq -r .[0].uuid)
    if [ "$PILL" = null ]; then
        echo "16 fail"
        exit
    else
        echo "16 OK - Get pills - Pill: $PILL"
    fi

    echo "-----------**--------------"

    # Get pill
    PILL=$(curl -s -H "Authorization: Bearer $TOKEN" "$PROTO://$URL/api/v1/users/2/pills/1" | jq -r .uuid)
    if [ "$PILL" = null ]; then
        echo "17 fail"
        exit
    else
        echo "17 OK - Get pill 1 - Pill: $PILL"
    fi

    echo "-----------**--------------"

    # Update pill
    PILL=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XPUT "$PROTO://$URL/api/v1/users/2/pills/1" -d '{"color":"#999999", "active":false}' | jq -r .message)
    if [ "$PILL" = "Pill $UUID updated!" ]; then
        echo "18 OK - Update pill color to #999999 - Pill: $PILL"
    else
        echo "18 fail"
        exit
    fi

    echo "-----------**--------------"

    UUID2=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/2/pills" -d '{"color":"#111111", "active":true}' | jq -r .uuid )

    # Delete pill
    PILL=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XDELETE "$PROTO://$URL/api/v1/users/1/pills/2" | jq -r .message)
    if [ "$PILL" = "Pill $UUID2 deleted!" ]; then
        echo "19 OK - Delete newly created pill - Pill: $PILL"
    else
        echo "19 fail"
        exit
    fi

    echo "-----------**--------------"

}
# -------------- PILLS DONE --------------------
check_records() {

    # Create record 1
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/2/records" -d '{"day_id": 1, "taken":true, "pill_id": 1 }' | jq -r .id)
    if [ "$REC" = null ]; then
        echo "20 fail"
        exit
    else
        echo "20 OK - Create record w/ day_id and taken-prop - Record: $REC"
    fi

    echo "-----------**--------------"

    # Create record 2
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/2/records" -d '{"today": "2021-01-22", "pill_id": 1 }' | jq -r .id)
    if [ "$REC" = null ]; then
        echo "21 fail"
        exit
    else
        echo "21 OK - Create record w/today and no taken-prop - Record: $REC"
    fi

    echo "-----------**--------------"

    # Get records
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" "$PROTO://$URL/api/v1/users/2/records" | jq -r .[0].id)
    if [ "$REC" = null ]; then
        echo "22 fail"
        exit
    else
        echo "22 OK - Get all records - Record: $REC"
    fi

    echo "-----------**--------------"

    # Get record
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" "$PROTO://$URL/api/v1/users/2/records/1" | jq -r .id )
    if [ "REC" = null ]; then
        echo "23 fail"
        exit
    else
        echo "23 OK - Get record 1 - Record: $REC"
    fi

    echo "-----------**--------------"

    # Update record
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XPUT "$PROTO://$URL/api/v1/users/2/records/1" -d '{"day_id":3, "taken":false, "pill_id": 1 }' | jq -r .message)
    if [ "$REC" = "Record 2021-01-22 - x - $UUID updated!" ]; then
        echo "24 OK - Update record from day 1 to 2 - REC: $REC"
    else
        echo "24 fail"
        exit
    fi

    echo "-----------**--------------"

    # Update record
    # day_id: 3 is correct, but need a failure test for ID 2 that is supposed to fail
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XPUT "$PROTO://$URL/api/v1/users/2/records/1" -d '{"day_id":2, "taken":false, "pill_id": 1 }' | jq -r .message)
    if [ "$REC" = "Couldn't find Day with 'id'=2" ]; then
        echo "24b OK - Record not accepted due to day not existing!"
    else
        echo "24b fail - update accepted, but should not be"
        exit
    fi

    echo "-----------**--------------"

    # Delete record
    REC=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XDELETE "$PROTO://$URL/api/v1/users/1/records/2" | jq -r .message)
    if [ "$REC" = "Record for 2021-01-22 - x - $UUID deleted!" ]; then
        echo "25 OK - Delete record - Record: $REC"
    else
        echo "25 fail"
        exit
    fi

    echo "-----------**--------------"

}
# -------------- RECORDS DONE --------------------

check_user
check_days
check_pills
check_records

echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "All tests passed!!!"
