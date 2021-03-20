class ApplicationController < ActionController::API
    def store
        # get json data from post data
        # field named timestamp
        if params[:timestamp] then
            timestamp = params[:timestamp]
            puts "Params in:"
            puts timestamp
            # store json data in db
            # Reply with 200 OK
        else
            # Give 400 if not correct format
            head 400
        end
    end
    def get
        # get json data from db
        #something
        # send reply
        @data = "jsondata"
    end
end