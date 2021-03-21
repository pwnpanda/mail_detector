class Api::PostsController < ApplicationController

    # https://medium.com/@oliver.seq/creating-a-rest-api-with-rails-2a07f548e5dc
    # https://codebrains.io/build-todolist-rest-api-ruby-rails/

    # GET /api/posts
    def index
        # Return last 14 days
        @posts = Post.all.limit(14)
        render json: @posts
    end

    # POST /api/posts
    def create
        begin
            # Verify params - if fails, return 400
            post = post_params
            # Convert to datetime in object post
            post[:pickup] = DateTime.parse(params[:pickup])
            post[:delivered] = DateTime.parse(params[:delivered])
            # create
            @post = Post.new(post)
            puts post_params
            if @post.save
                render json: @post
            else
                render json: {error: "Unable to store data!" }, status: 400
            end
        # Return 400
        rescue ActionController::ParameterMissing
            render json: {error: "Unable to store data!" }, status: 400
        end


    end
    
    # DELETE /api/post/:id
    def destroy
        # Verify post exists
        begin
            @post = Post.find(params[:id])
            if @post
                    @post.destroy
                    render json: { message: "Post deleted!" }, status: 200
            else
                render json: { error: "Not able to delete data!" }, status: 400
            end
        # If no post, return 404
        rescue ActiveRecord::RecordNotFound
            render json: { error: "Not found!" }, status: 404
        end
    end

    # Helper to verify parameters
    private
    def post_params
        # Require that all params are present
        params.require(:post).permit(:username, :delivered, :pickup).tap do |post_params|
            post_params.require([:username, :delivered, :pickup])
        end
    end
end
