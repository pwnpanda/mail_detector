Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  namespace :api do
    namespace :v1 do
      resources :users do
        post 'login', to: 'authentication#login'
        resources :pills
        resources :days
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
