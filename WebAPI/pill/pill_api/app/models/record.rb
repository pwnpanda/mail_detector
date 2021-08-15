class Record < ApplicationRecord
  belongs_to :day
  belongs_to :user
  belongs_to :pill
end
