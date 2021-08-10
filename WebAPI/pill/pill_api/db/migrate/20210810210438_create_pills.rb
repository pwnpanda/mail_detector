class CreatePills < ActiveRecord::Migration[6.1]
  def change
    create_table :pills do |t|
      t.string :uuid
      t.string :color
      t.references :user, null: false, foreign_key: true
      t.boolean :active
      t.datetime :created

      t.timestamps
    end
  end
end
