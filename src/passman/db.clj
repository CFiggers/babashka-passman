(ns passman.db
  (:require [babashka.pods :as pods]
            ;; [honey.sql :as sql]
            ;; [honey.sql.helpers :as h]
            [babashka.fs :as fs]))

(pods/load-pod 'org.babashka/go-sqlite3 "0.1.0")

(require '[pod.babashka.go-sqlite3 :as sqlite])

(def dbname (str (System/getenv "HOME") "/passman/passman.db"))

(defn create-db! []
  (when (not (fs/exists? dbname))
    (sqlite/execute! dbname
                    ;;  (-> (h/create-table :passwords)
                    ;;      (h/with-columns [[:url :text]
                    ;;                       [:username :text]
                    ;;                       [[:unique nil :url :username]]])
                    ;;      (sql/format))
                     ["CREATE TABLE passwords (url TEXT, username TEXT, UNIQUE(url, username))"])))

(defn insert-password! [url username]
  (sqlite/execute! dbname
                  ;;  (-> (h/insert-into :passwords)
                  ;;      (h/columns :url :username)
                  ;;      (h/values [[url username]])
                  ;;      (sql/format))
                   ["INSERT INTO passwords (url, username) VALUES (?, ?)" url username]))

(defn delete-password! [url username]
  (sqlite/execute! dbname
                  ;;  (-> (h/delete-from :passwords)
                  ;;      (h/where [:= :url url]
                  ;;               [:= :username username])
                  ;;      (sql/format))
                   ["DELETE FROM passwords WHERE (url = ?) AND (username = ?)" url username]))

(defn list-passwords []
  (sqlite/query dbname
                ;; (-> (h/select :url :username)
                ;;     (h/from :passwords)
                ;;     (sql/format))
                ["SELECT url, username FROM passwords"]))

(create-db!)

(comment
  (list-passwords)

  (insert-password! "facebook.com" "caleb@test.com")

  ;; (-> (h/insert-into :passwords)
  ;;     (h/columns :url :username)
  ;;     (h/values [["facebook.com" "caleb@test.com"]])
  ;;     (sql/format))

  ;; (-> (h/delete-from :passwords)
  ;;     (h/where [:= :url "facebook.com"]
  ;;              [:= :username "caleb@test.com"])
  ;;     (sql/format))

  ;; (sqlite/query dbname
  ;;               (-> (h/select :url :username)
  ;;                   (h/from :passwords)
  ;;                   (sql/format)))
  )