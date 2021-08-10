class Pill < ApplicationRecord
  belongs_to :user
  has_many :user_day
  has_many :days, :through => :user_days
end
