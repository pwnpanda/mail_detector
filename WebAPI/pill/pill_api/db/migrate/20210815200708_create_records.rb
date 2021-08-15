class CreateRecords < ActiveRecord::Migration[6.1]
  def change
    create_table :records do |t|
      t.belongs_to :day, null: false, foreign_key: true
      t.belongs_to :user, null: false, foreign_key: true
      t.belongs_to :pill, null: false, foreign_key: true
      t.string :taken

      t.timestamps
    end
  end
end
