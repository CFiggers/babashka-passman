(ns passman.app
  (:require [clojure.tools.cli :refer [parse-opts]]
            [passman.db :as db]
            [passman.password :refer [generate-password]]
            [passman.stash :as stash]
            [passman.clipboard :refer [copy]]
            [table.core :as t]))

(def cli-options
  [["-l" "--length Length" "Password length"
    :default 40
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-g" "--generate" "Generate new password"]
   [nil "--list"]])

(defn password-input []
  (println "Enter your master key:")
  (String. (.readPassword (System/console))))

(defn -main [& args]
  (let [parsed-options (parse-opts args cli-options)
        options (:options parsed-options)
        [url username] (:arguments parsed-options)]
    (cond
      (:generate options) (do
                            (stash/stash-init (password-input))
                            (let [password (generate-password (:length options))]
                              (db/insert-password! url username)
                              (stash/insert-password! url username password)
                              (println "added password")
                              (copy password)))
      (and url username) (do
                           (stash/stash-init (password-input))
                           (let [password (stash/find-password url username)]
                             (copy password)
                             (println "Password copied to clipboard")))
      (options :list) (t/table (db/list-passwords)))))

(comment
  (-main))