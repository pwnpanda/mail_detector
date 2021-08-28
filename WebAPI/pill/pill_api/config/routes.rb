Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  namespace :api do
    namespace :v1 do
      post 'login', to: 'authentication#authenticate'
      post 'signup', to: 'users#create'
      resources :users do
        resources :days
        resources :records
        resources :pills
      end
    end
    # All other gets 404'd
    get '*unmatched_route', to: 'errors#show'
    post '*unmatched_route', to: 'errors#show'
    delete '*unmatched_route', to: 'errors#show'
    put '*unmatched_route', to: 'errors#show'
  end
  # All other gets 404'd
  get '*unmatched_route', to: 'errors#show'
  post '*unmatched_route', to: 'errors#show'
  delete '*unmatched_route', to: 'errors#show'
  put '*unmatched_route', to: 'errors#show'
end
