Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  # post calls to /store goes to method store() in applicationcontroller
  post 'store', to: 'application#store'
  # get calls to /logs goes to method get() in applicationcontroller
  get 'logs', to: 'application#get'
  get '*unmatched_rout', to: 'errors#show'
end
