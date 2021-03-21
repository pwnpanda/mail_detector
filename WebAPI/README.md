# Install instructions

1. `sudo apt-get install -y ruby-dev ruby sqlite3 libsqlite3-dev`
2. `gem install rails`
3. `bundle install`
4. Follow (this article)[https://medium.com/@mshostdrive/how-to-run-a-rails-app-in-production-locally-f29f6556d786]
	- `RAILS_ENV=production rake db:create db:migrate db:seed`
	- Copy output from: `rake secret`
	- `export SECRET_KEY_BASE=<output-of-rake-secret>`
	- Change `production.rb` from `config.assets.compile = false` to `config.assets.compile = true`
	- `RAILS_ENV=production bundle exec rake assets:precompile`
	- `RAILS_ENV=production rails s`