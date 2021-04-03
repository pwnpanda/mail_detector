class Api::Post::PostController < ApplicationController
    
    @@newMail = false
    @@username = ""
    @@timestamp = ""

    # GET /api/post/status
    def get_status
        puts @@newMail
        render json: {newMail: @@newMail, username: @@username, timestamp: @@timestamp }, status: 200
    end

    # POST /api/post/status
    def set_status
        # Verify params - if fails, return 400
        begin
            status = status_params
            @@timestamp = status[:timestamp]
            @@username = status[:username]
            @@newMail = status[:newMail]
            render json: {message: "Status stored!"}, status: 200
        # Return 400
        rescue ActionController::ParameterMissing
            render json: {error: "Unable to save status!" }, status: 400
    end
    end

    # Helper to verify parameters
    private
    def status_params
        # Require that all params are present
        params.require(:post).permit(:username, :timestamp, :newMail).tap do |status_params|
            status_params.require([:username, :timestamp, :newMail])
        end
    end

end