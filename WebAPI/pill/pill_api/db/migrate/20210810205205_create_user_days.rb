class CreateUserDays < ActiveRecord::Migration[6.1]
  def change
    create_table :user_days do |t|
      t.references :day, null: false, foreign_key: true
      t.references :user, null: false, foreign_key: true
      t.string :taken
      t.references :pill, null: false, foreign_key: true

      t.timestamps
    end
  end
end
