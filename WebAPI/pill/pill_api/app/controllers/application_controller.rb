class ApplicationController < ActionController::API
    include Response
    include ExceptionHandler
    
    # https://dev.to/kpete2017/how-to-create-user-authentication-in-a-ruby-on-rails-api-5ajf
    def authenticate
        if request.headers["Authorization"]
            begin
                auth_header = request.headers["Authorization"]
                decoded_token = JWT.decode(token, secret)
                payload = decoded_token.first
                user_id = payload["user_id"]
                @user = User.find(user_id)
            rescue => exception
                json_response( {message: "Error: #{exception}"}, :forbidden)
            end
        else
            json_response({ message: "Authorization header required"}, :forbidden)
        end
    end
   
    def secret
        secret = ENV['SECRET_KEY_BASE'] || Rails.application.secrets.secret_key_base
    end

    def token
        auth_header.split(" ")[1]
    end

    def create_token(payload)
        JWT.encode(payload, secret)
    end

end