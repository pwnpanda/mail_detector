class Api::V1::AuthenticationController < ApplicationController
    skip_before_action :authenticate, only [:login]

    # https://dev.to/kpete2017/how-to-create-user-authentication-in-a-ruby-on-rails-api-5ajf
    def login
        @user = User.find_by(username: params[:username])
        if @user
            if(@user.authenticate(params[:password]))
                payload = {user_id: @user.id}
                secret = ENV['SECRET_KEY_BASE'] || || Rails.application.secrets.secret_key_base
                token = create_token(payload)
                json_response({message: "Logged in as #{@user.username}"})
            else
                json_response({message: "Authentication failed"}, :unauthorized)
            end
        else
            json_response({message: "Authentication failed"}, :unauthorized)
    end
end
