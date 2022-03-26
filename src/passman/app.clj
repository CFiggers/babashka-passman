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
   [nil "--list"]
   ["-h" "--help" "Show help"]
   ["-u" "--usage" "Show usage"]
   ["-p" "--password Password" "Manual password to add with given <url> and <username>."]
   ["-a" "--add" "Adds a manually provided password to the database."]
   ["-f" "--force-update" "Forces Add and Generate to overwrite existing."]])

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
       "  [-p <pass> | --password <pass>]       Provides a manual password to be stored with the given <url> and <username>." "\n"
       "                                          Ignored if not used with [-a | -add] or [-a! | -add!]." "\n"
       "  [-a | --add]                          Saves <pass> as a new password. Panics if <url> <username> combo already exists." "\n"
       "  [-f | --force-update]                 Forces [-a | --add] and [-g | --generate] to overwrite existing <url> and <username> combo." "\n"
       "  [--list]                              Print table of all urls and usernames with stored passwords." "\n"
       "                                          Runs only if <url> and <username> are not supplied."))

(defn password-input []
  (println "Enter your master key:")
  (String. (.readPassword (System/console))))

(defn confirm-overwrite []
  (print "Do you want to overwrite? [y/n] ") (flush)
  (let [input (String. (.readLine (System/console)))]
    (case input "y" true "Y" true false)))

(defn add-pass [url username password overwrite]
  (stash/stash-init (password-input))
  (let [existing-pass (stash/find-password url username)]
    (if existing-pass
      (do (println "This url/username combo already exists!")
          (when overwrite
            (let [confirm (confirm-overwrite)]
              (if confirm
                (do (stash/update-password! url username password)
                    (println "Updated password, copied to clipboard")
                    (copy password))
                (println "Canceled")))))
      (do (db/insert-password! url username)
          (stash/insert-password! url username password)
          (println "Added password, copied to clipboard")
          (copy password)))))

(defn generate-new-pass [url username length overwrite]
  (let [password (generate-password length)]
    (add-pass url username password overwrite)))

(defn -main [& args]
  (let [parsed-options (parse-opts args cli-options)
        options (:options parsed-options)
        [url username] (:arguments parsed-options)]
    ;; (println options) ;; Uncomment to debug options
    (cond
      (or (:help options) (:usage options)) (println usage)
      (:generate options) (generate-new-pass url username (:length options) (:force-update options))
      (:add options) (if (:password options)
                       (add-pass url username (:password options) (:force-update options))
                       (println "Provide a [-p <pass> | --password <pass>] argument with [-a | --add]"))
      (and url username) (do
                           (stash/stash-init (password-input))
                           (let [password (stash/find-password url username)]
                             (copy password)
                             (println "Password copied to clipboard")))
      (:list options) (t/table (db/list-passwords)))))

(comment
  (-main))