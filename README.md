# PersonalStatus-API
Actually does a lot more than only managing my discord status. This is the backend for my [portfolio website](https://github.com/Ym64/M64Dev).

### Environment variables
- `POST_API_KEY`: sting which acts as the API key to use the set-request to change the status
- `ALLOWED_HOSTS`: list of URL's which are allowed to use the api get, separated by a comma (eg. `google.com,github.com,*.docker,io`)
- `CONTACT_WEBHOOK_URL`: url to send the webhook message to when people use the Contact Me function
- `TURNSTILE_SECRET_KEY`: secret key for Cloudflare's Turnstile human verification

