class User < ApplicationRecord
  has_secure_password
  has_many :records, dependent: :destroy
  has_many :pills, :through => :records
  has_many :days, :through => :records
end
