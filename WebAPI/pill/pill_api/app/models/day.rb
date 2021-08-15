class Day < ApplicationRecord
    has_many :records
    has_many :pills, :through => :records
end
