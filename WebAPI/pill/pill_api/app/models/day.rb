class Day < ApplicationRecord
    has_many :records, dependent: :destroy
    has_many :pills, :through => :records
end
