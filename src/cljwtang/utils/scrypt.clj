(ns cljwtang.utils.scrypt
  "Functions for encrypting passwords using the cutting-edge scrypt algorithm.
  See: https://www.tarsnap.com/scrypt/scrypt.pdf"
  (:import com.lambdaworks.crypto.SCryptUtil))

(defn encrypt
  "Encrypt a password string using the scrypt algorithm. This function takes
  three optional parameters:
    n - the CPU cost, must be a power of 2, defaults to 2^14
    r - the memory cost, defaults to 8
    p - the parallelization parameter, defaults to 1"
  ([raw]
     (encrypt raw 16384))
  ([raw n]
     (encrypt raw n 8 1))
  ([raw n r p]
     (SCryptUtil/scrypt raw n r p)))

(defn check
  "Compare a raw string with a string encrypted with the
  crypto.password.scrypt/encrypt function. Returns true the string match, false
  otherwise."
  [raw encrypted]
  (SCryptUtil/check raw encrypted))

(defn verify [raw encrypted]
  (check raw encrypted))

(defn- credential-fn
  [verify-fn load-credentials-fn {:keys [username password]}]
  (println "u p########" username password)
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
