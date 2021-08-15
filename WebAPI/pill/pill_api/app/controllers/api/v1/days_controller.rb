class Api::V1::DaysController < ApplicationController
    before_action :set_day, only: [:show, :update, :destroy]

    # GET /api/v1/user/:user_id/days
    def index
        0
    end

    # GET /api/v1/user/:user_id/days/:id
    def show
        #json_response(@obj)
    end

    # POST /api/v1/user/:user_id/days
    def create
        0
    end

    # PUT /api/v1/user/:user_id/days
    def update
        0
    end

    # DELETE /api/v1/user/:user_id/days/:id
    def destroy
        0
    end

    def set_day
        @day = Day.find(params[:id])
    end
end
