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
   ["-h" "--help" "Show help"]
   ["-u" "--usage" "Show usage"]
(def usage
  (str "Passman CLI Password Manager v. 0.0.1" "\n"
       "\n"
       "Usage: passman <url> <username>" "\n"
       "\n"
       "Default behavior: Looks up password for the given <url> and <username>, copies to clipboard." "\n"
       "\n"
       "Available options:" "\n"
       "  [-h | --help] | [-u | --usage]        Show this help text. Ignores <url> and <username>." "\n"
       "  [-g | --generate]                     Generate a new password for given <url> and <username." "\n"
       "                                          Copies generated password to clipboard and saves to database." "\n"
       "  [-l <length> | --length <length>]     Specify length of password generated with [-g | --generate]. Defaults to 40." "\n"
       "  [--list]                              Print table of all urls and usernames with stored passwords." "\n"
       "                                          Runs only if <url> and <username> are not supplied."))

(defn password-input []
  (println "Enter your master key:")
  (String. (.readPassword (System/console))))

(defn -main [& args]
  (let [parsed-options (parse-opts args cli-options)
        options (:options parsed-options)
        [url username] (:arguments parsed-options)]
    (cond
      (or (:help options) (:usage options)) (println usage)
      (and url username) (do
                           (stash/stash-init (password-input))
                           (let [password (stash/find-password url username)]
                             (copy password)
                             (println "Password copied to clipboard")))
      (options :list) (t/table (db/list-passwords)))))

(comment
  (-main))