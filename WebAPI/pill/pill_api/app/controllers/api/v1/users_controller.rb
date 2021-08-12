class Api::V1::UsersController < ApplicationController
    skip_before_action :authenticate_request, only: :create
    before_action :set_user, only: [:show, :update, :destroy]

    # GET /api/v1/users
    def index
        @users = User.all
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
        json_response({message: "User #{user.username} created!", token: auth_token}, :created)
    end

    # PUT /api/v1/users
    # use json_response(@xx) for response
    def update
        @user.update(user_params)
        json_response({message: "User updated!"})
    end

    # DELETE /api/v1/users/:id
    def destroy
        @user.destroy
        json_response({message: "User deleted!"})
    end

    private

    def user_params
        params.permit(:password, :username)
    end

    def set_user
        @user = User.find(params[:id])
    end
end