class Api::V1::UsersController < ApplicationController
    skip_before_action :authenticate_request, only: :create

    # GET /api/v1/users
    def index
        @users = current_user
        # Remove the password hash for security
        @users.password_digest = "REDACTED"
        json_response(@users)
    end

    # GET /api/v1/user/:user_id
    def show
        if current_user.id == params[:id].to_i
            @user = current_user
            @user.password_digest = "REDACTED"
            json_response(current_user)
        else
            json_response( {message: "User not found!"}, :not_found)
        end
    end

    # POST /api/v1/users
    def create
        begin
            user = User.create!(user_params)
            response = AuthenticateUser.new(user.username, user.password).call
            auth_token = response.result
            user.password_digest = "REDACTED"
            json_response({id: user.id, username: user.username, token: auth_token}, :created)
        rescue ActiveRecord::RecordNotUnique
            json_response( {message: "User not created! An error occurred - Username may be taken"}, :bad_request)
        rescue
            json_response( {message: "User not created! An error occurred - Username may be taken"}, :bad_request)
        end
    end

    # PUT /api/v1/users
    # use json_response(@xx) for response
    def update
        current_user.update!(user_params)
        current_user.password_digest = "REDACTED"
        json_response({User: current_user, message: "User updated!"})
    end

    # DELETE /api/v1/users/:id
    def destroy
        username = current_user.username
        current_user.destroy
        json_response({message: "User #{username} deleted!"}, :accepted)
    end

    private

    def user_params
        params.permit(:password, :username, :id)
    end
end