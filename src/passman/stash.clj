(ns passman.stash
  (:require [babashka.pods :as pods]))

(def ^{:doc "path to stash file"} stash-file-path "passman.stash")

(pods/load-pod 'rorokimdim/stash "0.3.1")

(require '[pod.rorokimdim.stash :as stash])

(defn stash-init
  "Initializes stash.
  If `stash-file-path` does not exist, it will be created."
  [password]
  (stash/init {"encryption-key" password
               "stash-path" stash-file-path
               "create-stash-if-missing" true}))

(defn stash-add
  "Adds a new node under a parent."
  [parent-id k v]
  (stash/add parent-id k v))

(defn stash-update
  "Updates a node given a node id"
  [nid v]
  (stash/update nid v))

(defn stash-delete
  "Deletes a node given a node id"
  [nid]
  (stash/delete nid))

(defn stash-nodes
  "Gets all nodes stored in stash.
  If a parent-node-id is provided, only nodes with that parent-id are returned."
  ([] (stash-nodes 0))
  ([parent-id] (stash/nodes parent-id)))

(defn find-password-node [url username]
  (let [nodes (stash-nodes)
        key (str url username)
        found-node (first (filter (fn [n]
                                    (= (:key n) key)) nodes))]
    found-node))

(defn find-password [url username]
  (let [found-node (find-password-node url username)
        password (:value found-node)]
    password))

(defn insert-password! [url username password]
  (stash-add 0 (str url username) password))

(defn update-password!
  "Updates a node using url and username"
  [url username password]
  (let [found-node (find-password-node url username)
        nid (:id found-node)]
    (stash-update nid password)))

(defn delete-password!
  "Deletes a node using url and username"
  [url username]
  (let [found-node (find-password-node url username)
        nid (:id found-node)]
    (stash-delete nid)))

(comment

  (find-password "facebook.com" "testing@test.com")

  (stash-init "password")

  (insert-password! "facebook.com" "caleb@test.com" "secret")

  (stash-nodes))
