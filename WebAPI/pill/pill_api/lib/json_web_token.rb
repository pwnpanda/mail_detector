module JsonWebToken

    def self.encode(payload, exp = 744.hours.from_now)
      payload[:exp] = exp.to_i
      JWT.encode(payload, SECRET_KEY)
    end
  
    def self.decode(token)
      body = JWT.decode(token, SECRET_KEY)[0]
      HashWithIndifferentAccess.new body
    rescue
      nil
    end

    private

    SECRET_KEY = Rails.application.secrets.secret_key_base || Rails.application.credentials.secret_key_base
    
end
