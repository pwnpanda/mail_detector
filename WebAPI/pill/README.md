## Create

- rails new pill_api --api --database=postgresql
- rails generate model User username:string password:string
- rails generate model Pill uuid:string color:string user:references active:boolean created:datetime
- rails generate model UserDay day:references user:references taken:string pill:references
- rails generate model Day today:datetime

- rails db:setup
- rails db:migrate

- rails g controller api/v1/Users
- rails g controller api/v1/Days
- rails g controller api/v1/Pills

- Change user to this? YES
    - rails g model User username:uniq password:digest

- bundle install (after adding bcrypt, simple_command and jwt to Gemfile)
- rails g controller api/v1/authentication

- CHANGE TO THIS!! https://github.com/hggeorgiev/rails-jwt-auth-tutorial, https://www.pluralsight.com/guides/token-based-authentication-with-ruby-on-rails-5-api

#### TODO
- Run the above - do we need postgresql?
- Make taken and pill arrays for "Day"
    - Taken is array of pill uuids OR direct references to pills
    - Pill is array of pills
- Change from sqlite to postgres to support arrays

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

## Based on
- https://pamit.medium.com/todo-list-building-a-react-app-with-rails-api-7a3027907665
- https://www.digitalocean.com/community/tutorials/build-a-restful-json-api-with-rails-5-part-one
- https://medium.com/@oliver.seq/creating-a-rest-api-with-rails-2a07f548e5dc

## Operate


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


	
	
	- Build image: `docker build --tag mail_api:latest .`
	- Cleanup previous fragments `docker ps -aq --filter "name=mail_api"|grep -q . && docker stop mail_api && docker container rm -fv mail_api || true`
	- Start new instance `docker run --name mail_api -d=true -v mail_api_db:/mail_api/db/ -p 12121:12121 mail_api:latest`

	- To persist storage:
		* create volume: `docker volume create mail_api_db`
		* Use volume (as seen above)
		* FInd real location: `docker volume inspect mail_api_db`
		* Copy from that location to backup spot
