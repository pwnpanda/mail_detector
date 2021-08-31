class Pill < ApplicationRecord
  belongs_to :user
  has_many :records, dependent: :destroy
  has_many :days, :through => :records
end
