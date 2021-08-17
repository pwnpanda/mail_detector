# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the bin/rails db:seed command (or created alongside the database with db:setup).
#
# Examples:
#
#   movies = Movie.create([{ name: 'Star Wars' }, { name: 'Lord of the Rings' }])
#   Character.create(name: 'Luke', movie: movies.first)
u1 = User.find(1)
d1 = Day.find_or_create_by!(today: "2021-08-12 19:48:19.800489")
p1 = Pill.find_or_create_by!(uuid: "uuid-123", color: "blue", active: true, user: u1.first)
# Note how the previous pill object is used to insert!
p2 = Pill.find_or_create_by!(uuid: "uuid-456", color: "red", active: false, user: p1.user)
p3 = Pill.find_or_create_by!(uuid: "uuid-789", color: "green", active: false, user: User.where(username: "User").first)
r1 = Record.find_or_create_by!(day: d1, user: p1.user, pill: p1, taken: "")