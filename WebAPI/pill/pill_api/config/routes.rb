Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  namespace :api do
    namespace :v1 do
      post 'login', to: 'authentication#authenticate'
      post 'signup', to: 'users#create'
      resources :users do
        resources :pills
        resources :days
        resources :records
      end
    end
  end
end
