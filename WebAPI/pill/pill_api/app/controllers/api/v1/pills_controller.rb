class Api::V1::PillsController < ApplicationController
    before_action :set_pill, only: [:show, :update, :destroy]
    
    
    # GET /api/v1/user/:user_id/pills
    def index
        pills = Pill.where(user_id: current_user.id.to_i)
        json_response(pills)
    end

    # GET /api/v1/user/:user_id/pills/:id
    def show
        json_response(@pill)
    end

    # POST /api/v1/user/:user_id/pills
    def create
        pill = Pill.create!(pill_params)
        json_response(pill, :created)
    end

    # PUT /api/v1/user/:user_id/pills
    def update
        @pill.update(pill_params)
        json_response( {message: "Pill #{@pill.uuid} updated!"} )
    end

    # DELETE /api/v1/user/:user_id/pills/:id
    def destroy
        uuid = @Pill.uuid
        @Pill.destroy
        json_response( {message: "Pill #{uuid} deleted!"}, :accepted)
    end

    private

    def set_pill
        @pill = Pill.find(params[:id])
        if @pill.user_id != current_user.id.to_i
            json_response({message: "Pill #{params[:id]} not found!"}, :not_found)
        end
    end

    def pill_params
        params.permit(:color, :active, :uuid)
    end
end
