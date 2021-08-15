class CreatePills < ActiveRecord::Migration[6.1]
  def change
    create_table :pills do |t|
      t.string :uuid
      t.string :color
      t.boolean :active
      t.belongs_to :user, null: false, foreign_key: true

      t.timestamps
    end
  end
end
