Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  namespace :api do
    resources :posts
    namespace :post do
      get 'status', to: 'post#get_status'
      post 'status', to: 'post#set_status'
    end
    
    # All other gets 404'd
    get '*unmatched_route', to: 'errors#show'
    post '*unmatched_route', to: 'errors#show'
    delete '*unmatched_route', to: 'errors#show'
    put '*unmatched_route', to: 'errors#show'
    #resources :posts # do
      # post calls to /store goes to method store() in applicationcontroller
      #post 'store', to: 'application#store'
      # get calls to /logs goes to method get() in applicationcontroller
      #get 'logs', to: 'application#get'
    #end
  
  end
  # All other gets 404'd
  get '*unmatched_route', to: 'errors#show'
  post '*unmatched_route', to: 'errors#show'
  delete '*unmatched_route', to: 'errors#show'
  put '*unmatched_route', to: 'errors#show'
end
