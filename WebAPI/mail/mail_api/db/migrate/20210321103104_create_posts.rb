class CreatePosts < ActiveRecord::Migration[6.1]
  def change
    create_table :posts do |t|
      t.string :username
      t.datetime :delivered
      t.datetime :pickup

      t.timestamps
    end
  end
end
