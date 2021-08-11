class Api::V1::UsersController < ApplicationController
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
        @user = User.new(user_params)
        if @user.save
            payload = {user_id: @user.id}
            token = create_token(payload)
            render json: @user, status: :created, location: @user
        else
            json_response(@user.errors, :unprocessable_entity)
        end
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
        params.require(:username).permit(:password).tap do |user_params|
            user_params.require(:password)
        end
    end

    def set_user
        @user = User.find(params[:id])
    end
end