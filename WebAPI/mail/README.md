# Install instructions

1. `sudo apt-get install -y ruby-dev ruby sqlite3 libsqlite3-dev`
2. `gem install rails`
3. `bundle install`
4. `rake app:update:bin`
4.  Follow (this article)[https://gorails.com/deploy/ubuntu/18.04#ruby]

### Built:
- https://medium.com/@oliver.seq/creating-a-rest-api-with-rails-2a07f548e5dc
- https://codebrains.io/build-todolist-rest-api-ruby-rails/

### Requests
curl -v -XPOST https://robinlunde.com/api/post/status -H "Content-Type: application/json" -d '{"newMail":false,"username":"Robin","timestamp":"2021-03-23T22:53:16.686Z"}'

curl -v -XPOST https://robinlunde.com/api/posts -H "Content-Type: application/json" -d '{"delivered": "2021-03-10T12:13:16.686Z", "username":"Robin", "pickup":"2021-03-10T13:14:16.686Z"}'

curl -v https://robinlunde.com/api/post/status

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
