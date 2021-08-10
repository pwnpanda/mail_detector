# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `bin/rails
# db:schema:load`. When creating a new database, `bin/rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 2021_08_10_210438) do

  create_table "days", force: :cascade do |t|
    t.datetime "today"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "pills", force: :cascade do |t|
    t.string "uuid"
    t.string "color"
    t.integer "user_id", null: false
    t.boolean "active"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["user_id"], name: "index_pills_on_user_id"
  end

  create_table "user_days", force: :cascade do |t|
    t.integer "day_id", null: false
    t.integer "user_id", null: false
    t.string "taken"
    t.integer "pill_id", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["day_id"], name: "index_user_days_on_day_id"
    t.index ["pill_id"], name: "index_user_days_on_pill_id"
    t.index ["user_id"], name: "index_user_days_on_user_id"
  end

  create_table "users", force: :cascade do |t|
    t.string "username"
    t.string "password"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  add_foreign_key "pills", "users"
  add_foreign_key "user_days", "days"
  add_foreign_key "user_days", "pills"
  add_foreign_key "user_days", "users"
end
