# friend-oauth2-examples

Use friend-oauth2 version 0.0.4.

Includes [Facebook (server-side authentication)](https://developers.facebook.com/docs/authentication/server-side/), [App.net](https://github.com/appdotnet/api-spec/blob/master/auth.md) and [Github](http://developer.github.com/v3/oauth/) examples using [Friend-OAuth2](https://github.com/ddellacosta/friend-oauth2), an OAuth2 workflow for [Friend](https://github.com/cemerick/friend).

## Running

Configure your client id/secret and callback url in the handler code.

```clojure
(def client-config
  {:client-id "<HERE>"
   :client-secret "<HERE>"
   :callback {:domain "http://<HERE>" :path "/<AND HERE>"}})
```

At that point, you should be able to start it up using lein and aliases

	lein with-profile dev,facebook server -headless

or dev,appdotnet or dev,github

There are also the aliseses:
	lein facebook
	lein appdotnet
	lein github


## License

Distributed under the MIT License (http://dd.mit-license.org/)

## Authors

* Facebook, App.net examples by [ddellacosta](https://github.com/ddellacosta)
* Github example by [kanej](https://github.com/kanej)
