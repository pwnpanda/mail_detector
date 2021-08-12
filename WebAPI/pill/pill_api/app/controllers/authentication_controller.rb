class Api::V1::AuthenticationController < ApplicationController
    skip_before_action :authenticate_request
    
    # https://www.pluralsight.com/guides/token-based-authentication-with-ruby-on-rails-5-api
    def authenticate
        command = AuthenticateUser.new(params[:username], params[:password]).call
      
        if command.success?
            render json: { auth_token: command }
        else
            render json: { error: command.errors }, status: :unauthorized
        end
    end
end
