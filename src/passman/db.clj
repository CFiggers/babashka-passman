(ns passman.db
  (:require [babashka.pods :as pods]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [babashka.fs :as fs]))

(pods/load-pod 'org.babashka/go-sqlite3 "0.1.0")

(require '[pod.babashka.go-sqlite3 :as sqlite])

(def dbname "passman.db")

(defn create-db! []
  (when (not (fs/exists? dbname))
    (sqlite/execute! dbname
                     (-> (h/create-table :passwords)
                         (h/with-columns [[:url :text]
                                          [:username :text]
                                          [[:unique nil :url :username]]])
                         (sql/format)))))

(defn insert-password! [url username]
  (sqlite/execute! dbname
                   (-> (h/insert-into :passwords)
                       (h/columns :url :username)
                       (h/values [[url username]])
                       (sql/format))))

(defn list-passwords []
  (sqlite/query dbname
                (-> (h/select :url :username)
                    (h/from :passwords)
                    (sql/format))))

(create-db!)

(comment
  (list-passwords)

  (insert-password! "facebook.com" "caleb@test.com")

  (-> (h/insert-into :passwords)
      (h/columns :url :username)
      (h/values [["facebook.com" "caleb@test.com"]])
      (sql/format))

  (sqlite/query dbname
                (-> (h/select :url :username)
                    (h/from :passwords)
                    (sql/format))))