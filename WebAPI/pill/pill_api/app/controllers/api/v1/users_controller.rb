class Api::V1::UsersController < ApplicationController
    #skip_before_action :authenticate_request, only: :create

    # GET /api/v1/users
    def index
        @users = current_user
        json_response(@users)
    end

    # GET /api/v1/user/:user_id
    def show
        json_response(@user)
    end

    # POST /api/v1/users
    def create
        user = User.create!(user_params)
        response = AuthenticateUser.new(user.username, user.password).call
        auth_token = response.result
        json_response({message: "User #{user.username} created!", token: response}, :created)
    end

    # PUT /api/v1/users
    # use json_response(@xx) for response
    def update
        current_user.update(user_params)
        json_response({message: "User updated!"})
    end

    # DELETE /api/v1/users/:id
    def destroy
        current_user.destroy
        json_response({message: "User deleted!"})
    end

    private

    def user_params
        params.permit(:password, :username)
    end
end