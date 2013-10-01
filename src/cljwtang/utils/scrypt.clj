(ns cljwtang.utils.scrypt
  (require [crypto.password.scrypt :as password]))

(defn verify [raw encrypted]
  (password/check raw encrypted))

(defn- credential-fn
  [verify-fn load-credentials-fn {:keys [username password]}]
  (when-let [creds (load-credentials-fn username)]
     (let [password-key (or (-> creds meta ::password-key) :password)]
       (when (verify-fn password (get creds password-key))
         (dissoc creds password-key)))))

(defn scrypt-credential-fn
  "A SCrypt credentials function intended to be used with `cemerick.friend/authenticate`
    or individual authentication workflows.  You must supply a function of one argument
    that will look up stored user credentials given a username/id.  e.g.:

    (authenticate {:credential-fn (partial scrypt-credential-fn load-user-record)
                   :other :config ...}
      ring-handler-to-be-secured)

    The credentials map returned by the provided function will only be returned if
    the provided (cleartext, user-supplied) password matches the hashed
    password in the credentials map.

    The password in the credentials map will be looked up using a :password
    key by default; if the credentials-loading function will return a credentials
    map that stores the hashed password under a different key, it must specify
    that alternative key via a :cemerick.friend.credentials/password-key slot
    in the map's metadata.  So, if a credentials map looks like:

      {:username \"joe\" :app.foo/passphrase \"scrypt hash\"}

    ...then the hash will be verified correctly as long as the credentials
    map contains a [:cemerick.friend.credentials/password-key :app.foo/passphrase]
    entry."
  [load-credentials-fn user]
  (credential-fn verify load-credentials-fn user))
