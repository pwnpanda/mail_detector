class Api::V1::RecordsController < ApplicationController
    before_action :set_record, only: [:show, :update, :destroy]

    # GET /api/v1/user/:user_id/records
    def index
        records = records = Record.where(user_id: current_user.id.to_i)
        
        # Get records by pill
        if params[:pill].present?
            records = records.where(pill_id: params[:pill].to_i)
        # Get records by day
        elsif params[:day].present?
            records = records.where(day_id: params[:day].to_i)
        end

        json_response(records)
    end

    # GET /api/v1/user/:user_id/records/:id
    def show
        json_response(@record)
    end

    # POST /api/v1/user/:user_id/records
    def create
        params = record_params
        # if taken is not set, set it to false
        if params[:taken] == nil
            params[:taken] = false
        end
        # Created by referencing a day, a user, a pill and if it is taken (boolean)
        day = ""
        # Find or create day object
        if params[:day_id]
            day = Day.find!(params[:day_id])
        else
            day = Day.find_or_create_by(params[:today])
        end
        record = Record.find_or_create_by!(user: current_user, day: day, pill: params[:pill_id], taken: params[:taken])
        json_response(day, :created)
    end

    # PUT /api/v1/user/:user_id/records
    def update
        @record.update(record_params)
        json_response( { message: "Record #{@record.day.today} - #{@record.user.username} - #{@record.pill.uuid} updated!" } )
    end

    # DELETE /api/v1/user/:user_id/records/:id
    def destroy
        # Some identification
        user = @record.user.username
        pill = @record.pill.uuid
        day = @record.day.today
        @record.destroy
        json_response( { message: "Record for #{day.today} - #{user.username} - #{pill.uuid} deleted!" }, :accepted )
    end

    private

    def set_record
        @record = Record.find(params[:id])
        if @record.user.id != current_user.id.to_i
            json_response( {message: "Record #{params[:id]} not found!"}, :not_found )
        end
    end

    def record_params
        params.require(:record).permit(:day_id, :today, :pill_id, :taken)
    end

end