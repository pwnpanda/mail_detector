class CreateDays < ActiveRecord::Migration[6.1]
  def change
    create_table :days do |t|
      t.datetime :today

      t.timestamps
    end
  end
end
