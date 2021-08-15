class Api::V1::DaysController < ApplicationController
    before_action :set_day, only: [:show, :update, :destroy]

    # GET /api/v1/user/:user_id/days
    def index
        days = Day.order("created_at DESC")
        json_response(days)
    end

    # GET /api/v1/user/:user_id/days/:id
    def show
        json_response(@day)
    end

    # POST /api/v1/user/:user_id/days
    def create
        day = Day.create!(day_params)
        json_response(day, :created)
    end

    # PUT /api/v1/user/:user_id/days
    def update
        @day.update(day_params)
        json_response( { message: "Day #{@day.today} updated!" } )
    end

    # DELETE /api/v1/user/:user_id/days/:id
    def destroy
        today = @day.today
        @day.destroy
        json_response( { message: "Day #{today} deleted!" }, :accepted )
    end

    private

    def set_day
        @day = Day.find(params[:id])
    end

    def day_params
        params.permit(:today)
    end

end
