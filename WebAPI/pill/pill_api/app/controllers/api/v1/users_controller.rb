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
        user = User.find_or_create_by!(user_params)
        response = AuthenticateUser.new(user.username, user.password).call
        auth_token = response.result
        json_response({message: "User #{user.username} created!", token: auth_token}, :created)
    end

    # PUT /api/v1/users
    # use json_response(@xx) for response
    def update
        current_user.update(user_params)
        json_response({message: "User #{current_user.username} updated!"})
    end

    # DELETE /api/v1/users/:id
    def destroy
        username = current_user.username
        current_user.destroy
        json_response({message: "User #{username} deleted!"}, :accepted)
    end

    private

    def user_params
        params.permit(:password, :username)
    end
end