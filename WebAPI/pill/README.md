## Create

- rails new pill_api --api
- rails generate model User username:uniq password:digest
- rails generate model Pill uuid:string color:string active:boolean user:belongs_to
- rails generate model Record day:belongs_to user:belongs_to pill:belongs_to taken:string
- rails generate model Day today:date

- rails db:migrate
- rails db:setup

- rails g controller api/v1/Users
- rails g controller api/v1/Days
- rails g controller api/v1/Pills
- rails g controller authentication

- bundle install (after adding bcrypt, simple_command and jwt to Gemfile)


#### NOTES
- OBJECT.records returns a CollectionProxy, which is an array. Each item can be grabbed if you get the relevant entry:
    * d1.records.first.day, where d1 is a Day object
- Must check all updates manually and add error handling - only relevant for records! DONE
- Move all tests to postman? Easier and better automation! DONE
- Redeploy: docker-compose up --build --force-recreate

###### How to change db schema
- Make change directly in original migration, then:
    - rake db:migrate:reset
    - rake db:setup
## References for multiple fields
- Arrays: https://stackoverflow.com/questions/32409820/add-an-array-column-in-rails
- UUID: https://pawelurbanek.com/uuid-order-rails
- Arrays: https://www.codegrepper.com/code-examples/ruby/model+with+array+rails
- Return array: https://stackoverflow.com/questions/39102652/rails-api-adding-array-of-objects-to-json-return
- Model attr as array: https://juliana-ny2008.medium.com/rails-can-i-set-an-attribute-of-model-to-array-or-hash-b53d8b524dd3
- Many to many fields: https://stackoverflow.com/questions/21312252/how-to-handle-many-to-many-records-when-using-rails-as-rest-api
- Array in sqlite: https://apidock.com/rails/ActiveRecord/AttributeMethods/Serialization/ClassMethods/serialize
- HABTM: https://www.seancdavis.com/blog/why-i-dont-use-has-and-belongs-to-many-in-rails/
- HABTM: https://guides.rubyonrails.org/association_basics.html#choosing-between-has-many-through-and-has-and-belongs-to-many
- HABTM: https://medium.com/rubycademy/habtm-to-has-many-through-43f68f50e50e
- Login: https://dev.to/kpete2017/how-to-create-user-authentication-in-a-ruby-on-rails-api-5ajf
- JWT login: https://github.com/hggeorgiev/rails-jwt-auth-tutorial, https://www.pluralsight.com/guides/token-based-authentication-with-ruby-on-rails-5-api
- Best login guide: https://scotch.io/tutorials/build-a-restful-json-api-with-rails-5-part-two
- HTTP-codes: https://gist.github.com/mlanett/a31c340b132ddefa9cca
- DB actions: https://medium.com/@woodpecker21/rails-6-how-to-search-and-filter-index-results-2b7d4b348393
- How to build: https://meaganwaller.com/use-a-nested-dynamic-form-with-a-hasmany-through-association-in-rails
- Complex, nested objects in API - https://medium.com/@lushiyun/rails-api-for-triple-nested-resources-with-fast-json-api-and-javascript-frontend-6ca1e97eb00a 
- Complex, nested objects directly - https://dev.to/lberge17/posting-nested-resources-to-your-rails-api-he8
- Ruby updates suck because they never fail - https://www.ruby-forum.com/t/how-do-you-detect-if-activerecord-update-fails/54424/3

## Based on
- https://pamit.medium.com/todo-list-building-a-react-app-with-rails-api-7a3027907665
- https://www.digitalocean.com/community/tutorials/build-a-restful-json-api-with-rails-5-part-one
- https://medium.com/@oliver.seq/creating-a-rest-api-with-rails-2a07f548e5dc

## Operate
- Signup: `curl -H "Content-Type: application/json" -X POST -d '{"username":"x","password":"1"}' http://localhost:3000/api/v1/signup`
- Login: `curl -H "Content-Type: application/json" -X POST -d '{"username":"x","password":"1"}' http://localhost:3000/api/v1/login`
- Query: `curl -v -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyLCJleHAiOjE2MzAyNTI3NTh9.aYa56YwrMnvMJ6uoHxOGlsmRpkiWsYEONpfMssWbLWM" http://127.0.0.1:3000/api/v1/users`
- Query ID `curl -v -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyLCJleHAiOjE2MjkxNDU4ODR9.XXwI7IPAWWKb8BZ3KJixhmRHCfXQQfoaOeBuKmca3eo" http://127.0.0.1:3000/api/v1/users/1`
- Create pill: `curl -v -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyLCJleHAiOjE2MzAyNTI3NTh9.aYa56YwrMnvMJ6uoHxOGlsmRpkiWsYEONpfMssWbLWM" -H "Content-Type: application/json" -POST "http://127.0.0.1:3000/api/v1/users/2/pills" -d '{"color":"azure", "active":true}'`
- Create day: `curl -v -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyLCJleHAiOjE2MzAyNTI3NTh9.aYa56YwrMnvMJ6uoHxOGlsmRpkiWsYEONpfMssWbLWM" -POST http://127.0.0.1:3000/api/v1/users/2/days -d "{ 'today': 2021-08-15 }"`
- Get records: `curl -v -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyLCJleHAiOjE2MzAyNTI3NTh9.aYa56YwrMnvMJ6uoHxOGlsmRpkiWsYEONpfMssWbLWM" http://127.0.0.1:3000/api/v1/users/2/record`
    - Optional parameters for filtering:
        * ?pill=PILL_ID `http://127.0.0.1:3000/api/v1/users/2/record?pill=1`
        * ?day=DAY_ID: `http://127.0.0.1:3000/api/v1/users/2/record?day=1`


##### JSON Data
* All put operations return: {<entity>: <data>, message: <status>}, example: {"pill": {<pill_fields>}, "message": Pill updated!"}

- Signup
`{"id":3,"username":"testme","token":"eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjozLCJleHAiOjE2MzA2Njg0Mjh9.mFuPlFU500faHangh1xiqJwHvg38ryJ2G5wFWS9KPKE"}`
- Login
`{"token":"eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjozLCJleHAiOjE2MzA2Njg0Mjl9.O5f1aupLegMhxpi-xLdUo3ecpO1DUd-fxQEIR_J5c_s"}`
- User:
`{"id":3,"username":"testme","password_digest":"REDACTED","created_at":"2021-09-02T11:27:07.635Z","updated_at":"2021-09-02T11:27:07.635Z"}`
- Day:
`{"id":4,"today":"2021-08-15","created_at":"2021-09-02T11:27:14.397Z","updated_at":"2021-09-02T11:27:14.397Z"}`
- Pill:
`{"id":1,"uuid":"1d749fdd-2d2f-4877-8046-0e69612f3668","color":"#000000","active":true,"user_id":3,"created_at":"2021-09-02T11:27:18.717Z","updated_at":"2021-09-02T11:27:18.717Z"}`
`{"color":"#999999","active":false,"id":1,"uuid":"1d749fdd-2d2f-4877-8046-0e69612f3668","user_id":3,"created_at":"2021-09-02T11:27:18.717Z","updated_at":"2021-09-02T11:27:21.977Z"}`
- Record:
`{"id":2,"day_id":2,"user_id":3,"pill_id":1,"taken":false,"created_at":"2021-09-02T11:27:24.269Z","updated_at":"2021-09-02T11:27:24.269Z"}`
`{"id":1,"day_id":4,"user_id":3,"pill_id":1,"taken":true,"created_at":"2021-09-02T11:27:23.121Z","updated_at":"2021-09-02T11:27:23.121Z"}`

##### Routes
- /api/v1/login                     (POST)
- /api/v1/signup                    (POST)

- /api/v1/users                     (GET, POST)
- /api/v1/users/:id                 (GET, PUT, DELETE)

- /api/v1/users/:user_id/pills      (GET, POST)
- /api/v1/users/:user_id/pills/:id  (GET, PUT, DELETE)

- /api/v1/users/:user_id/days       (GET, POST)
- /api/v1/users/:user_id/days/:id   (GET, PUT, DELETE)

### DB Schema

```
  create_table "days", force: :cascade do |t|
    t.date "today"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "pills", force: :cascade do |t|
    t.string "uuid"
    t.string "color"
    t.boolean "active"
    t.integer "user_id", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["user_id"], name: "index_pills_on_user_id"
  end

  create_table "records", force: :cascade do |t|
    t.integer "day_id", null: false
    t.integer "user_id", null: false
    t.integer "pill_id", null: false
    t.boolean "taken"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["day_id"], name: "index_records_on_day_id"
    t.index ["pill_id"], name: "index_records_on_pill_id"
    t.index ["user_id"], name: "index_records_on_user_id"
  end

  create_table "users", force: :cascade do |t|
    t.string "username"
    t.string "password_digest"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["username"], name: "index_users_on_username", unique: true
  end

  add_foreign_key "pills", "users"
  add_foreign_key "records", "days"
  add_foreign_key "records", "pills"
  add_foreign_key "records", "users"
```

### Run:
Follow (this article)[https://medium.com/@mshostdrive/how-to-run-a-rails-app-in-production-locally-f29f6556d786]
	- `RAILS_ENV=production rake db:create db:migrate db:seed`
	- Copy output from: `rake secret`
	- `export SECRET_KEY_BASE=<output-of-rake-secret>`
	- `RAILS_ENV=production rails s`
	- `rails s -p 12121 -b 127.0.0.1 -e production`

### Docker

	- Easy mode:
		- put secret in "secret.txt" with dockerfile and docker-compse.yml
		- docker-compose up -d	

  - Restart the previously running container:
    - docker-compose up --detach --build
	
	- Build image: `docker build --tag mail_api:latest .`
	- Cleanup previous fragments `docker ps -aq --filter "name=mail_api"|grep -q . && docker stop mail_api && docker container rm -fv mail_api || true`
	- Start new instance `docker run --name mail_api -d=true -v mail_api_db:/mail_api/db/ -p 12121:12121 mail_api:latest`

	- To persist storage:
		* create volume: `docker volume create mail_api_db`
		* Use volume (as seen above)
		* FInd real location: `docker volume inspect mail_api_db`
		* Copy from that location to backup spot
