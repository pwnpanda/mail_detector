class Day < ApplicationRecord
    has_many :user_days
    has_many :pills, :through => :user_days
end
