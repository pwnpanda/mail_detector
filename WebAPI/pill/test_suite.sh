#!/bin/bash

# test suite

# $1 is domain name and port
if [[ $2 -eq "http" ]]; then
    PROTO="http"
else
    PROTO="https"
fi

URL=$1

# Create user
TOKEN=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x","password":"#abcd"}' $PROTO://$URL/api/v1/signup | jq -r .token)
if [ "$TOKEN" = null ]; then
    echo "1 fail"
    exit
else
    echo "1 OK - Token: $TOKEN"
fi


check_user(){
    # Create user not valid
    ERR=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x","password":""}' $PROTO://$URL/api/v1/signup | jq -r .message)

    if [ "$ERR" = "User not created! An error occurred - Username may be taken" ]; then
        echo "2 OK"
    else
        echo "2 fail"
        exit
    fi

    # Create user
    TOKEN2=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x2","password":"#abcd"}' $PROTO://$URL/api/v1/signup | jq -r .token)
    if [ "$TOKEN2" = null ]; then
        echo "2B fail"
        exit
    else
        echo "2B OK - Token: $TOKEN2"
    fi

    # Login
    TOKEN=$(curl -s -H "Content-Type: application/json" -POST -d '{"username":"x","password":"#abcd"}' $PROTO://$URL/api/v1/login | jq -r .token)
    if [ "$TOKEN" = null ]; then
        echo "3 fail"
        exit
    else
        echo "3 OK - Token: $TOKEN"
    fi

    # Get my user
    USER=$(curl -s -H "Authorization: Bearer $TOKEN" $PROTO://$URL/api/v1/users | jq -r .username)
    if [ "$USER" = "x" ]; then
        echo "4 OK - User: $USER"
    else
        echo "4 fail"
        exit
    fi

    # Get myuser by ID
    USER=$(curl -s -H "Authorization: Bearer $TOKEN" $PROTO://$URL/api/v1/users/1 | jq -r .username)
    if [ "$USER" = "x" ]; then
        echo "5 OK - User: $USER"
    else
        echo "5 fail"
        exit
    fi

    # Update username
    USER=$(curl -s -XPUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN2" -d '{"username":"x_new","password":"#abcd"}' $PROTO://$URL/api/v1/users/2 | jq -r .message)
    if [ "$USER" = "User x_new updated!" ]; then
        echo "6 OK - User: $USER"
    else
        echo "6 fail"
        exit
    fi

    # Update pw
    USER=$(curl -s -XPUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN2" -d '{"username":"x_new","password":"#abcd2"}' $PROTO://$URL/api/v1/users/2  | jq -r .message)
    if [ "$USER" = "User x_new updated!" ]; then
        echo "7 OK - User: $USER"
    else
        echo "7 fail"
        exit
    fi

    # Update pw & username
    USER=$(curl -s -XPUT -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN2" -d '{"username":"x2","password":"#abcd"}' $PROTO://$URL/api/v1/users/2 | jq -r .message)
    if [ "$USER" = "User x2 updated!" ]; then
        echo "8 OK - User: $USER"
    else
        echo "8 fail"
        exit
    fi

    # Update pw & username
    USER=$(curl -s -XDELETE -H "Authorization: Bearer $TOKEN2" $PROTO://$URL/api/v1/users/2 | jq -r .message)
    if [ "$USER" = "User x2 deleted!" ]; then
        echo "9 OK - User: $USER"
    else
        echo "9 fail"
        exit
    fi
}
# ----------- USER DONE -----------------

check_days() {
    # Create day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/1/days" -d '{ "today": "2021-08-15" }'  | jq -r .today)
    if [ "$DAY" = "2021-08-15" ]; then
        echo "10 OK - Day: $DAY"
    else
        echo "10 fail"
        exit
    fi

    # Show days
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/1/days" | jq -r .[0].today)
    if [ "$DAY" = "2021-08-15" ]; then
        echo "11 OK - Day: $DAY"
    else
        echo "11 fail"
        exit
    fi

    # Show day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "$PROTO://$URL/api/v1/users/1/days/1"| jq -r .today)
    if [ "$DAY" = "2021-08-15" ]; then
        echo "12 OK - Day: $DAY"
    else
        echo "12 fail"
        exit
    fi

    # Update day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XPUT "$PROTO://$URL/api/v1/users/1/days/1" -d '{ "today": "2021-06-06" }'  | jq -r .message)
    if [ "$DAY" = "Day 2021-06-06 updated!" ]; then
        echo "13 OK - Day: $DAY"
    else
        echo "13 fail"
        exit
    fi

    # Create day
    A=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -POST "$PROTO://$URL/api/v1/users/1/days" -d '{ "today": "2021-08-15" }')
    # Delete day
    DAY=$(curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -XDELETE "$PROTO://$URL/api/v1/users/1/days/2" | jq -r .message)
    if [ "$DAY" = "Day 2021-08-15 deleted!" ]; then
        echo "14 OK - Day: $DAY"
    else
        echo "14 fail"
        exit
    fi
}

# -------------- DAYS DONE --------------------
check_pills() {
    pass
}
# -------------- PILLS DONE --------------------
check_records() {
    pass
}
# -------------- RECORDS DONE --------------------

#check_user
#check_days
#check_pills
#check_records