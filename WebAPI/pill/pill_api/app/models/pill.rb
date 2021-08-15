class Pill < ApplicationRecord
  belongs_to :user
  has_many :records
  has_many :days, :through => :records
end
