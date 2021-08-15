class Api::V1::PillsController < ApplicationController
    before_action :set_pill, only: [:show, :update, :destroy]
    
    # GET /api/v1/user/:user_id/pills
    def index
        0
    end

    # GET /api/v1/user/:user_id/pills/:id
    def show
        #json_response(@obj)
    end

    # POST /api/v1/user/:user_id/pills
    def create
        0
    end

    # PUT /api/v1/user/:user_id/pills
    def update
        0
    end

    # DELETE /api/v1/user/:user_id/pills/:id
    def destroy
        0
    end

    def set_pill
        @pill = Pill.find(params[:id])
    end
end
