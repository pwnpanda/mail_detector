class UserDay < ApplicationRecord
  belongs_to :day
  belongs_to :user
  belongs_to :pill
end
