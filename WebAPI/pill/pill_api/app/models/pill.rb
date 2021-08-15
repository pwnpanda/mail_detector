class Pill < ApplicationRecord
  belongs_to :user
  has_many :record
  has_many :days, :through => :records
end
